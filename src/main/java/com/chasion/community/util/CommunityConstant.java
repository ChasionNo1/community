package com.chasion.community.util;

public interface CommunityConstant {

    int REGISTER_SUCCESS = 0;

    int REGISTER_REPEAT = 1;
    int REGISTER_FAILURE = 2;

    // 默认登录凭证的超时时间  (MS) 1min = 60s， 1h = 3600s = 3600 * 1000 ms
    int DEFAULT_EXPIRATION_TIME = 3600 * 12 * 1000;

    // 记住我时 (7天）
    int REMEMBER_EXPIRATION_TIME = 3600 * 24 * 1000 * 7;

}
