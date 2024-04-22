package com.chasion.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaMybatisImp implements AlphaDao{
    @Override
    public String select() {
        return "Mybatis";
    }
}
