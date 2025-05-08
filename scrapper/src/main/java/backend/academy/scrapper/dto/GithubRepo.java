package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;

public class GithubRepo extends GithubResource {
    public GithubRepo(String owner, String repo) {
        super(LinkType.GITHUB_REPO, owner, repo);
    }

    @Override
    public String getApiPath() {
        return "/repos/%s/%s".formatted(getOwner(), getRepo());
    }
}
