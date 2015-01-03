package com.zhiyi.im.metaq;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.zhiyi.im.config.InstantChatConfig;

public class MsgSender {
    protected static final Logger logger = Logger.getLogger(MsgSender.class);
    
    private static final String PRODUCER_GROUP = "zhiyi_im";
    
    private DefaultMQProducer producer;
    
    private String nameServer;
    
    private boolean test = false;

	private MsgSender() {}
	
	private static class MsgSenderHolder {
		public static final MsgSender instance= new MsgSender();
	}
	
	public static MsgSender getInstance() {
		return MsgSenderHolder.instance;
	}
    
    public void setNameServer(String nameServer) {
        this.nameServer = nameServer;
    }
    
    public void init() {
        if ("localTest".equals(nameServer)) {
            test = true;
            return;
        }
        
        producer = new DefaultMQProducer(PRODUCER_GROUP);
        try {
        	// Indentify different producers by instance name.
            producer.setInstanceName(
            		"PRODUCER-"+ InetAddress.getLocalHost().getHostName() + "-"
            		+ InstantChatConfig.getInstance().getServerPort() );
        } catch (UnknownHostException e) {
            logger.error("getHostName error",e);
        }
        
        try {
        	producer.setNamesrvAddr(nameServer);
            producer.start();
            logger.info("Producer start success! rocketmq.namesrv.domain=" + nameServer);
            
            // TODO: The following code is just for debugging. Remove it later.
//            Message msg = new Message(
//            		"TopicTest1",// topic
//                    "TagA",// tag
//                    "OrderID001",// key
//                    ("Hello MetaQ").getBytes());// body
//                SendResult sendResult = producer.send(msg);
//                System.out.println(sendResult);
                
        } catch (MQClientException e) {
            logger.error(MessageFormat.format("meta start failed!nameServer={0},group={1},excepton={2} ",
                    nameServer, "P_fundselling", e.getMessage()), e);
        }
    }

    public SendResult sendMessage(Serializable message, String topic, String tag) {
        if (test) {
            SendResult sendResult = new SendResult();
            sendResult.setSendStatus(SendStatus.SEND_OK);
            return sendResult;
        }
        
        try {
            logger.info("send mq mesage, message=" + message.toString());
            Message msg = new Message(topic, tag, HessianUtils.encode(message));
            SendResult sendResult = producer.send(msg);
            logger.info("sendResult=" + sendResult + "message=" + message.toString());
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
            logger.info("metaq closed successfully;rocketmq.namesrv.domain=" + nameServer);
        }
    }

}
