package com.chasion.community.dao;

import org.springframework.stereotype.Repository;

@Repository("alphaDaoHibernateImp")
public class AlphaDaoHibernateImp implements AlphaDao{
    @Override
    public String select() {
        return "Hibernate";
    }
}
