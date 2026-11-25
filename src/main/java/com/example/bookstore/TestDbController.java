package com.example.bookstore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

@RestController
public class TestDbController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test-db")
    public String testDb() {
        try (Connection conn = dataSource.getConnection()) {
            return "✅ Kết nối Database THÀNH CÔNG!<br>" +
                    "URL: " + conn.getMetaData().getURL() + "<br>" +
                    "User: " + conn.getMetaData().getUserName() + "<br>" +
                    "Mk:  " + conn.getMetaData().getDriverName() + "<br>" +
                    "Database: " + conn.getCatalog();
        } catch (Exception e) {
            return "❌ Kết nối Database THẤT BẠI!<br>Lỗi: " + e.getMessage();
        }
    }
}