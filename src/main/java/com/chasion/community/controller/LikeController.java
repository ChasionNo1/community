package com.chasion.community.controller;

import com.chasion.community.entity.Event;
import com.chasion.community.entity.User;
import com.chasion.community.event.EventProducer;
import com.chasion.community.service.LikeService;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import com.chasion.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;


@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType, entityId, entityUserId);
        long entityLikeCount = likeService.getEntityLikeCount(entityType, entityId);
        int entityLikeStatus = likeService.getEntityLikeStatus(user.getId(), entityType, entityId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("entityLikeCount", entityLikeCount);
        System.out.println(entityLikeCount);
        System.out.println(entityLikeStatus);
        map.put("entityLikeStatus", entityLikeStatus);
        // 触发点赞事件
        if (entityLikeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId)
                    ;
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        if (entityType == ENTITY_TYPE_POST){
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, postId);
        }
        return CommunityUtil.getJSONString(0, null, map);

    }
}
