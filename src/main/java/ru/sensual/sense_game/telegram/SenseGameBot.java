package ru.sensual.sense_game.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.sensual.sense_game.configuration.TelegramBotConfig;
import ru.sensual.sense_game.service.CardService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SenseGameBot extends TelegramLongPollingBot {

    private final TelegramBotConfig telegramBotConfig;
    private final CardService cardService;

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
            var userMessage = update.getMessage().getText();
            var chatId = update.getMessage().getChatId();
            if (userMessage.equalsIgnoreCase("/start")) {
                sendTextMessage(chatId, "Добро пожаловать в Sense Game! Используйте команду /get_card, чтобы получить карточку.");
            } else if (userMessage.equalsIgnoreCase("/get_card")) {
                var card = cardService.getRandomCard();
                sendTextMessage(chatId, "Вот ваша карточка: " + card.getText());
            } else {
                sendTextMessage(chatId, "Неизвестная команда. Попробуйте /get_card.");
            }
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        var message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.out.println("Error while trying send text message." + e);
        }
    }
}
