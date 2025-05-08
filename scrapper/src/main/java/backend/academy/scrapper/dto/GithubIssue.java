package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;

public class GithubIssue extends GithubResource {
    private final int issueNumber;

    public GithubIssue(String owner, String repo, int issueNumber) {
        super(LinkType.GITHUB_ISSUE, owner, repo);
        this.issueNumber = issueNumber;
    }

    public int getIssueNumber() {
        return issueNumber;
    }

    @Override
    public String getApiPath() {
        return "/repos/%s/%s/issues/%d"
            .formatted(getOwner(), getRepo(), getIssueNumber());
    }
}
