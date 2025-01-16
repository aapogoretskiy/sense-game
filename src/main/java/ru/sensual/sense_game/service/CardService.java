package ru.sensual.sense_game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.model.request.UploadCardRequest;
import ru.sensual.sense_game.model.response.UploadCardsResponse;
import ru.sensual.sense_game.model.type.UploadMessageType;
import ru.sensual.sense_game.repository.CardRepository;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.sensual.sense_game.model.type.UploadMessageType.FAILED;
import static ru.sensual.sense_game.model.type.UploadMessageType.LOADED_WITH_FAILS;
import static ru.sensual.sense_game.model.type.UploadMessageType.NOTHING_LOADED;
import static ru.sensual.sense_game.model.type.UploadMessageType.SUCCESS;

//@TODO существует проблема повторения карточек. Можно решить с помощью кэша и смотреть насколько давно карточка попадалась
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    private final CardRepository cardRepository;
    private final Random random = new Random();

    /**
     * Получает все карточки из репозитория.
     *
     * @return список всех объектов {@link Card}, хранящихся в репозитории.
     */
    public List<Card> getAllCards() {
        log.info("Receiving all cards from database");
        return cardRepository.findAll();
    }

    /**
     * Получает случайную карточку из репозитория.
     * Если карточки отсутствуют, возвращает карточку с дефолтными значениями.
     *
     * @return случайная {@link Card}, если доступна; в противном случае, карточка с placeholder-значениями.
     */
    public Card getRandomCard() {
        log.info("Receiving random card from database");
        var cards = cardRepository.findAll();
        if (CollectionUtils.isEmpty(cards)) {
            log.warn("Table with cards in database is empty. Nothing to return");
            return new Card(-1L, "Нет доступных карточек.", "NONE", -1);
        }
        return cards.get(random.nextInt(cards.size()));
    }

    /**
     * Обрабатывает загрузку списка карточек, предоставленных в запросе.
     * Каждая карточка сохраняется в репозитории, при этом метод отслеживает количество
     * успешно сохранённых карточек и карточек, которые не удалось сохранить.
     *
     * @param uploadCardRequest объект {@link UploadCardRequest}, содержащий список объектов {@link Card} для загрузки.
     * @return объект {@link UploadCardsResponse}, содержащий результат загрузки:
     * тип сообщения, количество успешных сохранений и количество неудач.
     */
    public UploadCardsResponse uploadCardsContent(UploadCardRequest uploadCardRequest) {
        var cards = uploadCardRequest.getCards();
        var total = cards.size();
        log.info("Start save cards. Total count of cards: [{}]", total);
        if (total == 0) {
            log.warn("List of cards is empty! Nothing loaded");
            return new UploadCardsResponse(NOTHING_LOADED, 0, 0);
        }
        var successCount = new AtomicInteger(0);
        var failCount = new AtomicInteger(0);
        cards.forEach(card -> {
            log.info("Start save card with text: [{}]", card.getText());
            try {
                cardRepository.save(card);
            } catch (Exception e) {
                failCount.getAndIncrement();
                log.error("[Card upload failed] Card with text: [{}] did not save. Reason: ", card.getText(), e);
                return;
            }
            successCount.getAndIncrement();
            log.info("[Card upload success] Card with text: [{}] saved", card.getText());
        });
        log.info("Process of saving cards is end");
        return new UploadCardsResponse(getMessageType(successCount.get(), failCount.get()), successCount.get(), failCount.get());
    }

    private UploadMessageType getMessageType(int successCount, int failCount) {
        if (failCount == 0) return SUCCESS;
        return successCount != 0 ? LOADED_WITH_FAILS : FAILED;
    }

}