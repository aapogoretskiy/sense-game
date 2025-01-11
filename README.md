# Sense Game

Sense Game - это бот для Telegram, который предоставляет карточки с заданиями различной сложности и категорий. Он написан на Java с использованием Spring Boot и PostgreSQL в качестве базы данных.

## Особенности
- Telegram-бот, написанный с использованием библиотеки TelegramBots.
- Работа с базой данных PostgreSQL для хранения карточек.
- Миграции базы данных через Flyway.
- Возможность запуска локально с использованием Docker.

## Локальный запуск с Docker

### Предварительные требования
- Установленный Docker и Docker Compose.

### Шаги для запуска

1. Склонируйте репозиторий:
   ```bash
   git clone https://github.com/aapogoretskiy/sense-game
   cd sense-game

2. Соберите приложение с помощью Maven
   ```bash
   mvn clean package
   
3. Запустите Docker Compose:
   ```bash
   docker-compose up --build

### Доступ к приложению
- Приложение будет доступно на http://localhost:8080.
- PostgreSQL будет работать на localhost:5432 с заданными учётными данными (см. docker-compose.yml).

### Переменные окружения
- SPRING_DATASOURCE_URL: URL подключения к базе данных PostgreSQL.
- SPRING_DATASOURCE_USERNAME: Имя пользователя базы данных.
- SPRING_DATASOURCE_PASSWORD: Пароль для базы данных.

### Миграция баз данных
Flyway автоматически применяет миграции при запуске приложения. Скрипты миграций находятся в папке: src/main/resources/db/migration

### Команды Telegram бота
- /start - начать взаимодействие с ботом.
- /get_card - получить случайную карточку из базы данных.
