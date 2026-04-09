package com.thepowermisha.service;

import com.thepowermisha.repository.MessageRepository;
import noNamespace.MessageDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageService {
    private static final int ACCEPTED_CODE = 0;
    private static final int REJECTED_CODE = 1;
    private static final String SUCCESS_REASON = "success";
    private static final String INAPPROPRIATE_REASON = "used inappropriate language";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FilterService filterService;
    private final MessageRepository messageRepository;

    public MessageService(FilterService filterService,
                          MessageRepository messageRepository) {
        this.filterService = filterService;
        this.messageRepository = messageRepository;
    }

    public String process(String requestXml) throws XmlException {
        MessageDocument requestDocument = MessageDocument.Factory.parse(requestXml);
        validateRequest(requestDocument);

        MessageDocument.Message message = requestDocument.getMessage();
        String time = message.getHeader().getTime();
        String user = message.getRequest().getUser();
        String text = message.getRequest().getText();

        int resultCode;
        String resultReason;

        if (filterService.containsForbiddenWords(text)) {
            resultCode = REJECTED_CODE;
            resultReason = INAPPROPRIATE_REASON;
        } else {
            resultCode = ACCEPTED_CODE;
            resultReason = SUCCESS_REASON;
        }

        messageRepository.save(time, user, text, resultCode);

        return buildResponseXml(resultCode, resultReason);
    }

    public String buildErrorResponse(String message) {
        return buildResponseXml(REJECTED_CODE, message);
    }

    private void validateRequest(MessageDocument document) {
        if (document == null || document.getMessage() == null) {
            throw new IllegalArgumentException("Missing message element");
        }

        MessageDocument.Message message = document.getMessage();

        if (message.getHeader() == null) {
            throw new IllegalArgumentException("Missing header element");
        }

        if (message.getRequest() == null) {
            throw new IllegalArgumentException("Missing request element");
        }

        if (message.getResponse() != null) {
            throw new IllegalArgumentException("Request must not contain response element");
        }

        if (message.getHeader().getTime() == null || message.getHeader().getTime().isBlank()) {
            throw new IllegalArgumentException("Missing time attribute");
        }

        if (message.getRequest().getUser() == null || message.getRequest().getUser().isBlank()) {
            throw new IllegalArgumentException("Missing user");
        }

        if (message.getRequest().getText() == null) {
            throw new IllegalArgumentException("Missing text");
        }
    }

    private String buildResponseXml(int code, String reason) {
        MessageDocument responseDocument = MessageDocument.Factory.newInstance();

        MessageDocument.Message message = responseDocument.addNewMessage();
        MessageDocument.Message.Header header = message.addNewHeader();
        header.setTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        MessageDocument.Message.Response response = message.addNewResponse();
        MessageDocument.Message.Response.Status status = response.addNewStatus();
        status.setCode(code);
        status.setReason(reason);

        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSavePrettyPrintIndent(4);

        return responseDocument.xmlText(xmlOptions);
    }
}