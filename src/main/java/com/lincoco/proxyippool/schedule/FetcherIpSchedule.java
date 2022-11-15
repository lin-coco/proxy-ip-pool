package com.lincoco.proxyippool.schedule;

import com.lincoco.proxyippool.webfetcher.FetcherIpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@Component
public class FetcherIpSchedule {

    public volatile static AtomicBoolean fetcherRunning = new AtomicBoolean(false);
//    public static boolean fetcherRunning = false;

    @Autowired
    private ApplicationContext context;

//    @PostConstruct
//    public void init(){
//        exec();
//    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void exec(){
        log.info("抓取ip线程开启，将会抑制验证线程的执行-----------");
        fetcherRunning.compareAndSet(false,true);
//        fetcherRunning = true;
        Map<String, FetcherIpService> fetcherIpServiceMap = context.getBeansOfType(FetcherIpService.class);
        FetcherIpSchedule fetcherIpScheduleBean = context.getBean(FetcherIpSchedule.class);
        log.info("一共有 " + fetcherIpServiceMap.size() + " 爬虫需要执行");
        CountDownLatch countDownLatch = new CountDownLatch(fetcherIpServiceMap.size());
        for (Map.Entry<String, FetcherIpService> entry : fetcherIpServiceMap.entrySet()) {
            FetcherIpService fetcherIpService = entry.getValue();
            fetcherIpScheduleBean.fetcherService(fetcherIpService,countDownLatch);
        }
        try {
            log.info("正在阻塞，等待爬虫全部执行完毕");
            countDownLatch.await();
            log.info("爬虫全部执行完毕！继续执行");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        fetcherRunning.compareAndSet(true,false);
//        fetcherRunning = false;
        log.info("抓取ip线程结束，允许验证线程的执行-----------");
    }

    /**
     * 异步是通过aop代理实现的，不能绕过代理对象直接调用方法，不然异步会失效
     */
    @Async
    public void fetcherService(FetcherIpService fetcherIpService,CountDownLatch countDownLatch){
        fetcherIpService.fetcherIps();
        countDownLatch.countDown();
    }
}
