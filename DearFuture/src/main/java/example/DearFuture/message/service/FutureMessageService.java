package example.DearFuture.message.service;

import example.DearFuture.message.dto.request.CreateMessageRequest;
import example.DearFuture.message.dto.request.CreateFutureMessageRequest;
import example.DearFuture.message.dto.response.MessageResponse;
import example.DearFuture.user.dto.response.MessageQuotaResponse;

import java.util.List;

import example.DearFuture.message.dto.request.UpdateMessageRequest;
import example.DearFuture.message.dto.response.MessageUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FutureMessageService {
    MessageResponse createMessage(CreateMessageRequest request);
    List<MessageResponse> getPendingMessages();
    List<MessageResponse> getDeliveredMessages();
    
    MessageResponse getMessage(Long id);
    MessageResponse updateMessage(Long id, UpdateMessageRequest request);
    String deleteMessage(Long id);

    ResponseEntity scheduleMessage(CreateFutureMessageRequest request);

    /**
     * Mesaj eki yükler. Plana göre tip (IMAGE/VIDEO/FILE/AUDIO), boyut ve sayı limitleri dinamik kontrol edilir.
     * @param file Yüklenecek dosya
     * @param type "IMAGE", "VIDEO", "FILE" veya "AUDIO"
     */
    MessageUploadResponse uploadAttachment(MultipartFile file, String type);

    /**
     * Kullanıcının planına göre mesaj kotası: limit, kullanılan ve kalan hak (long).
     * FREE: toplam SCHEDULED+QUEUED+SENT; PLUS/PREMIUM: dönem içi scheduledAt sayımı.
     */
    MessageQuotaResponse getMessageQuota(Long userId);
}
