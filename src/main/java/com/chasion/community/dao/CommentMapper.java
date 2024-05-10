package com.chasion.community.dao;


import com.chasion.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 显示评论
    // 查询帖子的所有评论，每个评论下还有若干回复
    List<Comment> selectCommentByEntity(@Param("entityType") int entityType, @Param("entityId")int entityId, @Param("offset")int offset, @Param("limit")int limit);

    // 查询数据的条数
    int selectCountByEntity(@Param("entityType")int entityType,  @Param("entityId")int entityId);

    // 增加评论
    int insertComment(Comment comment);

    // 查询某用户的评论帖子列表
    List<Comment> selectCommentByUserId(@Param("entityType")int entityType, @Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    // 查询某用户评论的帖子列表
    int selectCountByUserId(@Param("entityType")int entityType, @Param("userId") int userId);

}
