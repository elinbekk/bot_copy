package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;

public class GithubPR extends GithubResource {
    private final int prNumber;

    public GithubPR(String owner, String repo, int prNumber) {
        super(LinkType.GITHUB_PR, owner, repo);
        this.prNumber = prNumber;
    }

    public int getPrNumber() {
        return prNumber;
    }
}
