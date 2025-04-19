package backend.academy.scrapper;

import backend.academy.scrapper.dto.LinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.repository.LinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/links")
public class LinkController {
    private final LinkRepository linkRepository;

    public LinkController(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }
    @GetMapping
    public ResponseEntity<List<Link>> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<Link> data = linkRepository.findAllByChatId(chatId).stream()
            .map(link -> new Link(link.getLinkId(), link.getUrl(), link.getLinkType(), link.getTags(), link.getFilters(), link.getLastCheckedTime()))
            .toList();
        return ResponseEntity.ok(new ArrayList<Link>(data));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody LinkRequest linkRequest
    ) {
        Link model = new Link(null, linkRequest.getLink(), linkRequest.getLinkType(), linkRequest.getTags(), linkRequest.getFilters(), Instant.now());
        linkRepository.saveLink(chatId, model);
        LinkResponse resp = new LinkResponse(model.getLinkId(), model.getUrl(), model.getTags(), model.getFilters());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody LinkRequest req
    ) {
        linkRepository.remove(chatId, req.getLink());
        LinkResponse resp = new LinkResponse(null, req.getLink(), null, null);
        return ResponseEntity.ok(resp);
    }
}

