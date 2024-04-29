package com.chasion.community.util;

import jdk.nashorn.internal.ir.CallNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String replace = "**";

    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init(){
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
        ) {
            assert is != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ){
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 添加到前缀树
                        this.addKeyword(line);
                    }
            }
        } catch (IOException e){
            logger.error("Error reading file:" + e.getMessage());
        }
    }

    // 将一个敏感词添加到前缀树
    public void addKeyword(String keyword){
         TrieNode current = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            // 插入到前缀树中
            TrieNode child = current.getChild(c);
            if (child == null) {
                // 没有这个节点
                // 加入
                child = new TrieNode();
                current.addChildren(c, child);
            }
            current = child;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                current.setEnd(true);
            }

        }
    }

    // 过滤敏感词
    public String filter(String text){
        if (StringUtils.isBlank(text)){
            return text;
        }
        // 声明三个指针
        TrieNode current = root;
        int start = 0;
        int pos = 0;
        StringBuilder result = new StringBuilder();
        while (start < text.length()) {
            if (pos < text.length()){
                char c = text.charAt(pos);
//                System.out.println("c:" + c);
                // 跳过符号
                // 对于处于起始位置的符号字符添加到结果串中，中间位置的特殊符号可能存在分割敏感词的情况，就不添加了
                if (isSymbol(c)){
                    if (current == root){
                        result.append(text.charAt(start));
                        start++;
                    }
                    // 遇到敏感词，pos要跳过
                    pos++;
                    continue;
                }
                // 此时不是符号
                current = current.getChild(c);
                if (current == null){
                    // 以start开头到pos没有敏感词，即把start添加到result中，start往后移动一位
                    result.append(text.charAt(start));
                    start++;
                    // pos位置重置
                    pos = start;
                    // current指针重置
                    current = root;
                }else if (current.isEnd()){
                    // current 不为null，说明从start开始到pos是敏感词，且此时到达叶子节点，是完整的敏感词
                    // 进行替换
                    result.append(replace);
                    // 此时pos的位置是结尾
                    // 从pos的下一个位置开始遍历，重置指针位置
                    pos++;
                    start = pos;
                    current = root;
                }else {
                    // 此时是敏感词的中间位置，不为空，也没到叶子节点，继续遍历
                    pos++;
                }
            }else {
                // 此时pos==text.length()，而start还在前面，还需要对start到pos之间的串进行过滤
                // 以start开头到pos结尾的敏感词已经替换了
                // start+1到pos结尾的串还没处理
                // 指针重置
                result.append(text.charAt(start));
                start++;
                pos = start;
                current = root;
            }


        }
        return result.toString();

    }
    // 判断是否是特殊符号
    private boolean isSymbol(char c){
        // 字符合法性判断，跳过符号，0x2E80 0x9FFF是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


}

@Data
class TrieNode{
    // 使用map来存放子节点
    private HashMap<Character, TrieNode> children = new HashMap<>();
    // 叶子节点标志位
    private boolean isEnd = false;

    public boolean isEnd(){
        return isEnd;
    }

    // 获取子节点
    public TrieNode getChild(char c){
        return children.get(c);
    }

    // 添加子节点
    public void addChildren(char c, TrieNode child){
        children.put(c, child);
    }
}
