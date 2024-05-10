package com.chasion.community.controller;

import com.chasion.community.entity.User;
import com.chasion.community.service.LikeService;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;


@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId){
        User user = hostHolder.getUser();

        likeService.like(user.getId(), entityType, entityId, entityUserId);
        long entityLikeCount = likeService.getEntityLikeCount(entityType, entityId);
        int entityLikeStatus = likeService.getEntityLikeStatus(user.getId(), entityType, entityId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("entityLikeCount", entityLikeCount);
        System.out.println(entityLikeCount);
        System.out.println(entityLikeStatus);
        map.put("entityLikeStatus", entityLikeStatus);
        return CommunityUtil.getJSONString(0, null, map);

    }
}
