package ru.sensual.sense_game.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Содержит состояние игровой сессии в конкретном чате Telegram.
 */
@Getter
@Setter
@NoArgsConstructor
public class GameSession {

    private Long chatId;
    private boolean active;
    private Set<String> categories = new HashSet<>();
    private Integer minDifficulty;
    private Integer maxDifficulty;
    private Card currentCard;
    private Long currentPlayerId;
    private String currentPlayerName;

    public GameSession(Long chatId) {
        this.chatId = chatId;
    }

    public String describeSettings() {
        var builder = new StringBuilder();
        builder.append("Категории: ");
        if (categories == null || categories.isEmpty()) {
            builder.append("все");
        } else {
            builder.append(String.join(", ", categories));
        }

        builder.append("\nСложность: ");
        if (minDifficulty == null && maxDifficulty == null) {
            builder.append("любая");
        } else if (minDifficulty != null && maxDifficulty != null && minDifficulty.equals(maxDifficulty)) {
            builder.append(minDifficulty);
        } else {
            if (minDifficulty != null) {
                builder.append("от ").append(minDifficulty);
            }
            if (maxDifficulty != null) {
                if (minDifficulty != null) {
                    builder.append(" ");
                }
                builder.append("до ").append(maxDifficulty);
            }
        }
        return builder.toString();
    }
}

