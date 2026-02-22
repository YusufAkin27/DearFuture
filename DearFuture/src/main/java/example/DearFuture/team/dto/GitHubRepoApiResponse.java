package example.DearFuture.team.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubRepoApiResponse {
    private String name;
    private String description;
    @JsonProperty("html_url")
    private String htmlUrl;
}
