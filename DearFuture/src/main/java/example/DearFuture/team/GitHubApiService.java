package example.DearFuture.team;

import example.DearFuture.team.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubApiService {

    private static final String GITHUB_API = "https://api.github.com";
    private static final int MAX_README_LENGTH = 2000;
    private static final int MAX_REPOS = 10;

    private final RestTemplate restTemplate;

    @Value("${app.github.token.melisacicek:}")
    private String tokenMelisa;

    @Value("${app.github.token.yusufakin27:}")
    private String tokenYusuf;

    @Value("${app.github.token.default:}")
    private String tokenDefault;

    @Value("${app.github.allowed-usernames:melisacicek,yusufakin27}")
    private String allowedUsernamesStr;

    private Set<String> getAllowedUsernames() {
        if (allowedUsernamesStr == null || allowedUsernamesStr.isBlank()) return Set.of("melisacicek", "yusufakin27");
        return Arrays.stream(allowedUsernamesStr.split(",")).map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
    }

    /** İstek yapılacak kullanıcıya göre token seçer (Melisa / Yusuf / default). */
    private String getTokenForUser(String normalizedUsername) {
        if ("melisacicek".equals(normalizedUsername) && tokenMelisa != null && !tokenMelisa.isBlank())
            return tokenMelisa.trim();
        if ("yusufakin27".equals(normalizedUsername) && tokenYusuf != null && !tokenYusuf.isBlank())
            return tokenYusuf.trim();
        return (tokenDefault != null && !tokenDefault.isBlank()) ? tokenDefault.trim() : null;
    }

    public GitHubUserResponse fetchUserWithRepos(String username) {
        if (username == null || username.isBlank()) return null;
        String normalized = username.trim().toLowerCase();
        if (!getAllowedUsernames().contains(normalized)) {
            log.warn("GitHub username not allowed: {}", username);
            return null;
        }

        String token = getTokenForUser(normalized);
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }
        headers.set("X-GitHub-Api-Version", "2022-11-28");

        try {
            ResponseEntity<GitHubProfileResponse> userResp = restTemplate.exchange(
                    GITHUB_API + "/users/" + normalized,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubProfileResponse.class
            );
            if (userResp.getStatusCode() != HttpStatus.OK || userResp.getBody() == null) return null;
            GitHubProfileResponse profile = userResp.getBody();

            ResponseEntity<List<GitHubRepoApiResponse>> reposResp = restTemplate.exchange(
                    GITHUB_API + "/users/" + normalized + "/repos?sort=updated&per_page=" + MAX_REPOS,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );
            List<GitHubRepoDto> repos = new ArrayList<>();
            if (reposResp.getStatusCode() == HttpStatus.OK && reposResp.getBody() != null) {
                for (GitHubRepoApiResponse r : reposResp.getBody()) {
                    String readme = fetchReadme(normalized, r.getName(), headers);
                    repos.add(GitHubRepoDto.builder()
                            .name(r.getName())
                            .description(r.getDescription())
                            .htmlUrl(r.getHtmlUrl())
                            .readmeContent(readme)
                            .build());
                }
            }

            return GitHubUserResponse.builder()
                    .login(profile.getLogin())
                    .name(profile.getName())
                    .bio(profile.getBio())
                    .avatarUrl(profile.getAvatarUrl())
                    .profileUrl(profile.getHtmlUrl())
                    .repos(repos)
                    .build();
        } catch (Exception e) {
            log.error("GitHub API error for user {}: {}", username, e.getMessage());
            return null;
        }
    }

    private String fetchReadme(String owner, String repo, HttpHeaders headers) {
        try {
            ResponseEntity<GitHubReadmeApiResponse> resp = restTemplate.exchange(
                    GITHUB_API + "/repos/" + owner + "/" + repo + "/readme",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    GitHubReadmeApiResponse.class
            );
            if (resp.getStatusCode() != HttpStatus.OK || resp.getBody() == null || resp.getBody().getContent() == null)
                return null;
            String decoded = new String(Base64Utils.decodeFromString(resp.getBody().getContent().replace("\n", "")), StandardCharsets.UTF_8);
            if (decoded.length() > MAX_README_LENGTH)
                decoded = decoded.substring(0, MAX_README_LENGTH) + "\n\n...";
            return decoded;
        } catch (Exception e) {
            return null;
        }
    }
}
