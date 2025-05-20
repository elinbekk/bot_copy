package backend.academy.bot.helper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class InputParser {
    public static Set<String> parseTags(String message) {
        if (message.equals("-")) return Collections.emptySet();
        return Arrays.stream(message.split("\\s+"))
                .filter(tag -> !tag.isBlank())
                .collect(Collectors.toSet());
    }

    public static Map<String, String> parseFilters(String message) {
        Map<String, String> filters = new HashMap<>();

        if (message == null || message.isBlank() || message.equals("-")) {
            return filters;
        }

        return Arrays.stream(message.split("\\s+"))
                .filter(part -> !part.isBlank())
                .map(part -> part.split(":", 2))
                .collect(Collectors.toMap(keyValue -> keyValue[0].trim(), keyValue -> {
                    if (keyValue.length != 2) {
                        throw new IllegalArgumentException(String.format(
                                "Некорректный формат фильтра: %s%nИспользуйте формат: ключ:значение",
                                Arrays.toString(keyValue)));
                    }
                    return keyValue[1].trim();
                }));
    }
}
