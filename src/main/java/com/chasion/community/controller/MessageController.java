package com.chasion.community.controller;

import com.chasion.community.entity.Message;
import com.chasion.community.entity.Page;
import com.chasion.community.entity.User;
import com.chasion.community.service.MessageService;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityUtil;
import com.chasion.community.util.HostHolder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    // 私信列表
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        // 分页信息
        page.setLimit(5);
        User user = hostHolder.getUser();
        int conversationCount = messageService.getConversationCount(user.getId());
        page.setRows(conversationCount);
        page.setPath("/letter/list");
        // 会话列表
        List<Message> conversations = messageService.getConversation(user.getId(), page.getOffset(), page.getLimit());
        // 未读私信所有
        int unreadLetterCount = messageService.getUnreadLetterCount(user.getId(), null);
        List<Map<String, Object>> conversationVoList = new ArrayList<>();
        // 每个会话的未读消息何会话条数
        for (Message message :
                conversations) {
            HashMap<String, Object> map = new HashMap<>();
            // 获取会话的未读消息数
            int conversationUnreadLetterCount = messageService.getUnreadLetterCount(user.getId(), message.getConversationId());
            // 获取会话的条数
            int letterCount = messageService.getLetterCount(message.getConversationId());
            // 发送者的头像和名称等信息，如果是登录用户给朋友发送的私信，显示朋友头像，如果是朋友给登录用户发送的私信，显示朋友头像
            int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
            map.put("message", message);
            map.put("culc", conversationUnreadLetterCount);
            map.put("letterCount", letterCount);
            map.put("target", userService.findUserById(targetId));
            conversationVoList.add(map);
        }

        model.addAttribute("conversationVoList", conversationVoList);
        model.addAttribute("unreadLetterCount", unreadLetterCount);


        return "/site/letter";
    }

    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(Model model, Page page, @PathVariable("conversationId") String conversationId) {
        //设置分页信息
        page.setPath("/letter/detail/" + conversationId);
        page.setLimit(5);
        page.setRows(messageService.getLetterCount(conversationId));

        // 对话内容的封装
        String[] ids = conversationId.split("_");
        int targetId = hostHolder.getUser().getId() == Integer.parseInt(ids[0]) ? Integer.parseInt(ids[1]) : Integer.parseInt(ids[0]);
        model.addAttribute("target", userService.findUserById(targetId));
        List<Message> letters = messageService.getLetters(conversationId, page.getOffset(), page.getLimit());
        messageService.readMessage(letters);
        List<Map<String, Object>> letterVoList = new ArrayList<>();
        for (Message letter :
                letters) {
            HashMap<String, Object> map = new HashMap<>();
            // 放内容，放发送者的信息
            map.put("letter", letter);
            User user = userService.findUserById(letter.getFromId());
            map.put("user", user);
            letterVoList.add(map);
        }

        model.addAttribute("letters", letterVoList);
        return "/site/letter-detail";
    }

    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 对方目标
        User target = userService.getUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "用户不存在！");
        }
        // 构造消息
        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);
        String conversationId = message.getFromId() < message.getToId() ? message.getFromId() + "_" + message.getToId() : message.getToId() + "_" + message.getFromId();
        message.setConversationId(conversationId);
        message.setCreateTime(new Date());
        message.setStatus(0);
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

}
