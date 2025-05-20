package backend.academy.scrapper.dto;

import backend.academy.scrapper.entity.LinkType;

public abstract class GithubResource {
    private final LinkType type;
    private final String owner;
    private final String repo;

    public GithubResource(LinkType type, String owner, String repo) {
        this.type = type;
        this.owner = owner;
        this.repo = repo;
    }

    public LinkType getType() {
        return type;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepo() {
        return repo;
    }

    public abstract String getApiPath();
}
