package ru.sensual.sense_game.model.type;

import java.util.Arrays;
import java.util.Optional;

public enum CardCategory {
    EROTIC,
    FLIRT,
    EMOTIONS,
    TOUCH,
    FUN,
    GAME,
    PLANNING,
    CULINARY,
    CREATIVITY,
    ADVENTURE,
    TRAVEL,
    REVELATION,
    NONE;

    public static Optional<CardCategory> from(String value) {
        if (value == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(value.trim())
                        || e.getDisplayName().equalsIgnoreCase(value.trim()))
                .findFirst();
    }

    public String getDisplayName() {
        return switch (this) {
            case EROTIC -> "Эротика";
            case FLIRT -> "Флирт";
            case EMOTIONS -> "Эмоции";
            case TOUCH -> "Тактильность";
            case FUN -> "Веселье";
            case GAME -> "Игра";
            case PLANNING -> "Планирование";
            case CULINARY -> "Кулинария";
            case CREATIVITY -> "Творчество";
            case ADVENTURE -> "Приключения";
            case TRAVEL -> "Путешествия";
            case REVELATION -> "Откровения";
            default -> NONE.getDisplayName();
        };
    }
}
