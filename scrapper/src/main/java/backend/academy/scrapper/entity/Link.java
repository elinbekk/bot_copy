package backend.academy.scrapper.entity;

import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class Link {
    private Long linkId;
    private String url;
    private Long chatId;
    private LinkType linkType;
    private Set<String> tags;
    private Map<String, String> filters;
    private String lastCheckedTime;
}
