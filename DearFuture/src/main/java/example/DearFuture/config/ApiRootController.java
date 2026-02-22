package example.DearFuture.config;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET / — Sadece api.dearfuture.info için kısa metin. Web arayüzü ayrı sunulur (dearfuture.com.tr).
 */
@RestController
public class ApiRootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Dear Future API. Web: https://dearfuture.com.tr");
    }
}
