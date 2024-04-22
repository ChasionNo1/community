package com.chasion.community.dao;

import com.chasion.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 分页查询
    List<DiscussPost> selectDiscussPost(@Param("userId")int userId, @Param("offset")int offset, @Param("limit")int limit);

    // 查询帖子总数
    // @Param注解用于给参数起别名
    // 如果只有一个参数，并且在<if>里使用，则必须加别名
    int selectDiscussPostRows(@Param("userId") int userId);

}
