package example.DearFuture.team.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubReadmeApiResponse {
    private String content; // base64 encoded
    private String encoding;
}
