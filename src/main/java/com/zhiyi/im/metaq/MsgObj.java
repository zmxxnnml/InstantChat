package com.zhiyi.im.metaq;

import java.io.Serializable;
import java.util.List;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;

public class MsgObj {
    private int reconsumeTimes;
    List<Serializable> msgList;
    private String errorMsg;
    private ConsumeConcurrentlyContext context;

    public int getReconsumeTimes() {
        return reconsumeTimes;
    }

    public void setReconsumeTimes(int reconsumeTimes) {
        this.reconsumeTimes = reconsumeTimes;
    }

    public List<Serializable> getMsgList() {
        return msgList;
    }

    public void setMsgList(List<Serializable> msgList) {
        this.msgList = msgList;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public ConsumeConcurrentlyContext getContext() {
        return context;
    }

    public void setContext(ConsumeConcurrentlyContext context) {
        this.context = context;
    }


}
