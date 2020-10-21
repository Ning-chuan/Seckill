package com.yuziyan.seckill.entity;


import java.util.Date;

/**
 * 秒杀订单
 */
public class SeckillOrder {

    private Integer id;
    private String orderCode;//订单编码，方便验证
    private Integer seckillItemId;
    private Integer userId;
    // state表示订单的状态
    // 1 下单成功，未支付
    // 2 已支付
    // 4 订单过期（超过支付时间）
    private Integer state;
    private Date createTime;
    //订单超时时长：默认300秒（即5分钟）  单位：秒
    private Integer orderTimeout;

    public SeckillOrder() {
    }

    public SeckillOrder(int seckillItemId, int userId, int state, Date createTime) {
        this.id = id;
        this.seckillItemId = seckillItemId;
        this.userId = userId;
        this.state = state;
        this.createTime = createTime;
    }

    public SeckillOrder(String orderCode, Integer seckillItemId, Integer userId, Integer state, Date createTime, Integer orderTimeout) {
        this.orderCode = orderCode;
        this.seckillItemId = seckillItemId;
        this.userId = userId;
        this.state = state;
        this.createTime = createTime;
        this.orderTimeout = orderTimeout;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public Integer getSeckillItemId() {
        return seckillItemId;
    }

    public void setSeckillItemId(Integer seckillItemId) {
        this.seckillItemId = seckillItemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getOrderTimeout() {
        return orderTimeout;
    }

    public void setOrderTimeout(Integer orderTimeout) {
        this.orderTimeout = orderTimeout;
    }

    @Override
    public String toString() {
        return "SeckillOrder{" +
                "id=" + id +
                ", orderCode='" + orderCode + '\'' +
                ", seckillItemId=" + seckillItemId +
                ", userId=" + userId +
                ", state=" + state +
                ", createTime=" + createTime +
                ", orderTimeout=" + orderTimeout +
                '}';
    }
}
