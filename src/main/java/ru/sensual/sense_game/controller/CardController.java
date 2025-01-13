package ru.sensual.sense_game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.model.response.UploadCardsResponse;
import ru.sensual.sense_game.service.CardService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Cards API", description = "API для управления карточками")
public class CardController {

    private final CardService cardService;

    @GetMapping
    @Operation(
            summary = "Получить все карточки",
            description = "Возвращает список всех карточек из базы данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список карточек успешно получен",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Card.class)))
            }
    )
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @GetMapping("/random")
    @Operation(
            summary = "Получить случайную карточку",
            description = "Возвращает случайную карточку из базы данных",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Случайная карточка успешно получена",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Card.class)))
            }
    )
    public Card getRandomCard() {
        return cardService.getRandomCard();
    }

    @PostMapping("/upload/excel")
    @Operation(
            summary = "Загрузить карточки из Excel",
            description = "Позволяет загрузить карточки из Excel файла",
            responses = {
                    @ApiResponse(responseCode = "501", description = "Функционал не реализован",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UploadCardsResponse.class)))
            }
    )
    public UploadCardsResponse uploadCardsExcel(UploadCardsResponse uploadCardsResponse) {
        return new UploadCardsResponse("Not implemented"); //@TODO нужно заимплементить
    }
}
