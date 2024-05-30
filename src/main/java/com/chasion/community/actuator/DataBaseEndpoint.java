package com.chasion.community.actuator;

import com.chasion.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Endpoint(id = "database")
public class DataBaseEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(DataBaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkDatabase() {
        try (
                Connection connection = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "连接成功");
        } catch (SQLException e) {
            logger.error("连接失败:" + e.getMessage());
            return CommunityUtil.getJSONString(1, "连接失败");
        }
    }

}
