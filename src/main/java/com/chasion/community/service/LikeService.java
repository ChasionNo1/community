package com.chasion.community.service;

import com.chasion.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    // 集合存储的
    public void like(int userId, int entityType, int entityId, int entityUserId){
//        String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
//        // 判断用户是否点过赞
//        Boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember){
//            // 点过赞，再点一次取消
//            redisTemplate.delete(entityLikeKey);
//        }else {
//            // 未点过赞，加入集合
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
                // 帖子作者的id
                String userLikeKey = RedisKeyUtil.getPrefixUserLike(entityUserId);
                // 放在事务之外
                boolean isMember = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi();
                if(isMember){
                    // 点过赞，再点一次取消，帖子的赞数量减一
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else {
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec();
            }
        });

    }

    // 查询某个用户获得赞的数量
    public int getUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getPrefixUserLike(userId);
        // 获得赞的数量，没有列表
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }

    // 查询某实体点赞的数量
    public long getEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int getEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getPrefixEntityLike(entityType, entityId);
        System.out.println("----------key-----------");
        //        System.out.println(entityLikeKey);
//        System.out.println(userId);
//        System.out.println(res);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
