package com.chasion.community.dao;

import com.chasion.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {

    // 查询私信列表带分页
    List<Message> getConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);
    // 查询私信总数
    int getConversationCount(int userId);
    // 查询某个会话所包含的私信列表
    List<Message> getLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);
    // 查询某个会话所包含的私信数量
    int getLetterCount(String conversationId);
    // 查询未读信息数
    int getUnreadLetterCount(@Param("userId") int userId, @Param("conversationId") String conversationId);
}
