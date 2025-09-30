package ru.sensual.sense_game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.model.request.UploadCardRequest;
import ru.sensual.sense_game.model.response.UploadCardsResponse;
import ru.sensual.sense_game.model.type.UploadMessageType;
import ru.sensual.sense_game.repository.CardRepository;

import java.io.InputStream;
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
        var cards = cardRepository.findAll();
        if (CollectionUtils.isEmpty(cards)) {
            log.warn("Table with cards in database is empty. Nothing to return");
            return new Card(-1L, "Нет доступных карточек.", "NONE", -1);
        }
        var card = cards.get(random.nextInt(cards.size()));
        log.info("Received random card with text: [{}]", card.getText());
        return card;
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

    public UploadCardsResponse uploadCardsExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Excel file is empty or not provided");
            return new UploadCardsResponse(NOTHING_LOADED, 0, 0);
        }

        var successCount = new AtomicInteger(0);
        var failCount = new AtomicInteger(0);
        log.info("Start processing excel file [{}]", file.getOriginalFilename());
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                log.warn("Excel file [{}] does not contain sheets", file.getOriginalFilename());
                return new UploadCardsResponse(NOTHING_LOADED, 0, 0);
            }

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row == null) {
                    continue;
                }
                if (isHeaderRow(row, formatter) || isRowEmpty(row, formatter)) {
                    continue;
                }

                try {
                    String text = formatter.formatCellValue(row.getCell(0));
                    String category = formatter.formatCellValue(row.getCell(1));
                    Integer difficulty = parseDifficulty(row, formatter);

                    if (!StringUtils.hasText(text) || !StringUtils.hasText(category) || difficulty == null) {
                        failCount.incrementAndGet();
                        log.warn("[Excel upload skipped] Row {} has invalid data", row.getRowNum());
                        continue;
                    }

                    var card = new Card(null, text.trim(), category.trim(), difficulty);
                    cardRepository.save(card);
                    successCount.incrementAndGet();
                    log.info("[Excel upload success] Card with text: [{}] saved", card.getText());
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    log.error("[Excel upload failed] Row {} could not be processed", row.getRowNum(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process excel file [{}]", file.getOriginalFilename(), e);
            return new UploadCardsResponse(FAILED, 0, 0);
        }

        int success = successCount.get();
        int fail = failCount.get();
        log.info("Excel file [{}] processed. Success: {}, Fail: {}", file.getOriginalFilename(), success, fail);
        if (success == 0 && fail == 0) {
            return new UploadCardsResponse(NOTHING_LOADED, 0, 0);
        }
        return new UploadCardsResponse(getMessageType(success, fail), success, fail);
    }

    private Integer parseDifficulty(Row row, DataFormatter formatter) {
        String difficultyValue = formatter.formatCellValue(row.getCell(2));
        if (!StringUtils.hasText(difficultyValue)) {
            return null;
        }
        try {
            return Integer.parseInt(difficultyValue.trim());
        } catch (NumberFormatException e) {
            log.warn("[Excel upload skipped] Difficulty value [{}] is not a number", difficultyValue);
            return null;
        }
    }

    private boolean isRowEmpty(Row row, DataFormatter formatter) {
        return !StringUtils.hasText(formatter.formatCellValue(row.getCell(0)))
                && !StringUtils.hasText(formatter.formatCellValue(row.getCell(1)))
                && !StringUtils.hasText(formatter.formatCellValue(row.getCell(2)));
    }

    private boolean isHeaderRow(Row row, DataFormatter formatter) {
        String textHeader = formatter.formatCellValue(row.getCell(0));
        String categoryHeader = formatter.formatCellValue(row.getCell(1));
        String difficultyHeader = formatter.formatCellValue(row.getCell(2));
        return "text".equalsIgnoreCase(textHeader)
                && "category".equalsIgnoreCase(categoryHeader)
                && "difficulty".equalsIgnoreCase(difficultyHeader);
    }

    private UploadMessageType getMessageType(int successCount, int failCount) {
        if (failCount == 0) return SUCCESS;
        return successCount != 0 ? LOADED_WITH_FAILS : FAILED;
    }

}