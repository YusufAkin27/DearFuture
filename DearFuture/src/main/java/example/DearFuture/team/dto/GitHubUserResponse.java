package example.DearFuture.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Backend'den frontend'e dönen GitHub kullanıcı + repo + readme özeti */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubUserResponse {
    private String login;
    private String name;
    private String bio;
    private String avatarUrl;
    private String profileUrl;
    private List<GitHubRepoDto> repos;
}
