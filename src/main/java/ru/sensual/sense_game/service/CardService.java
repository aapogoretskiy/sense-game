package ru.sensual.sense_game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.repository.CardRepository;

import java.util.Random;

//@TODO существует проблема повторения карточек. Можно решить с помощью кэша и смотреть насколько давно карточка попадалась
@Slf4j(topic = "[CardService]")
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final Random random = new Random();

    public String getRandomCard() {
        var cards = cardRepository.findAll();
        if (CollectionUtils.isEmpty(cards)) {
            return "Нет доступных карточек.";
        }
        return cards.get(random.nextInt(cards.size())).getText();
    }

    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }
}