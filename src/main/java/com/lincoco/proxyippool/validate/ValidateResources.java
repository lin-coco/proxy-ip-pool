package com.lincoco.proxyippool.validate;

import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.schedule.FetcherIpSchedule;
import com.lincoco.proxyippool.service.ProxyIpService;
import com.lincoco.proxyippool.webfetcher.DefaultPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@Component
public class ValidateResources {

    @Autowired
    private ProxyIpService proxyIpService;

    @Autowired
    private DefaultPipeline defaultPipeline;

    @Async
    @Scheduled(cron = "30 */10 * * * ?")
    public void validateJob(){
        if (FetcherIpSchedule.fetcherRunning.get()){
            log.info("抓取ip线程正在执行，拒绝执行验证 " + LocalDateTime.now());
            return;
        }
        log.info("开始进行数据库ip验证 " + LocalDateTime.now());
        List<ProxyIp> list = proxyIpService.list();
        List<ProxyIp> failureCollect = list.parallelStream().filter(p -> !defaultPipeline.checkFailure(p)).collect(Collectors.toList());
        int size = failureCollect.size();
        list.removeAll(failureCollect);
        proxyIpService.removeByIds(failureCollect.stream().map(ProxyIp::getId).collect(Collectors.toList()));
        proxyIpService.updateBatchById(list);
        log.info("数据库ip验证结束 无效删除的ip有 " +size +" 个 " + LocalDateTime.now());
    }
}
