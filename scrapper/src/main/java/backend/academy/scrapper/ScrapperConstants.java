package backend.academy.scrapper;

public class ScrapperConstants {
    public static final String REPO_REGEX = "https?://github.com/([^/]+)/([^/]+)/?";
    public static final String ISSUE_REGEX = "https?://github.com/([^/]+)/([^/]+)/issues/(\\d+)";
    public static String PR_REGEX = "https?://github.com/([^/]+)/([^/]+)/pull/(\\d+)";
}
