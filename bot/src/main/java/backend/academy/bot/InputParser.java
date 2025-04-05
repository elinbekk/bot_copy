package backend.academy.bot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InputParser {
    protected Set<String> parseTags(String message) {
        return Arrays.stream(message.split("\\s+"))
            .filter(tag -> !tag.isBlank())
            .collect(Collectors.toSet());
    }

    protected Map<String, String> parseFilters(String message) {
        Map<String, String> filters = new HashMap<>();

        if (message == null || message.isBlank()) {
            return filters;
        }

        Arrays.stream(message.split("\\s+"))
            .filter(part -> !part.isBlank())
            .forEach(part -> {
                String[] keyValue = part.split(":", 2);
                if (keyValue.length != 2) {
                    throw new IllegalArgumentException(
                        "Некорректный формат фильтра: " + part + "\n" +
                            "Используйте формат: ключ:значение"
                    );
                }
                filters.put(keyValue[0].trim(), keyValue[1].trim());
            });

        return filters;
    }
}
