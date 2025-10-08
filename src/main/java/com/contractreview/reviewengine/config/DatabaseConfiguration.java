package com.contractreview.reviewengine.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.contractreview.reviewengine.domain.repository")
@EntityScan(basePackages = "com.contractreview.reviewengine.domain.model")
@EnableTransactionManagement
public class DatabaseConfiguration {
    
    // JPA配置已通过Spring Boot自动配置处理
}