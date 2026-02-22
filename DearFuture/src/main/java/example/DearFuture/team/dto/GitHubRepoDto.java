package example.DearFuture.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepoDto {
    private String name;
    private String description;
    private String htmlUrl;
    private String readmeContent; // decoded markdown or plain text, truncated if long
}
