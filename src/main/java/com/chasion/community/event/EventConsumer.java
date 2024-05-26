package com.chasion.community.event;

import com.alibaba.fastjson.JSONObject;
import com.chasion.community.dao.DiscussPostMapper;
import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.Event;
import com.chasion.community.entity.Message;
import com.chasion.community.service.DiscussPostService;
import com.chasion.community.service.ElasticsearchService;
import com.chasion.community.service.MessageService;
import com.chasion.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

//    @Value("${spring.kafka.consumer.group-id}")
//    private String groupId;

    @Value("${wk.image.storage}")
    private String storage;

    @Value("${wk.image.command}")
    private String command;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW}, groupId = "community-consumer-group")
    public void handleCommentMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("Received null record");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("Received format error");
            return;
        }
        // 构造message
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        HashMap<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        // 取原有的map中可能存在的数据
        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }


    // 消费发帖事件，将发布的帖子添加到es服务器中
    @KafkaListener(topics = {TOPIC_PUBLISH}, groupId = "community-consumer-group")
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("Received null record");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("Received format error");
            return;
        }

        // 查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        // 存到es服务器中
        if (post != null) {
            elasticsearchService.saveDiscussPost(post);
        }

    }

    // 消费删除帖子事件，将贴子从es服务器中删除
    @KafkaListener(topics = {TOPIC_DELETE}, groupId = "community-consumer-group")
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("Received null record");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("Received format error");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }


    @KafkaListener(topics = {TOPIC_SHARE}, groupId = "community-consumer-group")
    public void handleShareMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            logger.error("Received null record");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("Received format error");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        // 拼接路径
        String cmd = command + " --quality 75 " + htmlUrl + " " + storage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }
    }


}
