package backend.academy.bot;

import java.util.List;

record TrackedResource(String link, List<String> tags, String filters) {}
