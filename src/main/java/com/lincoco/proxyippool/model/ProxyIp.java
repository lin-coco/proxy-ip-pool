package com.lincoco.proxyippool.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author ：xys
 * @description：TODO
 * @date ：2022/10/30
 */
@Data
@Builder
@TableName("proxy_ip")
public class ProxyIp {

//    @TableId(type = IdType.ASSIGN_UUID)
    @TableId
    private String id;

    private String ip;

    private Integer port;

    /**
     * 区域
     */
    private String region;

    /**
     * 匿名级别  1：透明   2：普通   3：高匿
     */
    private Integer anonymous;

    /**
     * 是否支持https
     */
    private Boolean https;

    /**
     * 响应速度
     */
    private Float speed;

    /**
     * 来源
     */
    private String source;

    /**
     * 保存时间
     */
    private LocalDateTime saveTime;

    /**
     * 验证次数
     */
    private Integer valiCount;
}
