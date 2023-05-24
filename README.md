# Why does info endpoint not return git info when running in azure app service image?

```
Running command: java -cp /home/site/wwwroot/demo-0.0.1-SNAPSHOT.jar:/usr/local/appservice/lib/azure.appservice.jar: -Djava.util.logging.config.file=/usr/local/appservice/logging.properties -Dfile.encoding=UTF-8  -Dserver.port=80 -XX:ErrorFile=/home/LogFiles/java_error__dev_%p.log -XX:+CrashOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/LogFiles/java_memdump__dev.log -Duser.dir=/home/site/wwwroot org.springframework.boot.loader.JarLauncher
```

## Simulate image without all the specific configs

    java -cp target/demo-0.0.1-SNAPSHOT.jar org.springframework.boot.loader.JarLauncher

Results in:

    com.example.demo.DemoApplication         : The following 1 profile is active: "test"
    ...
    o.s.b.a.e.web.EndpointLinksResolver      : Exposing 3 endpoint(s) beneath base path '/actuator'
    ...
    com.example.demo.OnStartup               : 81cb5797956ce219de91ae101895e5bac60ebf54

And

    curl http://localhost:8080/actuator/info

Results in:

```
{
    "git": {
        "commit": {
            "id": {
                "full": "81cb5797956ce219de91ae101895e5bac60ebf54"
            }
        },
        "branch": "master",
        "build": {
            "time": "2023-05-24T12:28:03Z"
        }
    }
}
```

## Running with the image


    docker run -p 8080:80 -v MY_PATH/demo/target:/home/site/wwwroot -i -t mcr.microsoft.com/azure-app-service/java:17-java17_221014210614

Results in:

    Running command: java -cp /home/site/wwwroot/demo-0.0.1-SNAPSHOT.jar:/usr/local/appservice/lib/azure.appservice.jar: -Djava.util.logging.config.file=/usr/local/appservice/logging.properties -Dfile.encoding=UTF-8  -Dserver.port=80 -XX:ErrorFile=/home/LogFiles/java_error__dev_%p.log -XX:+CrashOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/LogFiles/java_memdump__dev.log -Duser.dir=/home/site/wwwroot org.springframework.boot.loader.JarLauncher
    ...
    com.example.demo.DemoApplication         : The following 1 profile is active: "test"
    ...
    o.s.b.a.e.web.EndpointLinksResolver      : Exposing 3 endpoint(s) beneath base path '/actuator'
    ...
    com.example.demo.OnStartup               : null

And

    curl http://localhost:8080/actuator/info

Results in:

```
{
    "git": {}
}
```

## Summery

Clearly both properties files are read and present in the jar file since the test profile is activated which contains the actuator configurations.

```
$ jar tvf target/demo-0.0.1-SNAPSHOT.jar | grep BOOT-INF/classes/
0 Wed May 24 14:28:06 CEST 2023 BOOT-INF/classes/
0 Wed May 24 14:28:04 CEST 2023 BOOT-INF/classes/com/
0 Wed May 24 14:28:04 CEST 2023 BOOT-INF/classes/com/example/
0 Wed May 24 14:28:04 CEST 2023 BOOT-INF/classes/com/example/demo/
1570 Wed May 24 14:28:04 CEST 2023 BOOT-INF/classes/com/example/demo/OnStartup.class
733 Wed May 24 14:28:04 CEST 2023 BOOT-INF/classes/com/example/demo/DemoApplication.class
411 Wed May 24 14:28:02 CEST 2023 BOOT-INF/classes/application-test.properties
27 Wed May 24 14:28:02 CEST 2023 BOOT-INF/classes/application.properties
155 Wed May 24 14:28:02 CEST 2023 BOOT-INF/classes/git.properties
```

And it seems the git.properties file is also present, but not read when running in the azure image?

## Solution

It seems the additional jar files added by java -cp already contained a git.properties file with information about the additional jar.

    {"git.commit.message.short"="Merged PR 7721155: [ANT101] Merge Dev into Master",, "git.remote.origin.url"="https://msazure.visualstudio.com/One/_git/AAPT-Antares-Websites-Extensions-Java",, "git.dirty"="false",, "git.closest.tag.name"="",, "git.branch"="5d33e9c40fdd4248b64cf6242ae5eeabac44a1c6",, "git.tags"="",, "git.build.time"="2023-03-05T03:43:51+0000",, "git.commit.id.describe-short"="5d33e9c",, "git.closest.tag.commit.count"="",, "git.local.branch.ahead"="NO_REMOTE",, "git.commit.message.full"="Merged PR 7721155: [ANT101] Merge Dev into Master
    
    Merged PR 7698940: Add dependency on commons-text 1.10.0 to mitigate CVE-2022-42889
    
    Related work items: #17158979",, "git.commit.id.describe"="5d33e9c",, "git.commit.time"="2023-03-05T03:30:25+0000",, "git.total.commit.count"="111", "git.build.version"="1.0",, {=, "git.commit.id"="5d33e9c40fdd4248b64cf6242ae5eeabac44a1c6",, "git.local.branch.behind"="NO_REMOTE",, "git.commit.id.abbrev"="5d33e9c",, }=}

How to find:  
https://github.com/teplyuska/spring-boot-actuator-info-demo/commit/8fb8ae5da1495bd83045c46bda021d46729adc3d

To solve this I added:

    <generateGitPropertiesFilename>${project.build.outputDirectory}/my-git.properties</generateGitPropertiesFilename>

and

    spring.info.git.location=classpath:my-git.properties

More info:  
https://github.com/teplyuska/spring-boot-actuator-info-demo/commit/9b42b6d432dfcc982ec5bcc0d5c8e67eca78c0e8