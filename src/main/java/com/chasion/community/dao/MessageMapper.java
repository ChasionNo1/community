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

    // 增加私信
    int insertMessage(Message message);
    // 更新消息的状态
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    // 查询某个主题下最新的通知
    Message getLatestNotice(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题包含的通知数量
    int  getNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询未读的通知数量
    int getUnreadNoticeCount(@Param("userId") int userId, @Param("topic") String topic);

    // 查询某个主题所包含的通知列表
    List<Message> getNotices(@Param("userId") int userId, @Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

}
