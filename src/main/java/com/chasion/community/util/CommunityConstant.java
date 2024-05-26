package com.chasion.community.util;

public interface CommunityConstant {

    int REGISTER_SUCCESS = 0;

    int REGISTER_REPEAT = 1;
    int REGISTER_FAILURE = 2;

    // 默认登录凭证的超时时间  (MS) 1min = 60s， 1h = 3600s = 3600 * 1000 ms
    int DEFAULT_EXPIRATION_TIME = 3600 * 12 * 1000;

    // 记住我时 (7天）
    int REMEMBER_EXPIRATION_TIME = 3600 * 24 * 1000 * 7;

    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;

    // 主题:评论、点赞、关注
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";
    int SYSTEM_USER_ID = 1;

    // 主题：发帖
    String TOPIC_PUBLISH = "publish";

    // 权限：普通用户
    String AUTHORITY_USER = "user";

    // 权限：管理员
    String AUTHORITY_ADMIN = "admin";

    // 权限：版主
    String AUTHORITY_MODERATOR = "moderator";

    // 主题：删除帖子
    String TOPIC_DELETE = "delete";

    // 主题：分享
    String TOPIC_SHARE = "share";
}
