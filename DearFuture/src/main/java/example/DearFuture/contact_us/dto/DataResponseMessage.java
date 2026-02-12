package example.DearFuture.contact_us.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataResponseMessage<T> extends ResponseMessage {
    private List<T> data;

    public DataResponseMessage(String message, boolean success, List<T> data) {
        super(message, success);
        this.data = data;
    }
}
