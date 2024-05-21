package com.chasion.community.service;

import com.chasion.community.dao.elasticsearch.DiscussPostRepository;
import com.chasion.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }

    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                // 按照什么字段排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 设置分页
                .withPageable(PageRequest.of(current, limit))
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
//                String createTime = searchHit.getContent().getCreateTime().toString();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//                Date date = null;
//                try {
//                    date = (Date) sdf.parse(createTime);
//                }catch (ParseException e){
//                    throw new RuntimeException(e);
//                }
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
        return list;
    }


}
