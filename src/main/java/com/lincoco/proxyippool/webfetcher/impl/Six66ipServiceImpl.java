package com.lincoco.proxyippool.webfetcher.impl;

import com.lincoco.proxyippool.model.ProxyIp;
import com.lincoco.proxyippool.webfetcher.DefaultPipeline;
import com.lincoco.proxyippool.webfetcher.FetcherIpService;
import com.lincoco.proxyippool.webfetcher.UserAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Slf4j
@Component
public class Six66ipServiceImpl implements FetcherIpService {

    @Autowired
    private DefaultPipeline defaultPipeline;

    private static final String PLATFORM = "66代理";

    private static final String ADDRESS = "http://www.66ip.cn/";

    private static final Site SITE = Site.me()
            .setDomain(ADDRESS)
//            .addHeader("Accept-Encoding","gzip, deflate")
//            .setCharset("UTF-8")
            .addHeader("Cookie","Hm_lvt_1761fabf3c988e7f04bec51acd4073f4=1667063049; Hm_lpvt_1761fabf3c988e7f04bec51acd4073f4=1667186394")
            .addHeader("User-Agent", UserAgent.getRandomAgent())
            .setRetryTimes(3);


    @Override
    public void fetcherIps() {
        String url1 = "http://www.66ip.cn/areaindex_50/1.html";
        String url2 = "http://www.66ip.cn/areaindex_50/2.html";

        Spider.create(new DefaultProcess())
                .addPipeline(defaultPipeline)
                .addUrl(url1,url2)
                .thread(2)
                .run();
    }

    public static void main(String[] args) {
        String url1 = "http://www.66ip.cn/areaindex_50/1.html";
//        String url2 = "http://www.66ip.cn/areaindex_50/2.html";

//        DefaultPipeline defaultPipeline = new DefaultPipeline();
        Spider.create(new DefaultProcess())
//                .addPipeline(defaultPipeline)
                .addUrl(url1)
                .thread(1)
                .run();
    }

    private static class DefaultProcess implements PageProcessor{

        @Override
        public void process(Page page) {
            String s = page.getUrl().get();
            log.info(s + " 开始爬取-------------");
            List<String> all = page.getHtml().xpath("//div[@id='footer']//table/tbody/tr").all();
            all.remove(0);
            all = all.subList(0,100);
//            System.out.println("总共有 " + all.size() + " 个数据");
            BlockingQueue<ProxyIp> proxyIps = new ArrayBlockingQueue<>(all.size());
            all.parallelStream()
                    .map(t -> t.replaceAll("tr","div").replaceAll("td","div"))
                    .forEach(t -> {
                        Html html = new Html(t);
                        List<String> ipDetail = html.xpath("//div/allText()").all();
//                        System.out.println(Arrays.toString(ipDetail.toArray()));
                        String ip = ipDetail.get(1);
                        String port = ipDetail.get(2);
                        String region = ipDetail.get(3);
                        String anonymous = ipDetail.get(4);
                        ProxyIp proxyIp = ProxyIp.builder()
                                .ip(ip)
                                .port(Integer.parseInt(port))
                                .region(region)
                                .anonymous("高匿代理".equals(anonymous) ? 3 : 1)
                                .source(PLATFORM)
                                .speed((float) -1)
                                .https(false)
                                .valiCount(0)
                                .build();
                        log.info(proxyIp.toString());
                        proxyIps.add(proxyIp);
                    });
            List<ProxyIp> proxyIpList = new ArrayList<>(proxyIps);
//            System.out.println(proxyIpList.size() + " 个数据处理完毕--------");
            page.putField("proxyIps",proxyIpList);
        }

        @Override
        public Site getSite() {
            return SITE;
        }
    }
}
