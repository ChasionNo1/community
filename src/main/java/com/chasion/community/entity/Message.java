package com.chasion.community.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {
    // 私信属性
    private int id;
    // 私信的发送者的id
    private int fromId;
    // 私信的接收者id
    private int toId;
    // 对话的id 111_112，id的唯一性，小的id在前面，大的id在后面
    private String conversationId;
    private String content;
    // 状态，0是正常，1是不正常
    private int status;
    private Date createTime;
}