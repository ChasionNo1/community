package com.chasion.community.service;

import com.chasion.community.dao.MessageMapper;
import com.chasion.community.entity.Message;
import com.chasion.community.util.HostHolder;
import com.chasion.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private HostHolder hostHolder;

    public List<Message> getConversation(int userId, int offset, int limit){
        return messageMapper.getConversations(userId, offset, limit);
    }

    public int getConversationCount(int userId){
        return messageMapper.getConversationCount(userId);
    }

    public List<Message> getLetters(String conversationId, int offset, int limit){
        return messageMapper.getLetters(conversationId, offset, limit);
    }

    public int getLetterCount(String conversationId){
        return messageMapper.getLetterCount(conversationId);
    }

    public int getUnreadLetterCount(int userId, String conversationId){
        return messageMapper.getUnreadLetterCount(userId, conversationId);
    }

    public int addMessage(Message message){
        // 过滤敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Message> messages){
        // 获取未读letter的id
        ArrayList<Integer> ids = new ArrayList<>();
        if (messages != null && !messages.isEmpty()){
            for (Message message :
                    messages) {
                // 如果当前登录的用户是接收者，且消息未读
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
                    ids.add(message.getId());
                }
            }
        }
        if (!ids.isEmpty()){
            return messageMapper.updateStatus(ids, 1);
        }else {
            return 1;
        }
    }

//    public List<Integer> getUnreadList(List<Message> messages){
//        // 获取未读letter的id
//        ArrayList<Integer> ids = new ArrayList<>();
//        if (messages != null && !messages.isEmpty()){
//            for (Message message :
//                    messages) {
//                // 如果当前登录的用户是接收者，且消息未读
//                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0){
//                    ids.add(message.getId());
//                }
//            }
//        }
//        return ids;
//    }

    public Message getLastNotice(int userId, String topic){
        return messageMapper.getLatestNotice(userId, topic);
    }

    public int getNoticeCount(int userId, String topic){
        return messageMapper.getNoticeCount(userId, topic);
    }

    public int getUnreadNoticeCount(int userId, String topic){
        return messageMapper.getUnreadNoticeCount(userId, topic);
    }

    public List<Message> getNotices(int userId, String topic, int offset, int limit){
        return messageMapper.getNotices(userId, topic, offset, limit);
    }
}
