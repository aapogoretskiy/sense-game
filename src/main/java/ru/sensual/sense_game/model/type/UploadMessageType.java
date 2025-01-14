package ru.sensual.sense_game.model.type;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Типы сообщений о результате загрузки карточек.
 */
@Schema(description = "Типы сообщений о результате загрузки карточек")
public enum UploadMessageType {

    @Schema(description = "Все карточки успешно загружены")
    SUCCESS,

    @Schema(description = "Не удалось загрузить карточки")
    FAILED,

    @Schema(description = "Некоторые карточки были загружены, но часть загрузки завершилась с ошибками")
    LOADED_WITH_FAILS,

    @Schema(description = "Ничего не загружено")
    NOTHING_LOADED
}