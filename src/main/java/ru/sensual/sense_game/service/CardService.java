package ru.sensual.sense_game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.repository.CardRepository;

import java.util.List;
import java.util.Random;

//@TODO существует проблема повторения карточек. Можно решить с помощью кэша и смотреть насколько давно карточка попадалась
@Slf4j(topic = "[CardService]")
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final Random random = new Random();

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card getRandomCard() {
        var cards = cardRepository.findAll();
        if (CollectionUtils.isEmpty(cards)) {
            return new Card(-1L, "Нет доступных карточек.", "NONE", -1);
        }
        return cards.get(random.nextInt(cards.size()));
    }

    public Card saveCard(Card card) {
        return cardRepository.save(card);
    }
}