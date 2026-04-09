package com.thepowermisha;

import com.thepowermisha.service.MessageService;

import java.io.*;
import org.apache.xmlbeans.XmlException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final MessageService messageService;

    public ClientHandler(Socket socket, MessageService messageService) {
        this.socket = socket;
        this.messageService = messageService;
    }

    @Override
    public void run() {
        String clientId = getClientId(socket);
        logger.info("[" + clientId + "] Connected");

        try (
                Socket clientSocket = socket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8)),
                        true)
        ) {

            String requestXml;
            while ((requestXml = reader.readLine()) != null) {
                logger.info("[" + clientId + "] Received request : " + requestXml);

                String responseXml;
                try {
                    responseXml = messageService.process(requestXml);
                }catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "[" + clientId + "] Invalid XML request ", e);
                    responseXml = messageService.buildErrorResponse(e.getMessage());
                }
                catch (XmlException e){
                    logger.log(Level.WARNING, "[" + clientId + "] Failed to process XML ", e);
                    responseXml = messageService.buildErrorResponse("Failed to process XML");
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, "[" + clientId + "] Failed to process request: ", e);
                    responseXml = messageService.buildErrorResponse("Failed to process message");
                }

                writer.println(responseXml);
                logger.info("[" + clientId + "] Sent response: " + responseXml);
            }

            logger.info("[" + clientId + "] Client disconnected: ");
        } catch (IOException e) {
            logger.log(Level.SEVERE,"[" + clientId + "] I/O error while handling client: ", e);

        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "[" + clientId + "] Unexpected error in ClientHandler: " , e);
        }
    }

    private String getClientId(Socket socket) {
        return socket.getRemoteSocketAddress().toString();
    }
}