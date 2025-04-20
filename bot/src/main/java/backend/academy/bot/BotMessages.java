package backend.academy.bot;

public class BotMessages {
    public static final String START_MESSAGE = """
        Привет! Я помогу отслеживать изменения на GitHub и Stack Overflow.
        Доступные команды:
        /track - начать отслеживание ссылки
        /untrack - прекратить отслеживание
        /list - показать отслеживаемые ссылки
        /help - показать справку""";

    public static final String HELP_MESSAGE = """
        Доступные команды:
        /track - добавить ссылку
        /untrack - удалить ссылку
        /list - показать все ссылки
        /help - показать справку""";

    public static final String UNKNOWN_COMMAND_MESSAGE = "Неизвестная команда. Используйте /help для списка команд";
    public static final String TRACK_MESSAGE = "Ссылка успешно добавлена!";
    public static final String UNTRACK_MESSAGE = "Ссылка успешно удалена!";
    public static final String LIST_MESSAGE = "Ваши отслеживаемые ссылки:\n";
    public static final String FORMAT_LIST_MESSAGE = "• %s\nТеги: %s\nФильтры: %s\n Последнее время проверки:%s";
    public static final String LIST_EMPTY_MESSAGE = "Список отслеживаемых ссылок пуст";
    public static final String LINK_DUPLICATED_MESSAGE = "Эта ссылка уже отслеживается";
    public static final String LINK_NOT_FOUND_MESSAGE = "Этой ссылки нет в вашем списке отслеживания";
    public static final String LINK_INCORRECT_MESSAGE= "Некорректный формат ссылки";
    public static final String WAITING_FOR_LINK_MESSAGE = "Введите ссылку для отслеживания:";
    public static final String WAITING_FOR_TAGS_MESSAGE = "Введите теги через пробел (опционально), если их нет, поставьте - :";
    public static final String WAITING_FOR_FILTERS_MESSAGE = "Введите фильтры в формате key:value (опционально), если их нет, поставьте -:";

    public static final String UPDATE_MESSAGE = """
             Обновление в отслеживаемой ссылке!

            Ссылка: %s
            Описание: %s

            Чтобы прекратить отслеживание, используйте /untrack
            """;
}
