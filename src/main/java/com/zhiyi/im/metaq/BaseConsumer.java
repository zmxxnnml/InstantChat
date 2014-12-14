package com.zhiyi.im.metaq;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.common.message.MessageExt;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseConsumer implements MessageListenerConcurrently {

    private final static Logger log = LoggerFactory.getLogger(BaseConsumer.class);

    protected DefaultMQPushConsumer consumer;
    
    protected String nameServer;
    
    protected int minConsumeThread = 2;
    
    protected int maxConsumeThread = 5;
    
    protected String group;
    
    protected String subExpression;
    
    protected TopicEnum topicEnum;

    private final static int[] DELAY_LEVELS = new int[]{3, 5, 9, 14, 15, 16, 17, 18, 19, 20, 21};
    
    protected int maxRetryCount = 10;

    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }

    public void setMinConsumeThread(int minConsumeThread) {
        this.minConsumeThread = minConsumeThread;
    }

    public void setMaxConsumeThread(int maxConsumeThread) {
        this.maxConsumeThread = maxConsumeThread;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setSubExpression(String subExpression) {
        this.subExpression = subExpression;
    }
    
    public void init() throws MQClientException {
        if ("localTest".equals(nameServer)) {
            return;
        }
        
        topicEnum = getTopicEnum();
        if (StringUtils.isBlank(group)) {
            group = "S_" + topicEnum.getTopic() + "_" + topicEnum.getTags();
        }
        consumer = new DefaultMQPushConsumer(group);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeThreadMin(minConsumeThread);
        consumer.setConsumeThreadMax(maxConsumeThread);

        try {
            consumer.setInstanceName("DEFAULT_CONSUMER-" +
            		InetAddress.getLocalHost().getHostName() + "_" + topicEnum.getTopic());
        } catch (UnknownHostException e) {
            log.error("getHostName error", e);
        }

        if (StringUtils.isBlank(subExpression)) {
            subExpression = topicEnum.getTags();
        }
        consumer.subscribe(topicEnum.getTopic(), subExpression);
        
        try {
            consumer.registerMessageListener(this);
            consumer.start();
        } catch (MQClientException e) {
            log.error(e.getMessage(), e);
        }
        
        log.info("consumer start! ns={},topic={},subExpression={},group={}",
        		nameServer, topicEnum.getTopic(), subExpression, group);
    }

    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("consumer shutdown! topic=" + 
            		topicEnum.getTopic() + ",subExpression=" + subExpression + ",group=" + group);
        }
    }

    public abstract TopicEnum getTopicEnum();

    public abstract void doLogErrorConsumeMessage(MsgObj msgObj);

    public abstract ConsumeConcurrentlyStatus doConsumeMessage(MsgObj msgObj);

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                    ConsumeConcurrentlyContext context) {
        log.info("receive_message:" + msgs.toString());
        
        if (msgs == null || msgs.size() < 1) {
            log.error("receive empty msg!");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }

        List<Serializable> msgList = new ArrayList<Serializable>();
        for (MessageExt message : msgs) {
            msgList.add(decodeMsg(message));
        }

        final int reconsumeTimes = msgs.get(0).getReconsumeTimes();
        MsgObj msgObj = new MsgObj();
        msgObj.setReconsumeTimes(reconsumeTimes);
        msgObj.setMsgList(msgList);
        msgObj.setContext(context);
        context.setDelayLevelWhenNextConsume(getDelayLevelWhenNextConsume(reconsumeTimes));

        ConsumeConcurrentlyStatus status = doConsumeMessage(msgObj);
        return status;
    }

    public int getDelayLevelWhenNextConsume(int reconsumeTimes) {
        if (reconsumeTimes >= DELAY_LEVELS.length) {
            return DELAY_LEVELS[DELAY_LEVELS.length - 1];
        }
        
        return DELAY_LEVELS[reconsumeTimes];
    }

    private Serializable decodeMsg(MessageExt msg) {
        if (msg == null) {
            return null;
        }

        try {
            return HessianUtils.decode(msg.getBody());
        } catch (IOException e) {
            log.error("Deserialized error!" + e.getMessage(), e);
            return null;
        }
    }

}
