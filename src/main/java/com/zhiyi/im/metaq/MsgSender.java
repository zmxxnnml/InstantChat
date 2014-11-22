package com.zhiyi.im.metaq;

import com.alibaba.rocketmq.client.producer.SendResult;

import java.io.Serializable;

public interface MsgSender {

    SendResult sendMessage(Serializable message, String topic, String tag);
}
