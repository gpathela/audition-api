package com.audition.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "audition.client")
@Getter
@Setter
public class AuditionClientProperties {

    private String baseUrl;
    private String postsPath;
    private String commentsPath;
}
