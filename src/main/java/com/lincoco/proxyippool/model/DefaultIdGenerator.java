package com.lincoco.proxyippool.model;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Component
public class DefaultIdGenerator implements IdentifierGenerator {

    @Override
    public Number nextId(Object entity) {
        return null;
    }

    @Override
    public String nextUUID(Object entity) {
        ProxyIp proxyIp = (ProxyIp) entity;
        String ip = proxyIp.getIp();
        Integer port = proxyIp.getPort();
        return ip+":"+port;
    }
}
