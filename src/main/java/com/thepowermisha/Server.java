package com.thepowermisha;

import com.thepowermisha.service.MessageService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ServerSocket serverSocket;
    private final ExecutorService executor;
    private final MessageService messageService;

    public Server(int port, MessageService messageService, int threadPoolSize) throws IOException {
        serverSocket = new ServerSocket(port);
        this.messageService = messageService;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);

        logger.info("Server started on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    public void start() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("New client connected: " + socket.getRemoteSocketAddress());

                executor.submit(new ClientHandler(socket, messageService));

            } catch (SocketException e) {
                if (!running.get()) {
                    logger.info("Server stopped accepting connections");
                } else {
                    logger.log(Level.SEVERE, "Socket error while accepting connection", e);
                }
            } catch (RejectedExecutionException e) {
                logger.log(Level.WARNING, "Task rejected by executor", e);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "I/O error in server loop", e);
            }
        }
    }


    public void stop() {
        logger.info("Shutting down server...");

        running.set(false);

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while closing server socket", e);
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warning("Forcing shutdown of active tasks");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Shutdown interrupted", e);
            executor.shutdownNow();
        }

        logger.info("Server stopped");
    }
}
