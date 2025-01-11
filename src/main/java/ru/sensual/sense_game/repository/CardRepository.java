package ru.sensual.sense_game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sensual.sense_game.model.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
}