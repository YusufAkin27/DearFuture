package example.DearFuture.team;

import example.DearFuture.team.dto.GitHubUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/team")
@RequiredArgsConstructor
public class TeamController {

    private final GitHubApiService githubApiService;

    /**
     * GitHub kullanıcı bilgisi, repolar ve README içeriklerini döner.
     * Sadece app.github.allowed-usernames içindeki kullanıcılar için çalışır (token güvenliği).
     */
    @GetMapping("/github/{username}")
    public ResponseEntity<GitHubUserResponse> getGitHubUser(@PathVariable String username) {
        GitHubUserResponse body = githubApiService.fetchUserWithRepos(username);
        return body != null ? ResponseEntity.ok(body) : ResponseEntity.notFound().build();
    }
}
