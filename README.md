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