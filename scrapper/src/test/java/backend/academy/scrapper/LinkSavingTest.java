package backend.academy.scrapper;

import static backend.academy.scrapper.entity.LinkType.GITHUB_REPO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.InMemoryLinkRepository;
import backend.academy.scrapper.repository.LinkRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class LinkSavingTest {
    private LinkRepository linkRepository;
    private static final long testChatId = 123L;
    private static final long testLinkId = 1234L;
    Link link;

    @Before
    public void setUp() {
        linkRepository = new InMemoryLinkRepository();
        link = new Link(
                testLinkId,
                "https://github.com/user/repo",
                GITHUB_REPO,
                Set.of("bug", "feature"),
                Map.of("state", "open"),
                "2025-04-16T12:00:00Z");
    }

    @Test
    public void linkSavedSuccessfulTest() {
        linkRepository.saveLink(testChatId, link);
        int expectedSize = 1;
        assertEquals(expectedSize, linkRepository.getAllLinks().size());
    }

    @Test
    public void saveDuplicateLinkThrowsExceptionTest() {
        linkRepository.saveLink(testChatId, link);
        Link duplicate = new Link(
                testLinkId + 1,
                link.getUrl(),
                link.getLinkType(),
                link.getTags(),
                link.getFilters(),
                link.getLastCheckedTime());
        assertThrows(IllegalArgumentException.class, () -> linkRepository.saveLink(testChatId, duplicate));
    }

    @Test
    public void remove_ShouldDeleteExistingLink() {
        linkRepository.saveLink(testChatId, link);
        linkRepository.remove(testChatId, link.getUrl());

        assertTrue(linkRepository.findAllByChatId(testChatId).isEmpty());
    }

    @Test
    public void remove_ShouldThrowWhenLinkNotFound() {
        assertThrows(IllegalStateException.class, () -> linkRepository.remove(testChatId, "invalid-url"));
    }

    @Test
    public void linkIsAlreadyExists_ShouldDetectDuplicates() {
        linkRepository.saveLink(testChatId, link);

        Link duplicate = new Link(
                2L,
                link.getUrl(),
                link.getLinkType(),
                link.getTags(),
                link.getFilters(),
                Instant.now().toString());

        assertTrue(linkRepository.linkIsExists(testChatId, duplicate));
    }
}
