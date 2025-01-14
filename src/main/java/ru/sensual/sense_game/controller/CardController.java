package ru.sensual.sense_game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.sensual.sense_game.model.Card;
import ru.sensual.sense_game.model.request.UploadCardRequest;
import ru.sensual.sense_game.model.response.UploadCardsResponse;
import ru.sensual.sense_game.service.CardService;

import java.util.List;

import static ru.sensual.sense_game.constant.ExampleObject.UPLOAD_EXAMPLE_OBJECT;
import static ru.sensual.sense_game.model.type.UploadMessageType.NOTHING_LOADED;

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

    @PostMapping
    @Operation(
            summary = "Загрузить карточки",
            description = "Позволяет загрузить карточки из JSON объектов",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "JSON объект, содержащий список карточек",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadCardRequest.class),
                            examples = @ExampleObject(
                                    name = "Пример запроса на загрузку карточек",
                                    value = UPLOAD_EXAMPLE_OBJECT
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Карточки успешно загружены",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UploadCardsResponse.class)))
            }
    )
    public UploadCardsResponse uploadCardsContent(@RequestBody UploadCardRequest uploadCardRequest) {
        return cardService.uploadCardsContent(uploadCardRequest);
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
        return new UploadCardsResponse(NOTHING_LOADED, 0, 0); //@TODO нужно заимплементить
    }
}
