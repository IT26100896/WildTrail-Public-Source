package com.wildtrail.repository;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

/** Portable generated-key inserts for PostgreSQL and the local H2 SQL database. */
public final class JdbcInsert {
    private JdbcInsert() {}

    public static int id(JdbcTemplate jdbc, String sql, Object... values) {
        GeneratedKeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql.replace(" RETURNING id", ""), new String[]{"id"});
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        }, keys);
        return keys.getKey().intValue();
    }
}
