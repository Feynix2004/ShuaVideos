package org.shuavideos;

import org.mybatis.spring.annotation.MapperScan;
import org.shuavideos.authority.AuthorityUtils;
import org.shuavideos.authority.BaseAuthority;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAsync
@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "org.shuavideos.mapper")
public class ShuaVideosApplication {

    public static void main(String[] args) {

        AuthorityUtils.setGlobalVerify(true,new BaseAuthority());
        SpringApplication.run(ShuaVideosApplication.class, args);
    }

}
