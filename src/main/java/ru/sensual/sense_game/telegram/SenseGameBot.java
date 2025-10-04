package ru.sensual.sense_game.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sensual.sense_game.configuration.TelegramBotConfig;
import ru.sensual.sense_game.model.GameSession;
import ru.sensual.sense_game.service.CardService;
import ru.sensual.sense_game.service.GameSessionService;

import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SenseGameBot extends TelegramLongPollingBot {

    private final TelegramBotConfig telegramBotConfig;
    private final CardService cardService;
    private final GameSessionService gameSessionService;

    @Override
    public String getBotUsername() {
        return telegramBotConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return telegramBotConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            var userMessage = update.getMessage().getText().trim();
            var chatId = update.getMessage().getChatId();
            if (!userMessage.startsWith("/")) {
                return;
            }
            var command = extractCommand(userMessage);
            switch (command) {
                case "/start" -> handleStartCommand(chatId);
                case "/help" -> handleHelpCommand(chatId);
                case "/newgame" -> handleNewGameCommand(chatId, userMessage);
                case "/nextcard" -> handleNextCardCommand(update, chatId);
                case "/skipcard" -> handleSkipCardCommand(chatId);
                case "/stopgame" -> handleStopGameCommand(chatId);
                default -> sendTextMessage(chatId, "Неизвестная команда. Используйте /help, чтобы посмотреть доступные команды.");
            }
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        var message = new SendMessage(chatId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error while trying send text message", e);
        }
    }

    private void handleStartCommand(Long chatId) {
        var welcomeText = "Добро пожаловать в Sense Game!\n"
                + "Команды:\n"
                + "/newgame - начать новую игру (можно указать параметры, например: /newgame categories=Флирт,Эксперимент difficulty=1-3)\n"
                + "/nextcard - получить карточку для текущего игрока\n"
                + "/skipcard - пропустить карточку и получить новую\n"
                + "/stopgame - завершить текущую игру\n"
                + "Используйте /help, чтобы посмотреть подробное описание команд.";
        sendTextMessage(chatId, welcomeText);
    }

    private void handleHelpCommand(Long chatId) {
        var helpText = "Доступные команды:\n"
                + "/start - приветствие и краткие правила\n"
                + "/newgame [categories=..] [difficulty=min-max] - начать новую игру с нужными фильтрами\n"
                + "/nextcard - получить следующую карточку\n"
                + "/skipcard - пропустить текущую карточку\n"
                + "/stopgame - завершить игру и очистить состояние.";
        sendTextMessage(chatId, helpText);
    }

    private void handleNewGameCommand(Long chatId, String message) {
        var settings = parseSettings(message);
        var session = gameSessionService.startSession(chatId, settings.categories(), settings.minDifficulty(), settings.maxDifficulty());
        var response = new StringBuilder("Новая игра началась! Используйте /nextcard для получения карточки.\n");
        response.append(session.describeSettings());
        sendTextMessage(chatId, response.toString());
    }

    private void handleNextCardCommand(Update update, Long chatId) {
        var sessionOptional = ensureActiveSession(chatId);
        if (sessionOptional.isEmpty()) {
            return;
        }
        var session = sessionOptional.get();
        var card = cardService.getRandomCard(session.getCategories(), session.getMinDifficulty(), session.getMaxDifficulty(), session.getCurrentCard());
        if (card.getId() != null && card.getId() == -1L) {
            sendTextMessage(chatId, card.getText());
            return;
        }

        var from = update.getMessage().getFrom();
        var playerName = formatPlayerName(from.getFirstName(), from.getLastName(), from.getUserName());
        gameSessionService.updateCurrentTurn(chatId, card, from.getId(), playerName);

        var response = new StringBuilder();
        response.append("Игрок ").append(playerName).append(", ваша карточка:\n");
        response.append(card.getText());
        response.append("\nКатегория: ").append(card.getCategory());
        response.append("\nСложность: ").append(card.getDifficulty());
        sendTextMessage(chatId, response.toString());
    }

    private void handleSkipCardCommand(Long chatId) {
        var sessionOptional = ensureActiveSession(chatId);
        if (sessionOptional.isEmpty()) {
            return;
        }
        var session = sessionOptional.get();
        var card = cardService.getRandomCard(session.getCategories(), session.getMinDifficulty(), session.getMaxDifficulty(), session.getCurrentCard());
        if (card.getId() != null && card.getId() == -1L) {
            sendTextMessage(chatId, card.getText());
            return;
        }

        gameSessionService.updateCurrentCard(chatId, card);
        var playerName = Optional.ofNullable(session.getCurrentPlayerName()).orElse("Игрок");

        var response = new StringBuilder();
        response.append("Карточка пропущена. ").append(playerName).append(", попробуйте новую:\n");
        response.append(card.getText());
        response.append("\nКатегория: ").append(card.getCategory());
        response.append("\nСложность: ").append(card.getDifficulty());
        sendTextMessage(chatId, response.toString());
    }

    private void handleStopGameCommand(Long chatId) {
        if (!gameSessionService.isSessionActive(chatId)) {
            sendTextMessage(chatId, "Активная игра не найдена. Используйте /newgame, чтобы начать новую сессию.");
            return;
        }
        var session = gameSessionService.getSession(chatId).orElse(null);
        gameSessionService.endSession(chatId);
        var response = new StringBuilder("Игра завершена.");
        if (session != null && session.getCurrentPlayerName() != null) {
            response.append(" Последним ходил: ").append(session.getCurrentPlayerName()).append('.');
        }
        sendTextMessage(chatId, response.toString());
    }

    private Optional<GameSession> ensureActiveSession(Long chatId) {
        var sessionOptional = gameSessionService.getSession(chatId);
        if (sessionOptional.isEmpty() || !sessionOptional.get().isActive()) {
            sendTextMessage(chatId, "Игра ещё не запущена. Используйте /newgame, чтобы начать новую сессию.");
            return Optional.empty();
        }
        return sessionOptional;
    }

    private SessionSettings parseSettings(String message) {
        var categories = new HashSet<String>();
        Integer minDifficulty = null;
        Integer maxDifficulty = null;

        var parts = message.split("\\s+");
        for (int i = 1; i < parts.length; i++) {
            var part = parts[i];
            var delimiterIndex = part.indexOf('=');
            if (delimiterIndex <= 0) {
                continue;
            }
            var key = part.substring(0, delimiterIndex).toLowerCase(Locale.ROOT);
            var value = part.substring(delimiterIndex + 1).trim();
            if (value.isEmpty()) {
                continue;
            }
            switch (key) {
                case "category", "categories" -> categories.addAll(parseCategories(value));
                case "difficulty" -> {
                    var range = parseDifficultyRange(value);
                    minDifficulty = range.minDifficulty();
                    maxDifficulty = range.maxDifficulty();
                }
                case "mindifficulty" -> minDifficulty = parseNumber(value).orElse(minDifficulty);
                case "maxdifficulty" -> maxDifficulty = parseNumber(value).orElse(maxDifficulty);
                default -> log.warn("Unknown parameter [{}] in /newgame command", key);
            }
        }
        return new SessionSettings(categories, minDifficulty, maxDifficulty);
    }

    private Set<String> parseCategories(String value) {
        var result = new HashSet<String>();
        for (var category : value.split(",")) {
            var trimmed = category.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private DifficultyRange parseDifficultyRange(String value) {
        if (value.contains("-")) {
            var rangeValues = value.split("-");
            if (rangeValues.length == 2) {
                var min = parseNumber(rangeValues[0]).orElse(null);
                var max = parseNumber(rangeValues[1]).orElse(null);
                return new DifficultyRange(min, max);
            }
        }
        var number = parseNumber(value).orElse(null);
        return new DifficultyRange(number, number);
    }

    private Optional<Integer> parseNumber(String value) {
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            log.warn("Failed to parse number from value [{}]", value, e);
            return Optional.empty();
        }
    }

    private String extractCommand(String message) {
        var spaceIndex = message.indexOf(' ');
        return spaceIndex > 0 ? message.substring(0, spaceIndex).toLowerCase(Locale.ROOT) : message.toLowerCase(Locale.ROOT);
    }

    private String formatPlayerName(String firstName, String lastName, String username) {
        var builder = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            builder.append(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(lastName.trim());
        }
        if (builder.length() == 0) {
            if (username != null && !username.isBlank()) {
                builder.append('@').append(username.trim());
            } else {
                builder.append("Игрок");
            }
        }
        return builder.toString();
    }

    private record SessionSettings(Set<String> categories, Integer minDifficulty, Integer maxDifficulty) {}

    private record DifficultyRange(Integer minDifficulty, Integer maxDifficulty) {}
}

