package com.zhiyi.im.metaq;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

public class MsgSenderService implements MsgSender {
    protected static final Logger logger = LoggerFactory.getLogger(MsgSenderService.class);
    private DefaultMQProducer producer;
    private String nameServer;
    private boolean test = false;

    public void init() {
        if ("localTest".equals(nameServer)) {
            test = true;
            return;
        }
        producer = new DefaultMQProducer("P_user");

        if (StringUtils.isBlank(System.getProperty("rocketmq.namesrv.domain"))) {
            System.setProperty("rocketmq.namesrv.domain", nameServer);
        }
        try {
        	producer.setNamesrvAddr(nameServer);
            try {
                producer.setInstanceName("DEFAULT_MSG_SENDER-"+ InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException e) {
                logger.error("getHostName error",e);
            }
            producer.start();
            logger.info("metaq start success;rocketmq.namesrv.domain={}",
                    System.getProperty("rocketmq.namesrv.domain"));
        } catch (MQClientException e) {
            logger.error(MessageFormat.format("meta start failed!nameServer={0},group={1},excepton={2} ",
                    System.getProperty("rocketmq.namesrv.domain"), "P_fundselling", e.getMessage()), e);
        }
    }

    @Override
    public SendResult sendMessage(Serializable message, String topic, String tag) {
        if (test) {
            SendResult sendResult = new SendResult();
            sendResult.setSendStatus(SendStatus.SEND_OK);
            return sendResult;
        }
        try {
            logger.info("send mq mesage, message={}", message.toString());
            Message msg = new Message(topic, tag, HessianUtils.encode(message));
            SendResult sendResult = producer.send(msg);
            logger.info("sendResult={},message={}", sendResult, message.toString());
            return sendResult;
        } catch (IOException ioe) {
            logger.error(MessageFormat.format("meta send msg failed   message={} ", message), ioe);
            throw new RuntimeException("meta send msg failed   message=" + message);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    public void destroy() {
        if (producer != null) {
            producer.shutdown();
            logger.info("metaq 发送端关闭成功;rocketmq.namesrv.domain={}",
                    System.getProperty("rocketmq.namesrv.domain"));
        }
    }

    /**
     * Setter method for property <tt>nameServer</tt>.
     *
     * @param nameServer value to be assigned to property nameServer
     */
    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }
}
