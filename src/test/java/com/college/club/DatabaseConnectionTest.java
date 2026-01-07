package com.college.club;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest // 加载Spring Boot上下文
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource; // 自动注入数据库连接池

    @Test
    public void testConnection() throws Exception {
        // 尝试获取数据库连接
        Connection connection = dataSource.getConnection();
        // 若能成功获取连接，说明连接正常
        System.out.println("数据库连接成功！连接信息：" + connection);
        connection.close();
    }
}