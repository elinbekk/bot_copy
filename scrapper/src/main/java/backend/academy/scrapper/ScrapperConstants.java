package backend.academy.scrapper;

public class ScrapperConstants {
    public static final String REPO_REGEX = "https?://github.com/([^/]+)/([^/]+)/?";
    public static final String ISSUE_REGEX = "https?://github.com/([^/]+)/([^/]+)/issues/(\\d+)";
    public static final String PR_REGEX = "https?://github.com/([^/]+)/([^/]+)/pull/(\\d+)";
    public static final String SO_REGEX = "https?://stackoverflow.com/questions/(\\d+)";
    public static final String BOT_UPDATES_URI = "/updates";
}
