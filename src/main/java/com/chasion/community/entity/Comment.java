package com.chasion.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Comment {
    // 评论或者是回复的id
    private int id;
    // 发布人的用户id
    private int userId;
    // comment的类型，1是评论，2是回复
    private int entityType;
    // 目标的id，评论或者是回复的目标的id
    private int targetId;
    // 帖子id
    private int entityId;
    private String content;
    // 0正常，1删除
    private int status;
    private Date createTime;

}
