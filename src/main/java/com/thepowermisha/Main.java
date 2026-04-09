package com.thepowermisha;

import com.thepowermisha.config.Config;
import com.thepowermisha.repository.MessageRepository;
import com.thepowermisha.service.DatabaseService;
import com.thepowermisha.service.FilterService;
import com.thepowermisha.service.MessageService;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Config config = new Config("AppConfig.properties");

        int port = config.getInt("server.port");
        int threadPoolSize = config.getInt("server.threadPoolSize");

        String dbUrl = config.getString("db.url");
        String dbUsername = config.getString("db.username");
        String dbPassword = config.getString("db.password");

        String forbiddenWordsFile = config.getString("forbidden.words.file");

        DatabaseService databaseService = new DatabaseService(dbUrl, dbUsername, dbPassword);
        MessageRepository messageRepository = new MessageRepository(databaseService);
        FilterService forbiddenWordsService = new FilterService(forbiddenWordsFile);
        MessageService messageService = new MessageService(forbiddenWordsService, messageRepository);

        try {
            Server server = new Server(port, messageService, threadPoolSize);
            server.start();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Can't Start Server", e);
        }
    }
}
