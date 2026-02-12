package example.DearFuture.contact_us.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMessage {
    private String message;
    private boolean success;

    public static ResponseMessage success(String message) {
        return new ResponseMessage(message, true);
    }

    public static ResponseMessage error(String message) {
        return new ResponseMessage(message, false);
    }
}
