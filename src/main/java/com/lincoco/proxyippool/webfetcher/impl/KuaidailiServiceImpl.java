package com.lincoco.proxyippool.webfetcher.impl;

import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.webfetcher.DefaultPipeline;
import com.lincoco.proxyippool.webfetcher.FetcherIpService;
import com.lincoco.proxyippool.webfetcher.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@Service
public class KuaidailiServiceImpl implements FetcherIpService {

    @Autowired
    private DefaultPipeline defaultPipeline;


    private static final String PLATFORM = "kuaidaili";

    private static final String ADDRESS = "https://www.kuaidaili.com/";

    private static final Site SITE = Site.me()
            .setDomain(ADDRESS)
            .setCharset("UTF-8")
            .addHeader("Cookie","channelid=0; _gcl_au=1.1.2034628318.1667062735; Hm_lvt_7ed65b1cc4b810e9fd37959c9bb51b31=1667062735; _ga=GA1.2.1459704711.1667062736; _gid=GA1.2.1877004391.1667062736; sid=1667104572590429; _gat=1; Hm_lpvt_7ed65b1cc4b810e9fd37959c9bb51b31=1667105172")
            .addHeader("User-Agent", UserAgent.getRandomAgent())
            .setRetryTimes(3);

    @Override
    public void fetcherIps() {

        String[] urls = new String[5];
        for (int i = 1; i <= 5; i++) {
            urls[i-1] = "https://www.kuaidaili.com/free/inha/"+i+"/";
        }
        Spider.create(new DefaultProcess())
                .addPipeline(defaultPipeline)
                .addUrl(urls)
                .run();
    }

    private static class DefaultProcess implements PageProcessor{

        @Override
        public void process(Page page) {
            String s = page.getUrl().get();
            log.info(s + " 开始爬取-------------");
            List<String> all = page.getHtml().xpath("//tbody/tr").all();
            List<ProxyIp> proxyIps = new ArrayList<>(all.size());
            for (String tr : all) {
                tr = tr.replaceAll("tr","div").replaceAll("td","div");
                Html html = new Html(tr);
                String ip = html.xpath("//div[@data-title='IP']/text()").get();
                String port = html.xpath("//div[@data-title='PORT']/text()").get();
                String anonymous = html.xpath("//div[@data-title='匿名度']/text()").get();
                String https = html.xpath("//div[@data-title='类型']/text()").get();
                String region = html.xpath("//div[@data-title='位置']/text()").get();
                String speed = html.xpath("//div[@data-title='响应速度']/text()").get();
                ProxyIp proxyIp = ProxyIp.builder()
                        .source(PLATFORM)
                        .ip(ip)
                        .port(Integer.parseInt(port))
                        .anonymous("高匿名".equals(anonymous) ? 3 : 1)
                        .https("HTTPS".equals(https))
                        .region(region)
                        .speed(Float.parseFloat(speed.replace("秒", "")))
                        .valiCount(0).build();
                log.info(proxyIp.toString());
                proxyIps.add(proxyIp);
            }
            page.putField("proxyIps",new LinkedList<ProxyIp>());
            ((List<ProxyIp>)page.getResultItems().get("proxyIps")).addAll(proxyIps);
        }

        @Override
        public Site getSite() {
            return SITE;
        }
    }

    public static void main(String[] args){
        String[] urls = new String[5];
        for (int i = 1; i <= 5; i++) {
            urls[i-1] = "https://www.kuaidaili.com/free/inha/"+i+"/";
        }
        long currentTimeMillis = System.currentTimeMillis();
        DefaultPipeline defaultPipeline = new DefaultPipeline();
        Spider.create(new DefaultProcess())
                .addUrl(urls)
                .addPipeline(defaultPipeline)
                .thread(1) //网站可能防止多个线程同时爬取
                .runAsync();
        System.out.println("总共花费 " + (System.currentTimeMillis() - currentTimeMillis));
    }
}
