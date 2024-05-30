package com.chasion.community;

import com.chasion.community.entity.DiscussPost;
import com.chasion.community.service.DataService;
import com.chasion.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringbootTests {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private DataService dataService;

    private DiscussPost post;

    @BeforeClass
    public static void beforeClass(){
        System.out.println("Before Class");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("After Class");
    }

    @Before
    public void before(){
        // 初始化数据
        System.out.println("Before");
        post = new DiscussPost();
        post.setTitle("Discuss Post");
        post.setContent("This is a post");
        post.setId(111);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

    }

    @After
    public void after(){
        // 删除数据
        System.out.println("After");
        discussPostService.updateStatus(post.getId(), 2);
    }

    @Test
    public void test1(){
        System.out.println("test1");
    }

    @Test
    public void test2(){
        System.out.println("test2");
    }

    @Test
    public void testFindById(){
        DiscussPost post1 = discussPostService.findDiscussPostById(post.getId());
        Assert.assertNotNull(post1);
        Assert.assertEquals(post.getTitle(), post1.getTitle());
        Assert.assertEquals(post.getContent(), post1.getContent());

    }

    @Test
    public void testUpdateScore(){
        int row = discussPostService.updateDiscussPostScore(post.getId(), 2000.00);
        Assert.assertNotNull(row);
        Assert.assertEquals(1, row);
        DiscussPost post1 = discussPostService.findDiscussPostById(post.getId());
        Assert.assertEquals(2000.00, post1.getScore(), 2);


    }
}
