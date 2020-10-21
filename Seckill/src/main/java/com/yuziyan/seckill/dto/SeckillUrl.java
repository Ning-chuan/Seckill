package com.yuziyan.seckill.dto;

public class SeckillUrl {

    //是否可用（商品是否存在，库存是否足够等等）
    private boolean enable;
    //此商品的专属md5
    private String md5;
    //商品ID
    private int itemId;

    public SeckillUrl() {
    }
    public SeckillUrl(boolean enable, int itemId) {
        this.enable = enable;
        this.itemId = itemId;
    }
    public SeckillUrl(boolean enable, String md5, int itemId) {
        this.enable = enable;
        this.md5 = md5;
        this.itemId = itemId;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
