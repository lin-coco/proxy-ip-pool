package com.lincoco.proxyippool.webfetcher.impl;

import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.webfetcher.FetcherIpService;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
public class IpyqieServiceImpl implements FetcherIpService {

    private static final String ADDRESS = "http://ip.yqie.com/ipproxy.htm/";

    @Override
    public void fetcherIps() {

    }

    private static class DefaultProcess implements PageProcessor{

        @Override
        public void process(Page page) {

        }

        @Override
        public Site getSite() {
            return null;
        }
    }
}
