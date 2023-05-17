package com.zhuanyi.leveldb.core.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "log")
@Data
public class LogConfiguration {

    /**
    @Value("${fileMaxSize}")
    private long fileMaxSize;**/

    @Value("${path}")
    private String path;

}
