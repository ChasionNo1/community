package com.chasion.community.service;

import com.chasion.community.dao.MessageMapper;
import com.chasion.community.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

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
}
