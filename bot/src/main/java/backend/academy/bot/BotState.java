package backend.academy.bot;

public enum BotState {
    INITIAL,
    WAITING_FOR_LINK,
    WAITING_FOR_TAGS,
    WAITING_FOR_FILTERS,
    TRACKING_CONFIRMED
}
