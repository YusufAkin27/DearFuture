package example.DearFuture.message.mapper;

import example.DearFuture.message.dto.request.CreateFutureMessageRequest;
import example.DearFuture.message.dto.request.MessageContentRequest;
import example.DearFuture.message.entity.ContentType;
import example.DearFuture.message.entity.FutureMessage;
import example.DearFuture.message.entity.FutureMessageContent;
import example.DearFuture.message.entity.MessageStatus;

import java.util.ArrayList;
import java.util.List;

public class FutureMessageMapper {

    public static FutureMessage toEntity(CreateFutureMessageRequest request) {
        FutureMessage message = new FutureMessage();
        message.setStatus(MessageStatus.SCHEDULED);
        message.setScheduledAt(request.getScheduledAt());
        message.setSentAt(null);
        message.setRecipientEmails(request.getRecipientEmails());

        List<FutureMessageContent> contents =
                mapContents(request.getContents(), message);

        message.setContents(contents);
        return message;
    }

    private static List<FutureMessageContent> mapContents(
            List<MessageContentRequest> requests,
            FutureMessage message
    ) {
        List<FutureMessageContent> contents = new ArrayList<>();

        for (MessageContentRequest req : requests) {
            FutureMessageContent content = new FutureMessageContent();
            content.setFutureMessage(message);
            content.setType(req.getType());

            if (req.getType() == ContentType.TEXT) {
                content.setTextContent(req.getText());
            } else {
                content.setFileUrl(req.getFileUrl());
                content.setFileName(req.getFileName());
                content.setFileSize(req.getFileSize());
            }

            contents.add(content);
        }
        return contents;
    }
}
