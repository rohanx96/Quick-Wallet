package com.rose.quickwallet.transactions;

/**
 *
 * Created by rose on 3/8/15.
 *
 */
public class DetailsRecyclerViewItem {
    private String type;
    private String detail;
    private long time;
    private float amount;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


}
