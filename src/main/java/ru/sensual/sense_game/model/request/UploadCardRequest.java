package ru.sensual.sense_game.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sensual.sense_game.model.Card;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UploadCardRequest {
    @Schema(description = "Карточки для загрузки", defaultValue = "[]")
    private List<Card> cards = new ArrayList<>();
}
