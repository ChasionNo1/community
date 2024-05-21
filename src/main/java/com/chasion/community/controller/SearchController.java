package com.chasion.community.controller;

import com.chasion.community.entity.DiscussPost;
import com.chasion.community.entity.Page;
import com.chasion.community.service.ElasticsearchService;
import com.chasion.community.service.LikeService;
import com.chasion.community.service.UserService;
import com.chasion.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;


    @RequestMapping(path = "/search", method = RequestMethod.GET)
    public String search(Model model, String keyword, Page page) {

        // 搜索帖子
        List<DiscussPost> discussPosts = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
        // 聚合数据
        List<Map<String, Object>> postVO = new ArrayList<>();
        if (discussPosts != null){
            for (DiscussPost post : discussPosts) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                // 点赞数量
                map.put("likeCount", likeService.getEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST, post.getId()));
                postVO.add(map);
            }
        }
        model.addAttribute("discussPosts", postVO);
        model.addAttribute("keyword", keyword);

        // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPosts == null ? 0 : discussPosts.size());
        page.setLimit(5);

        return "/site/search";
    }
}
