package com.chasion.community.service;

import com.chasion.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Service
public class DataService {

    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // 将指定的ip计入uv
    public void recordUV(String ip){
        String redisKey = RedisKeyUtil.getUVKey(sdf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(redisKey, ip);

    }

    // 统计指定日期内的uv
    public long calculateUV(Date start, Date end){
        if(start == null || end == null){
            throw new NullPointerException("start and end can't be null");
        }
        // 整理该日期范围内的key
        ArrayList<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            String uvKey = RedisKeyUtil.getUVKey(sdf.format(calendar.getTime()));
            keyList.add(uvKey);
            calendar.add(Calendar.DATE, 1);
        }
        // 合并这些数据
        String unionKey = RedisKeyUtil.getuvKey(sdf.format(start), sdf.format(end));
        redisTemplate.opsForHyperLogLog().add(unionKey, keyList);
        return redisTemplate.opsForHyperLogLog().size(unionKey);
    }

    // 将指定用户计入dau
    public void recordDau(int userId){
        String redisKey = RedisKeyUtil.getDauKey(sdf.format(new Date()));
        redisTemplate.opsForValue().setBit(redisKey, userId, true);
    }

    // 统计指定日期内的dau
    public long calculateDau(Date start, Date end){
        if(start == null || end == null){
            throw new NullPointerException("start and end can't be null");
        }
        ArrayList<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            byte[] key = RedisKeyUtil.getDauKey(sdf.format(calendar.getTime())).getBytes();
            keyList.add(key);
            calendar.add(Calendar.DATE, 1);
        }
        // 进行or运算
        return (long) redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String redisKey = RedisKeyUtil.getDauKey(sdf.format(start), sdf.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

    }

}
