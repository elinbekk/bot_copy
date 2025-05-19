package backend.academy.scrapper.controller;

import backend.academy.scrapper.dto.Link;
import backend.academy.scrapper.dto.LinkRequest;
import backend.academy.scrapper.dto.LinkResponse;
import backend.academy.scrapper.exception.DuplicateLinkException;
import backend.academy.scrapper.service.LinkService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ResponseEntity<List<LinkResponse>> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<LinkResponse> data = linkService.getUserListLinks(chatId).stream()
                .map(link ->
                        new LinkResponse(link.getUrl(), link.getTags(), link.getFilters(), link.getLastCheckedTime()))
                .toList();
        return ResponseEntity.ok(new ArrayList<>(data));
    }

    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody LinkRequest linkRequest) {
        Link model = new Link(
                null,
                linkRequest.getLink(),
                linkRequest.getLinkType(),
                linkRequest.getTags(),
                linkRequest.getFilters(),
                String.valueOf(Instant.now()));
        try {
            linkService.addLink(chatId, model);
            LinkResponse resp = new LinkResponse(
                    model.getLinkId(), model.getUrl(), model.getTags(), model.getFilters(), model.getLastCheckedTime());
            return ResponseEntity.ok(resp);
        } catch (DuplicateLinkException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") Long chatId, @RequestBody LinkRequest req) {
        try {
            linkService.removeLink(chatId, req.getLink());
            LinkResponse resp = new LinkResponse(null, req.getLink(), null, null, null);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
