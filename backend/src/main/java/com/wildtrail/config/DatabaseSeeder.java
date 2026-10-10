package com.wildtrail.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder
implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    public DatabaseSeeder(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
    }

    public void run(ApplicationArguments args) {
        Integer count = (Integer)this.jdbc.queryForObject("SELECT COUNT(*) FROM destinations", Integer.class);
        if (count != null && count == 0) {
            new ResourceDatabasePopulator(new Resource[]{new ClassPathResource("catalog-seed.sql")}).execute(this.dataSource);
        }
    }
}
