package com.ygoj.record.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "record")
@Data
public class RecordProperties {
    int test;
    String a;
}
