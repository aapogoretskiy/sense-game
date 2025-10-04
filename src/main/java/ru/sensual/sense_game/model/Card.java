package ru.sensual.sense_game.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sensual.sense_game.model.type.CardCategory;

@Entity(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Сущность карточки, содержащей текст задания, категорию и уровень сложности")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Уникальный идентификатор карточки", example = "1")
    private Long id;

    @Schema(description = "Текст задания карточки", example = "Сделайте комплимент партнеру")
    private String text;

    @Schema(description = "Категория карточки", example = "Флирт")
    private CardCategory category;

    @Schema(description = "Уровень сложности карточки", example = "3")
    private int difficulty;
}