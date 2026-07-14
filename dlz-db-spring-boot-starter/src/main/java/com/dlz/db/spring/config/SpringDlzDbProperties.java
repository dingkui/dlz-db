package com.dlz.db.spring.config;

import com.dlz.db.core.DlzDbProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dlz.db")
public class SpringDlzDbProperties extends DlzDbProperties {
}