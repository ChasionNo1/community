package com.chasion.community;

import com.alibaba.fastjson.JSONObject;
import com.chasion.community.dao.DiscussPostMapper;
import com.chasion.community.dao.elasticsearch.DiscussPostRepository;
import com.chasion.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import org.springframework.test.context.ContextConfiguration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;


    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList(){
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(101, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(102, 0, 100, 0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(134, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPost(156, 0, 100,0));

    }

    @Test
    public void testUpdate(){
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(231);
        discussPost.setContent("我是新人，使劲灌水");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void testDelete(){
//        discussPostRepository.deleteById(231);
        // 删除所有数据
        discussPostRepository.deleteAll();
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

    /**
     * 方案一：使用elasticsearch rest template
     *
     * */
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
        // queryForPage()过时了，可以用search先查询到结果，再自行包装成对象，然后返回
        // 使用SearchHits存储搜索结果
        SearchHits<DiscussPost> searchHits = elasticsearchRestTemplate.search(searchQuery, DiscussPost.class);
        long totalHits = searchHits.getTotalHits();
        // 遍历搜索结果设置帖子的各个参数
        ArrayList<DiscussPost> list = new ArrayList<>();
        if (totalHits != 0){
            for (SearchHit<DiscussPost> searchHit : searchHits){
                DiscussPost post = new DiscussPost();

                post.setId(searchHit.getContent().getId());
                post.setTitle(searchHit.getContent().getTitle());
                post.setContent(searchHit.getContent().getContent());
                post.setUserId(searchHit.getContent().getUserId());
                post.setStatus(searchHit.getContent().getStatus());
                post.setType(searchHit.getContent().getType());
//                System.out.println("----------createTime----------");
//                System.out.println(searchHit.getContent().getCreateTime());
                // 时间不需要格式化，因为在set的时候还是Date格式的
                post.setCreateTime(searchHit.getContent().getCreateTime());
                post.setCommentCount(searchHit.getContent().getCommentCount());
                // 获得刚刚构建的高光区域，填到帖子的内容和标题上
                List<String> contentField = searchHit.getHighlightFields().get("content");
                if (contentField != null && !contentField.isEmpty()){
                    post.setContent(contentField.get(0));
                }
                List<String> titleField = searchHit.getHighlightFields().get("title");
                if (titleField != null && !titleField.isEmpty()){
                    post.setTitle(titleField.get(0));
                }
                list.add(post);
            }
        }

        for (DiscussPost post:list){
            System.out.println(post);
        }
    }

    /**
     * 这个应该是评论区提供的方案
     *
     * */
    @Test
    public void highlightQuery() throws Exception {
        //1.创建搜索请求 searchRequest
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        //2.配置高亮 HighlightBuilder
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //为哪些字段匹配到的内容设置高亮
        highlightBuilder.field("title");
        highlightBuilder.field("content");
        highlightBuilder.requireFieldMatch(false);
        //相当于把结果套了一点html标签  然后前端获取到数据就直接用
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        //3.构建搜索条件 searchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 指定从哪条开始查询 需要查出的总记录条数
                .from(0).size(10)
                //配置高亮
                .highlighter(highlightBuilder);
        //4.将搜索条件参数传入搜索请求
        searchRequest.source(searchSourceBuilder);
        //5.使用客户端发送请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<DiscussPost> list = new LinkedList<>();
        for (org.elasticsearch.search.SearchHit hit : searchResponse.getHits().getHits()) {
            // 处理高亮显示的结果
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                //title=<span style='color:red'>互联网</span>求职暖春计划...  }
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                //content=它是最时尚的<span style='color:red'>互联网</span>公司之一...  }
                discussPost.setContent(contentField.getFragments()[0].toString());

            }
            System.out.println(discussPost);
            list.add(discussPost);
        }

    }

    @Test
    public void testDiscussPosts(){
        // DiscussPost(id=287, userId=156, title=今天星期二, content=天比天高，命比纸薄！, type=0, status=0, createTime=Tue May 21 21:54:43 CST 2024, commentCount=0, score=0.0)
        DiscussPost discussPost = discussPostMapper.selectDiscussPostById(287);
        Date createTime = discussPost.getCreateTime();
        System.out.println(createTime);
        String time = discussPost.getCreateTime().toString();
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime);
        System.out.println(format);
//        System.out.println(discussPost);
    }
}
