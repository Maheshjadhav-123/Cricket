package com.cricket.tournament.jdbc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PlayerBatchInsertDao {

    private final DataSource dataSource;

    @Autowired
    public PlayerBatchInsertDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void batchInsertPlayers(List<String> playerNames) {
        String sql = "INSERT INTO players (player_name) VALUES (?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            for (String name : playerNames) {
                ps.setString(1, name);
                ps.addBatch();
            }
            ps.executeBatch();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
