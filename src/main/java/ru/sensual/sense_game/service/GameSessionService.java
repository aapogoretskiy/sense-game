package ru.sensual.sense_game.service;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.model.GameSession;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameSessionService {

    private final Map<Long, GameSession> sessions = new ConcurrentHashMap<>();

    public GameSession startSession(Long chatId, Set<String> categories, Integer minDifficulty, Integer maxDifficulty) {
        var session = sessions.computeIfAbsent(chatId, GameSession::new);
        session.setActive(true);
        session.setCategories(new HashSet<>(normalizeCategories(categories)));
        session.setMinDifficulty(minDifficulty);
        session.setMaxDifficulty(maxDifficulty);
        session.setCurrentCard(null);
        session.setCurrentPlayerId(null);
        session.setCurrentPlayerName(null);
        return session;
    }

    public Optional<GameSession> getSession(Long chatId) {
        return Optional.ofNullable(sessions.get(chatId));
    }

    public void endSession(Long chatId) {
        sessions.computeIfPresent(chatId, (id, session) -> {
            session.setActive(false);
            session.setCurrentCard(null);
            session.setCurrentPlayerId(null);
            session.setCurrentPlayerName(null);
            return session;
        });
    }

    public void updateCurrentTurn(Long chatId, Card card, Long playerId, String playerName) {
        sessions.computeIfPresent(chatId, (id, session) -> {
            session.setCurrentCard(card);
            session.setCurrentPlayerId(playerId);
            session.setCurrentPlayerName(playerName);
            return session;
        });
    }

    public void updateCurrentCard(Long chatId, Card card) {
        sessions.computeIfPresent(chatId, (id, session) -> {
            session.setCurrentCard(card);
            return session;
        });
    }

    public boolean isSessionActive(Long chatId) {
        return getSession(chatId).map(GameSession::isActive).orElse(false);
    }

    public Set<String> normalizeCategories(Set<String> categories) {
        if (CollectionUtils.isEmpty(categories)) {
            return Collections.emptySet();
        }
        var normalized = new HashSet<String>();
        categories.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.trim())
                .forEach(normalized::add);
        return normalized;
    }
}

