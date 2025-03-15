package backend.academy.scrapper;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ClientTest {
    private final GithubClient gitHubClient = new GithubClient();

    @Test
    void testGitHubClient() {
        GithubResponse response = gitHubClient.getRepository("octocat", "Hello-World");

        assertThat(response).isNotNull();
        System.out.println(response);
    }
}
