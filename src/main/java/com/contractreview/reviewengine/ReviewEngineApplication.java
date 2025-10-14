package com.contractreview.reviewengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
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
@EnableFeignClients(basePackages = "com.contract.common.feign")
@EnableTransactionManagement
public class ReviewEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewEngineApplication.class, args);
    }
}