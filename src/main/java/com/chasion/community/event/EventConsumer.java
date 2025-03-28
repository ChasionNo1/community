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
import com.chasion.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

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

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

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

        // 启动一个定时器，每隔一段时间查看图片是否生成
        // 在一个服务器上启动的任务，不涉及其他服务器
        UploadTask task = new UploadTask(fileName, suffix);
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }


    class UploadTask implements Runnable {

        // 文件名称
        private String fileName;

        // 文件后缀
        private String suffix;

        // 启动任务的返回值，可以停止定时器
        private Future future;

        // 开始时间   计算超时时间
        private long startTime;

        // 上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 任务完成停止
            // 生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("执行时间过长，终止任务：" + fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3){
                logger.error("上传次数过多，终止任务：" + fileName);
                return;
            }

            // 本地路径
            String path = storage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()){
                logger.info(String.format("开始第%d次上传[%S].", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone0()));
                try {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false
                    );
                    // 处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if (jsonObject == null || jsonObject.get("code") == null || !jsonObject.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            }else {
                logger.info("等待图片生成[" + fileName + "].");
            }

        }
    }


}
