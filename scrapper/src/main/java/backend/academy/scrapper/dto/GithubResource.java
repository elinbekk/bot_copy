package backend.academy.scrapper.dto;

import backend.academy.bot.entity.LinkType;

public class GithubResource {
    private final LinkType type;
    private final String owner;
    private final String repo;
    private final String number; // для issue/PR

    public GithubResource(LinkType type, String owner, String repo, String number) {
        this.type = type;
        this.owner = owner;
        this.repo = repo;
        this.number = number;
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

    public String getNumber() {
        return number;
    }
}
