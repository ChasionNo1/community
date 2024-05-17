package com.chasion.community.controller;

import com.chasion.community.entity.Event;
import com.chasion.community.entity.Page;
import com.chasion.community.entity.User;
import com.chasion.community.event.EventProducer;
import com.chasion.community.service.FollowService;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant{

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        // 关注
        followService.follow(user.getId(), entityType, entityId);
        // 触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @RequestMapping(path = "/followee/list/{userId}", method = RequestMethod.GET)
    public String followList(@PathVariable int userId, Model model, Page page) {
        // 设置分页信息
        page.setRows((int) followService.getFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        page.setPath("/follow/list/" + userId);
        page.setLimit(10);
        // 设置volist
        List<Map<String, Object>> followeeList = followService.getFolloweeList(userId, CommunityConstant.ENTITY_TYPE_USER, page.getOffset(), page.getLimit());
        model.addAttribute("followeeList", followeeList);
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        //
        return "/site/followee";

    }

    @RequestMapping(path = "/follower/list/{userId}", method = RequestMethod.GET)
    public String followerList(@PathVariable int userId, Model model, Page page) {
        // 设置分页信息
        page.setRows((int) followService.getFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId));
        page.setPath("/follower/list/" + userId);
        page.setLimit(10);
        // 设置volist
        List<Map<String, Object>> followerList = followService.getFollowerList(CommunityConstant.ENTITY_TYPE_USER, userId, page.getOffset(), page.getLimit());
        model.addAttribute("followerList", followerList);
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);
        return "/site/follower";
    }

}


