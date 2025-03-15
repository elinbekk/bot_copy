package backend.academy.scrapper;


import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class GithubClient {
    private final WebClient webClient = WebClient.create("https://api.github.com");

    public GithubResponse getRepository(String owner, String repo) {
        return webClient.get()
            .uri("/repos/{owner}/{repo}", owner, repo)
            .retrieve()
            .bodyToMono(GithubResponse.class)
            .block();
    }
}
