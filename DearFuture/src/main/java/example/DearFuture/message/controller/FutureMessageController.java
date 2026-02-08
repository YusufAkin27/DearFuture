package example.DearFuture.message.controller;

import example.DearFuture.message.dto.request.CreateFutureMessageRequest;
import example.DearFuture.message.dto.request.CreateMessageRequest;
import example.DearFuture.message.dto.response.MessageResponse;
import example.DearFuture.message.dto.response.MessageUploadResponse;
import example.DearFuture.message.service.FutureMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class FutureMessageController {

    private final FutureMessageService futureMessageService;

    @PostMapping
    public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        return ResponseEntity.ok(futureMessageService.createMessage(request));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> getDeliveredMessages() {
        return ResponseEntity.ok(futureMessageService.getDeliveredMessages());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<MessageResponse>> getPendingMessages() {
        return ResponseEntity.ok(futureMessageService.getPendingMessages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable Long id) {
        return ResponseEntity.ok(futureMessageService.getMessage(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable Long id, @Valid @RequestBody example.DearFuture.message.dto.request.UpdateMessageRequest request) {
        return ResponseEntity.ok(futureMessageService.updateMessage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long id) {
       return ResponseEntity.ok(futureMessageService.deleteMessage(id)) ;
    }

    @PostMapping("/schedule")
    public ResponseEntity scheduleFutureMessage(@Valid @RequestBody CreateFutureMessageRequest request) {
        return futureMessageService.scheduleMessage(request);
    }

    @PostMapping("/upload")
    public ResponseEntity<MessageUploadResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        return ResponseEntity.ok(futureMessageService.uploadAttachment(file, type));
    }
}
