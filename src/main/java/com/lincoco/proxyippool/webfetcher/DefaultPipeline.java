package com.lincoco.proxyippool.webfetcher;

import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.service.ProxyIpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@Component
public class DefaultPipeline implements Pipeline {

    @Autowired
    private ProxyIpService proxyIpService;


    @Override
    public void process(ResultItems resultItems, Task task) {
        List<ProxyIp> kuaidailiProxyIds = resultItems.get("proxyIps");
        if (kuaidailiProxyIds != null){
            log.info("pipeline："+Thread.currentThread().getName()+"长度为："+kuaidailiProxyIds.size());
            int allSize = kuaidailiProxyIds.size();

            List<List<ProxyIp>> listProxyIps = proxyIpSubList(kuaidailiProxyIds);

            AtomicInteger addNumber = new AtomicInteger();
            AtomicInteger updateNumber = new AtomicInteger();
            List<List<ProxyIp>> collectProxyIp = listProxyIps.stream()
                    .map(this::filterFailure)
                    .peek(proxyIps -> {
                        for (ProxyIp proxyIp : proxyIps) {
                            proxyIp.setId(proxyIp.getIp() + ":" + proxyIp.getPort());
                        }
                    })
                    .peek(proxyIps -> {
                        for (ProxyIp p : proxyIps) {
                            if (proxyIpService.getById(p.getId()) == null){
                                proxyIpService.save(p);
                                addNumber.getAndIncrement();
                            }else {
                                proxyIpService.updateById(p);
                                updateNumber.getAndIncrement();
                            }
                        }
                        log.info("保存到数据库---" + proxyIps.size() + "个");
                    })
                    .collect(Collectors.toList());
            log.info("pipeline 处理一共 " + allSize + " 个数据，最终保存到数据库--" + addNumber.get() + "个数据，更新数据库--" + updateNumber.get() +"个数据");
        }
    }
    /**
     * 过滤失效代理ip
     */
    public List<ProxyIp> filterFailure(List<ProxyIp> proxyIps){
        return proxyIps.parallelStream()
                .filter(this::checkFailure)
                .collect(Collectors.toList());
    }

    /**
     * 检查代理ip的可用性
     */
    public boolean checkFailure(ProxyIp proxyIp){
        InputStream inputStream = null;
        try {
            URL url = new URL("http://www.baidu.com");
            InetSocketAddress address = new InetSocketAddress(proxyIp.getIp(),proxyIp.getPort());
            Proxy proxy = new Proxy(Proxy.Type.HTTP,address);
            URLConnection urlConnection = url.openConnection(proxy);
            urlConnection.setConnectTimeout(5 * 1000);
            inputStream = urlConnection.getInputStream();
            String s = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
//            log.info(s);
            if (s.indexOf("百度") > 0){
                log.info("有效ip：" + proxyIp.getIp() + ":" + proxyIp.getPort());
            }else {
                log.info("异常结果" + " 无效ip：" + proxyIp.getIp() + ":" + proxyIp.getPort() + "将过滤");
                return false;
            }
            proxyIp.setValiCount(proxyIp.getValiCount() + 1);
            return true;
        } catch (IOException e) {
            log.info(e.getMessage() + " 无效ip：" + proxyIp.getIp() + ":" + proxyIp.getPort() + "将过滤");
            return false;
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public List<List<ProxyIp>> proxyIpSubList(List<ProxyIp> proxyIps){

        List<List<ProxyIp>> listProxyIps = new ArrayList<>();

        int size = proxyIps.size();
        if (size < 15){
            listProxyIps.add(proxyIps);
            return listProxyIps;
        }

        int index = 15;

        while (index < size){
            List<ProxyIp> proxyIps1 = proxyIps.subList(index - 15, index);
            listProxyIps.add(proxyIps1);
            index += 15;
        }
        index -= 15;
        List<ProxyIp> proxyIps1 = proxyIps.subList(index, size);
        listProxyIps.add(proxyIps1);
        log.info("原" + size + "长度集合，被分割成 " + listProxyIps.size() + "个集合，每个集合最多15长度");
        return listProxyIps;
    }
}
