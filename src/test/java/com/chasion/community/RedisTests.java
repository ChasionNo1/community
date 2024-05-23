package com.chasion.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testStrings() {
        String key = "test:count";
        redisTemplate.opsForValue().set(key, 1);
        System.out.println(redisTemplate.opsForValue().get(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
        System.out.println(redisTemplate.opsForValue().decrement(key));
    }

    @Test
    public void testHash() {
        String key = "test:user";
        redisTemplate.opsForHash().put(key, "name", "chasion");
        redisTemplate.opsForHash().put(key, "id", "101");
        System.out.println(redisTemplate.opsForHash().get(key, "name"));
        System.out.println(redisTemplate.opsForHash().get(key, "id"));
    }

    @Test
    public void testList() {
        String key = "test:ids";
        redisTemplate.opsForList().leftPush(key, 101);
        redisTemplate.opsForList().leftPush(key, 102);
        redisTemplate.opsForList().leftPush(key, 103);
        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().index(key, 0));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().range(key, 0, 2));
    }

    @Test
    public void testSet() {
        String key = "test:teachers";
        redisTemplate.opsForSet().add(key, "刘备", "关羽", "张飞", "诸葛亮", "曹操");
        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));
    }

    @Test
    public void testZSet() {
        String key = "test:students";
        redisTemplate.opsForZSet().add(key, "a", 100);
        redisTemplate.opsForZSet().add(key, "b", 50);
        redisTemplate.opsForZSet().add(key, "c", 60);
        redisTemplate.opsForZSet().add(key, "d", 70);
        redisTemplate.opsForZSet().add(key, "e", 80);
        System.out.println(redisTemplate.opsForZSet().size(key));
        System.out.println(redisTemplate.opsForZSet().score(key, "a"));
        System.out.println(redisTemplate.opsForZSet().rank(key, "c"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(key, "c"));
        System.out.println(redisTemplate.opsForZSet().range(key, 0, 2));
    }

    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:teachers", 10, TimeUnit.SECONDS);
        System.out.println(redisTemplate.keys("*"));
    }

    // 多次访问一个key
    @Test
    public void testBoundOperation(){
        String key = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(key);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    // 编程式事务
    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = "test:tx";
                // 启用事务，将操作放在一个队列里
                redisOperations.multi();
                redisOperations.opsForSet().add(key, "a");
                redisOperations.opsForSet().add(key, "b");
                redisOperations.opsForSet().add(key, "c");
                // 在这个管道里不要使用查询语句，因为redis是等所有语句都提交了一起执行的
                System.out.println(redisOperations.opsForSet().members(key));
                // 提交事务
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }


    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String key = "test:hyperloglog";
        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(key, i);
        }
        for (int i = 1; i <= 100000; i++) {
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(key, r);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(key));
    }

    // 将3组数据合并，再合并后的重复数据的独立总数
    @Test
    public void testHyperLogLog2(){
        String key = "test:hyperloglog2:01";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(key, i);
        }
        String key2 = "test:hyperloglog2:02";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(key2, i);
        }
        String key3 = "test:hyperloglog2:03";
        for (int i = 10001; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(key3, i);
        }

        String unionKey = "test:hyperloglog2:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, key, key2, key3);

        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 3, true);
        redisTemplate.opsForValue().setBit(redisKey, 6, true);
        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        // 统计
        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        }));
    }

    // 统计3组数据的布尔值，并对这3组数据做or运算
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
        redisTemplate.opsForValue().setBit(redisKey4, 6, true);

        String redisKey = "test:bm:or";
        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return redisConnection.bitCount(redisKey.getBytes());
            }
        }));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }
}
