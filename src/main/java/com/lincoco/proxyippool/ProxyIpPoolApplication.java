package com.lincoco.proxyippool;

import com.lincoco.proxyippool.schedule.FetcherIpSchedule;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
@MapperScan(basePackages = {"com.lincoco.proxyippool.dao"})
public class ProxyIpPoolApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(ProxyIpPoolApplication.class, args);

        //初始化执行...
//        FetcherIpSchedule schedule = run.getBean(FetcherIpSchedule.class);
//        if (!FetcherIpSchedule.fetcherRunning){
//            schedule.exec();
//        }
    }

}
