package com.dlz.db.config;

import com.dlz.db.core.BaseDbProperties;
import com.dlz.db.core.BaseDbProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "dlz.db")
public class DlzDbProperties extends BaseDbProperties{
}