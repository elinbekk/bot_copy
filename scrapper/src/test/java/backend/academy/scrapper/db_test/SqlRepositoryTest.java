package backend.academy.scrapper.db_test;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkType;
import backend.academy.scrapper.repository.SqlChatRepository;
import backend.academy.scrapper.repository.SqlLinkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@SpringBootTest(properties = "access-type=SQL")
public class SqlRepositoryTest {
    @Autowired
    private SqlLinkRepository linkRepo;

    @Autowired
    private SqlChatRepository chatRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long chatId = 42L;

    private Link makeLink() {
        return new Link(
            null,                            // linkId генерится в БД
            "https://foo/bar",
            chatId,// url
            LinkType.GITHUB_REPO,            // тип
            Set.of("tag1", "tag2"),           // тэги
            Map.of("f1", "v1"),               // фильтры
            Instant.now().toString()         // lastCheckedTime
        );
    }

    @Test
    void saveAndFindAllAndExistsTest() {

//        assertThat(linkRepo.findAllLinksByChatId(chatId)).isEmpty();
//        assertThat(linkRepo.exists(chatId, "https://foo/bar")).isFalse();

        Link link = makeLink();
        chatRepo.save(chatId);
        linkRepo.save(link);

        List<Link> all = linkRepo.findAllLinksByChatId(chatId);
//        assertThat(all).hasSize(1);

        Link saved = all.getFirst();
        assertThat(saved.getChatId()).isEqualTo(chatId);
        assertThat(saved.getUrl()).isEqualTo(link.getUrl());
        assertThat(saved.getLinkType()).isEqualTo(link.getLinkType());
        assertThat(saved.getTags()).containsExactlyInAnyOrderElementsOf(link.getTags());
        assertThat(saved.getFilters()).containsExactlyInAnyOrderEntriesOf(link.getFilters());
        assertThat(saved.getLastCheckedTime()).isNotBlank();

        assertThat(linkRepo.exists(chatId, link.getUrl())).isTrue();
        linkRepo.delete(chatId, link.getUrl());
    }

    /*@Test
    void testDelete() {
        Link link1 = makeLink();
        Link link2 = makeLink();
        link2.setUrl("https://another/url");

        chatRepo.save(chatId);
        linkRepo.save(link1);
        linkRepo.save(link2);

        assertThat(linkRepo.findAllLinksByChatId(chatId))
            .extracting(Link::getUrl)
            .containsExactlyInAnyOrder(link1.getUrl(), link2.getUrl());

        linkRepo.delete(chatId, link1.getUrl());
        List<Link> after = linkRepo.findAllLinksByChatId(chatId);
        assertThat(after).hasSize(1)
            .extracting(Link::getUrl)
            .containsExactly(link2.getUrl());


        assertThat(linkRepo.exists(chatId, link1.getUrl())).isFalse();
    }*/
}


