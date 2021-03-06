package com.zhiyi.im.metaq;

import org.apache.commons.lang3.StringUtils;

public enum TopicEnum {
    TEST("TEST", "TEST", "测试应用"),
    NOTIFYASYNCQUEUE("NotifyAsyncQueue", "DEFAULT_TAGS", "异步处理消息发送队列");

    private String topic;

    private String tags;

    private String desc;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    private TopicEnum(String topic, String tags, String desc) {
        this.topic = topic;
        this.tags = tags;
        this.desc = desc;
    }

    public String[] getTopicAndTag() {
        return new String[]{topic, tags, desc};
    }

    public static TopicEnum findTopicEnum(String topic, String tags) {
        for (TopicEnum topicEnum : TopicEnum.class.getEnumConstants()) {
            if (StringUtils.equals(topicEnum.getTopic(), topic)
                    && StringUtils.equals(topicEnum.getTags(), tags)) {
                return topicEnum;
            }
        }
        return null;
    }

    public String getTopicStr() {
        return topic + "," + tags;
    }

    public static TopicEnum getTopicEnumFromStr(String desc) {
        String[] tags = desc.split(",");
        return findTopicEnum(tags[0], tags[1]);
    }

}
