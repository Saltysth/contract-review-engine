package com.contractreview.reviewengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 审查引擎应用启动类
 *
 * @author SaltyFish
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.contract.common.feign")
@EnableJpaRepositories(basePackages = "com.contractreview.reviewengine.infrastructure.persistence.repository")
@EntityScan(basePackages = "com.contractreview.reviewengine.infrastructure.persistence.entity")
@ComponentScan(basePackages = {"com.contractreview.reviewengine", "com.contractreview.fileapi"})
@EnableTransactionManagement
public class ReviewEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewEngineApplication.class, args);
    }
}