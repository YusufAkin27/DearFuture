package example.DearFuture.message.service;

import example.DearFuture.message.dto.request.CreateMessageRequest;
import example.DearFuture.message.dto.request.CreateFutureMessageRequest;
import example.DearFuture.message.dto.response.MessageResponse;
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
     * Mesaj eki (fotoğraf veya dosya) yükler. PLUS/PREMIUM plana göre boyut ve tip kontrolü yapılır.
     * @param file Yüklenecek dosya
     * @param type "IMAGE" veya "FILE"
     */
    MessageUploadResponse uploadAttachment(MultipartFile file, String type);
}
