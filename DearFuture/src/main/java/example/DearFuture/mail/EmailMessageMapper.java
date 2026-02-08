package example.DearFuture.mail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EmailMessageMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(EmailMessage emailMessage) {
        try {
            return objectMapper.writeValueAsString(emailMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting EmailMessage to JSON", e);
        }
    }

    public static EmailMessage fromJson(String json) {
        try {
            return objectMapper.readValue(json, EmailMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting JSON to EmailMessage", e);
        }
    }
}
