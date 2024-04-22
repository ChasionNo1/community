package com.chasion.community.entity;

import lombok.Data;

@Data
public class DiscussPost {
    private int id;
    private int userId;
    private String title;
    private String content;
    private int type;
    private int status;
    private Data createTime;
    private int commentCount;
    private double score;

}
