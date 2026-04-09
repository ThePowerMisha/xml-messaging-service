package com.thepowermisha.repository;

import com.thepowermisha.service.DatabaseService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageRepository {
    private static final Logger logger = Logger.getLogger(MessageRepository.class.getName());

    private static final String INSERT_SQL = """
            INSERT INTO messages (time, username, text, result)
            VALUES (?, ?, ?, ?)
            """;

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DatabaseService databaseService;

    public MessageRepository(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    public synchronized void save(String time, String user, String text, int result) {

        try (Connection connection = databaseService.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            LocalDateTime parsedTime = LocalDateTime.parse(time, DATE_TIME_FORMATTER);

            statement.setTimestamp(1, Timestamp.valueOf(parsedTime));
            statement.setString(2, user);
            statement.setString(3, text);
            statement.setInt(4, result);

            statement.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save message to database", e);
            throw new RuntimeException("Database insert failed", e);
        }

    }
}
