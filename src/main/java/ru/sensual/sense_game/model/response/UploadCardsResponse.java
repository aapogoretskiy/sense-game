package ru.sensual.sense_game.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sensual.sense_game.model.type.UploadMessageType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadCardsResponse {
    @Schema(description = "Результат обработки запроса", example = "SUCCESS")
    private UploadMessageType message;

    @Schema(description = "Количество успешно сохраненных карточек", example = "10")
    private int successCount;

    @Schema(description = "Количество не сохраненных карточек из-за ошибок", example = "3")
    private int failCount;
}
