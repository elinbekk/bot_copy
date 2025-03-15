//package backend.academy.scrapper;
//
//
//
//@Component
//public class StackOverflowClient {
//    private final WebClient webClient;
//
//    public StackOverflowClient() {
//        this.webClient = WebClient.builder().baseUrl("https://api.stackexchange.com/2.3").build();
//    }
//
//    public Mono<Question> getQuestion(String questionId) {
//        return webClient.get()
//            .uri("/questions/{id}?site=stackoverflow", questionId)
//            .retrieve()
//            .bodyToMono(Question.class);
//    }
//}
//
//
//private final Map<Long, BotState> userStates = new HashMap<>();
//
//private void handleCommand(long chatId, String command) {
//    BotState state = userStates.getOrDefault(chatId, BotState.IDLE);
//
//    switch (state) {
//        case IDLE -> {
//            if (command.startsWith("/track")) {
//                userStates.put(chatId, BotState.AWAITING_LINK);
//                sendMessage(chatId, "Введите ссылку для отслеживания:");
//            } else {
//                sendMessage(chatId, "Неизвестная команда.");
//            }
//        }
//        case AWAITING_LINK -> {
//            userStates.put(chatId, BotState.AWAITING_TAGS);
//            sendMessage(chatId, "Введите теги (опционально):");
//        }
//        case AWAITING_TAGS -> {
//            userStates.put(chatId, BotState.AWAITING_FILTERS);
//            sendMessage(chatId, "Настройте фильтры (опционально):");
//        }
//        case AWAITING_FILTERS -> {
//            userStates.put(chatId, BotState.IDLE);
//            sendMessage(chatId, "Ссылка добавлена в отслеживание.");
//        }
//    }
//}
//
//
//@Component
//public class LinkChecker {
//    private final StackOverflowClient stackOverflowClient;
//
//    public LinkChecker(StackOverflowClient stackOverflowClient) {
//        this.stackOverflowClient = stackOverflowClient;
//    }
//
//    @Scheduled(fixedRate = 60000)
//    public void checkLinks() {
//        List<String> links = List.of("292357"); // Заглушка, пока без БД
//        for (String link : links) {
//            stackOverflowClient.getQuestion(link)
//                .subscribe(q -> System.out.println("Обновление: " + q.title()));
//        }
//    }
//}
//
//
//Связать сервисы через HTTP
//В Bot создаём HTTP-клиент:
//@Component
//public class ScrapperClient {
//    private final WebClient webClient;
//
//    public ScrapperClient() {
//        this.webClient = WebClient.builder().baseUrl("http://scrapper-service").build();
//    }
//
//    public Mono<Void> notifyUpdate(String chatId, String message) {
//        return webClient.post()
//            .uri("/notify")
//            .bodyValue(new Notification(chatId, message))
//            .retrieve()
//            .bodyToMono(Void.class);
//    }
//}
///*************/
//
//
//public interface UpdateChecker {
//    boolean hasUpdates(String url);
//}
//
//public class GitHubClient implements UpdateChecker {
//    private final HttpClient httpClient;
//
//    public GitHubClient(HttpClient httpClient) {
//        this.httpClient = httpClient;
//    }
//
//    @Override
//    public boolean hasUpdates(String url) {
//        // Разбираем URL и извлекаем owner/repo
//        String[] parts = url.split("/");
//        String owner = parts[parts.length - 2];
//        String repo = parts[parts.length - 1];
//
//        String apiUrl = "https://api.github.com/repos/" + owner + "/" + repo;
//        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl)).GET().build();
//
//        try {
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            JSONObject json = new JSONObject(response.body());
//            String updatedAt = json.getString("updated_at");
//
//            // Логика проверки обновлений
//            return checkIfUpdated(url, updatedAt);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private boolean checkIfUpdated(String url, String updatedAt) {
//        // Реализовать сохранение и сравнение с предыдущими данными
//        return true;
//    }
//}
//
//
//public class StackOverflowClient implements UpdateChecker {
//    private final HttpClient httpClient;
//
//    public StackOverflowClient(HttpClient httpClient) {
//        this.httpClient = httpClient;
//    }
//
//    @Override
//    public boolean hasUpdates(String url) {
//        // Извлекаем question_id
//        String questionId = url.replaceAll("\\D+", "");
//
//        String apiUrl = "https://api.stackexchange.com/2.3/questions/" + questionId +
//            "?order=desc&sort=activity&site=stackoverflow";
//        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl)).GET().build();
//
//        try {
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            JSONObject json = new JSONObject(response.body());
//            long lastActivity = json.getJSONArray("items").getJSONObject(0).getLong("last_activity_date");
//
//            // Логика проверки обновлений
//            return checkIfUpdated(url, lastActivity);
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private boolean checkIfUpdated(String url, long lastActivity) {
//        return true;
//    }
//}
//
//
//public class UpdateService {
//    private final Map<String, UpdateChecker> clients = Map.of(
//        "github.com", new GitHubClient(HttpClient.newHttpClient()),
//        "stackoverflow.com", new StackOverflowClient(HttpClient.newHttpClient())
//    );
//
//    public boolean checkForUpdates(String url) {
//        for (String key : clients.keySet()) {
//            if (url.contains(key)) {
//                return clients.get(key).hasUpdates(url);
//            }
//        }
//        return false;
//    }
//}
//
//
//@Scheduled(fixedRate = 60000) // Раз в минуту
//public void checkAllTrackedLinks() {
//    for (String url : trackedUrls) {
//        if (updateService.checkForUpdates(url)) {
//            sendUpdateNotification(url);
//        }
//    }
//}
//
//
//@Test
//void testFetchUpdates() throws Exception {
//    mockWebServer.enqueue(new MockResponse()
//        .setBody("{\"updated_at\": \"2024-03-07T12:00:00Z\"}")
//        .setResponseCode(200));
//
//    String lastUpdate = gitHubClient.fetchLastUpdate("test/repo");
//
//    assertEquals("2024-03-07T12:00:00Z", lastUpdate);
//}
//
