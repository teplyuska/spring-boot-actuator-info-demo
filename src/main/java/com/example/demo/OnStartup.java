package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.GitProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

@Component
public class OnStartup implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(OnStartup.class);
    private final ProjectInfoProperties projectInfoProperties;

    public OnStartup(ProjectInfoProperties projectInfoProperties) {
        this.projectInfoProperties = projectInfoProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info(projectInfoProperties.getGit().getLocation().getFilename());

        try {
            var gitProperties = new GitProperties(loadFrom(this.projectInfoProperties.getGit().getLocation(), "git", this.projectInfoProperties.getGit().getEncoding()));
            logger.info(gitProperties.get("commit.id.full"));
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    protected Properties loadFrom(Resource location, String prefix, Charset encoding) throws IOException {
        prefix = prefix.endsWith(".") ? prefix : prefix + ".";
        Properties source = loadSource(location, encoding);
        logger.info(source.toString());
        Properties target = new Properties();
        for (String key : source.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                target.put(key.substring(prefix.length()), source.get(key));
            }
        }
        return target;
    }

    private Properties loadSource(Resource location, Charset encoding) throws IOException {
        if (encoding != null) {
            return PropertiesLoaderUtils.loadProperties(new EncodedResource(location, encoding));
        }
        return PropertiesLoaderUtils.loadProperties(location);
    }
}
