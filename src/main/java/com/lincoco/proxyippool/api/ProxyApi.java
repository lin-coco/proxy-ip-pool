package com.lincoco.proxyippool.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.schedule.FetcherIpSchedule;
import com.lincoco.proxyippool.service.ProxyIpService;
import com.lincoco.proxyippool.webfetcher.DefaultPipeline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@RestController
@RequestMapping("/proxy")
public class ProxyApi {

    @Autowired
    private ProxyIpService proxyIpService;

//    @Autowired
//    private DefaultPipeline defaultPipeline;
//
//    @Autowired
//    private ApplicationContext applicationContext;

    @GetMapping
    public List<ProxyIp> getListProxyIp(@RequestParam(required = false) String region,
                                        @RequestParam(required = false) Integer anonymous,
                                        @RequestParam(required = false) Boolean https,
                                        @RequestParam(required = false) Float speed){
        List<ProxyIp> list = proxyIpService.list(new LambdaQueryWrapper<ProxyIp>()
                .eq(anonymous != null, ProxyIp::getAnonymous, anonymous)
                .eq(https != null, ProxyIp::getHttps, https)
                .lt(speed != null, ProxyIp::getSpeed, speed));
        //先暂时不验证，否则可能请求太慢
//        List<ProxyIp> failureCollect = list.parallelStream().filter(l -> !defaultPipeline.checkFailure(l)).collect(Collectors.toList());
//        list.removeAll(failureCollect);
//        //delete
//        ProxyApi bean = applicationContext.getBean(ProxyApi.class);
//        bean.delete(failureCollect);
//        bean.update(list);
        if (region != null){
            return list.stream().filter(l -> l.getRegion().contains(region)).collect(Collectors.toList());
        }
        return list;
    }

    @Async
    public void delete(List<ProxyIp> failureCollect){
        if (FetcherIpSchedule.fetcherRunning.get()){
            log.info("访问api，删除无效的ip失败，因为正在完成抓取任务");
            return;
        }
        proxyIpService.removeByIds(failureCollect.stream().map(ProxyIp::getId).collect(Collectors.toList()));
    }

    @Async
    public void update(List<ProxyIp> successCollect){
        if (FetcherIpSchedule.fetcherRunning.get()){
            log.info("访问api，删除无效的ip失败，因为正在完成抓取任务");
            return;
        }
        proxyIpService.updateBatchById(successCollect);
    }
}
