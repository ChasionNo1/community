package com.chasion.community;

import com.chasion.community.dao.DiscussPostMapper;
import com.chasion.community.dao.elasticsearch.DiscussPostRepository;
import com.chasion.community.entity.DiscussPost;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(101, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(102, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(103, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(111, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(112, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(131, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(132, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(133, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(134, 0, 100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(156, 0, 100));

    }

    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人，使劲灌水");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void testDelete(){
        discussPostRepository.deleteById(231);
        // 删除所有数据
//        discussPostRepository.deleteAll();
    }

    @Test
    public void testSearch(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 按照什么字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 设置分页
                .withPageable(PageRequest.of(0, 10))
                // 设置高亮
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 底层获取到了高亮显示的值，但没有返回
        Page<DiscussPost> page = discussPostRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        for (DiscussPost post :
                page) {
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate(){
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                // 按照什么字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 设置分页
                .withPageable(PageRequest.of(0, 10))
                // 设置高亮
                .withHighlightFields(new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        // 7.6版本 elasticsearch template过时了，改为elasticsearch rest template

    }
}
