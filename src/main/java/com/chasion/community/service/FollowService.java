package com.chasion.community.service;

import com.chasion.community.entity.User;
import com.chasion.community.util.CommunityConstant;
import com.chasion.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;


    // 关注
    public void follow(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {

                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                // 事务
                // A 关注了 B， A followee b ，  b follower  a
                redisOperations.multi();
                // A的关注列表添加B
                //                followee:userId:entityType  -> zset(entityId,now)
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                // follower:entityType:entityId -> zset(userId, now)
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }
    // 取关

    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();
                // A的关注列表添加B
//                followee:userId:entityType  -> zset(entityId,now)

                redisOperations.opsForZSet().remove(followeeKey, entityId);
                // follower:entityType:entityId -> zset(userId, now)
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    // 查询关注的实体数量
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    // 查询粉丝的实体数量
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    // 当前用户的是否关注了该实体
    public boolean isFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    // 查询某个用户关注的人，支持分页
    public List<Map<String, Object>> getFolloweeList(int userId, int entityType, int offset, int limit){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (ids == null || ids.size() <= 0) return null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Integer id : ids) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, id);
            map.put("followeeTime", new Date(score.longValue()));
            // userId是否关注了id
            boolean followed = isFollowed(userId, CommunityConstant.ENTITY_TYPE_USER, id);
            map.put("followed", followed);
            list.add(map);
        }
        return list;
    }

    // 查询某个用户的粉丝列表，支持分页
    public List<Map<String, Object>> getFollowerList(int entityType, int entityId, int offset, int limit){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (ids == null || ids.size() <= 0) return null;
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : ids) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followerTime", new Date(score.longValue()));
            // 关注情况
            boolean followed = isFollowed(entityId, CommunityConstant.ENTITY_TYPE_USER, id);
            map.put("followed", followed);
            list.add(map);
        }
        return list;

    }

}
