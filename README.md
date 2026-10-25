# Анализатор страниц

[![Hexlet Check](https://github.com/voronovyyyyy/java-project-72/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/voronovyyyyy/java-project-72/actions/workflows/hexlet-check.yml)
[![Java CI](https://github.com/voronovyyyyy/java-project-72/actions/workflows/ci.yml/badge.svg)](https://github.com/voronovyyyyy/java-project-72/actions/workflows/ci.yml)
[![JaCoCo coverage](https://img.shields.io/badge/JaCoCo-coverage_%E2%89%A5_80%25-brightgreen)](https://github.com/voronovyyyyy/java-project-72/actions/workflows/ci.yml)

Page Analyzer сохраняет адреса сайтов и проверяет их доступность и SEO-разметку:
HTTP-код ответа, первый заголовок `h1`, `title` и `meta description`.

**Приложение:** [page-analyzer-java-72.onrender.com](https://page-analyzer-java-72.onrender.com)

## Стек

Java 21, Gradle 9.7 (Wrapper), Javalin 7, JTE, Tailwind CSS 4 и
`@tailwindcss/forms`, JDBC / HikariCP, H2 и PostgreSQL, Unirest и jsoup.
Тесты: JUnit, Javalin Testtools, AssertJ, MockWebServer и JaCoCo.
Форматирование и проверки стиля: Spotless / google-java-format.

## Установка

Нужны JDK 21, Node.js 22+ с npm и GNU Make. Отдельно устанавливать Gradle
для сборки не обязательно: в проекте есть Gradle Wrapper.
Для работы команды `gradle` глобально установите Gradle версии от 8.7.
Переменная `JAVA_HOME` должна указывать на JDK 21.

```sh
git clone https://github.com/voronovyyyyy/java-project-72.git
cd java-project-72
make setup
```

`make setup` переходит в каталог `app`, устанавливает зависимости из
`package-lock.json`, собирает Tailwind и выполняет `./gradlew installDist`.
Эту же цель вызывает автопроверка.

## Запуск

Режим разработки — шаблоны и статика читаются из исходников, стили
пересобираются автоматически, включено логирование запросов:

```sh
make start
```

Откройте [localhost:3000](http://localhost:3000). По умолчанию приложение
использует H2 в памяти с именем `project`; после перезапуска данные исчезают.

Запуск готового дистрибутива:

```sh
cd app
./build/install/app/bin/app
```

В PowerShell используйте `build\install\app\bin\app.bat`.
Цели Make учитывают Windows и запускают `gradlew.bat` и `npm.cmd`.

Для сборки одного исполняемого JAR:

```sh
cd app
./gradlew shadowJar
java -jar build/libs/app-1.0-SNAPSHOT-all.jar
```

Для запуска нужен именно JDK: JTE компилирует шаблоны во время работы.

## Переменные окружения

| Переменная | Значение по умолчанию | Назначение |
| --- | --- | --- |
| `PORT` | `3000` | Порт HTTP-сервера |
| `APP_ENV` | `production` | `development` включает чтение шаблонов и статики из `app/src` |
| `JDBC_DATABASE_URL` | `jdbc:h2:mem:project;DB_CLOSE_DELAY=-1` | Подключение к базе |

Пример подключения PostgreSQL:

```sh
export JDBC_DATABASE_URL='jdbc:postgresql://localhost:5432/page_analyzer?user=postgres&password=password'
export PORT=3000
```

Также поддерживается строка подключения Render
`postgresql://user:password@host/database`: она преобразуется в настройки JDBC.
Файл `.env.example` служит примером; приложение не загружает `.env` автоматически.

Схема `app/src/main/resources/schema.sql` создаётся при старте без удаления
существующих данных. Параметризованные JDBC-запросы находятся в репозиториях.
Уникальность имён и связь проверок с URL обеспечиваются ограничениями базы.

## Возможности и маршруты

| Метод | Путь | Действие |
| --- | --- | --- |
| GET | `/` | Форма добавления сайта |
| POST | `/urls` | Валидация, нормализация и добавление URL |
| GET | `/urls` | Сайты с датой и кодом последней проверки |
| GET | `/urls/{id}` | Информация о сайте и история его проверок |
| POST | `/urls/{id}/checks` | Запуск HTTP-запроса и SEO-анализа |

Сохраняются протокол, домен и явно указанный порт; путь, query и fragment
отбрасываются. Повторное добавление открывает уже существующий сайт.
Некорректный URL возвращает `422` и ту же форму без редиректа.
Оповещения выводятся как flash-сообщения в элементе `role="alert"`.

Проверки с HTTP-ошибкой `4xx/5xx` или ошибкой соединения не сохраняются.
Новые URL и проверки выводятся первыми. SEO-данные сохраняются полностью;
в таблице показываются первые 200 символов и `...` при превышении длины.
HTML из проверяемого сайта экранируется шаблонизатором.

## Проверки

```sh
make test
make lint
make lint-fix
make build
```

`make test` собирает CSS, запускает тесты и формирует отчёты JaCoCo:
`app/build/reports/jacoco/test/html/index.html` и
`app/build/reports/jacoco/test/jacocoTestReport.xml`.

CI выполняет `make setup`, Spotless, все тесты, проверку покрытия от 80%
и сборку исполняемого JAR. Бейдж покрытия отражает этот обязательный порог;
измеренные проценты доступны в отчёте каждого запуска CI.
Отчёты тестирования и покрытия сохраняются в Artifacts.

Тесты принудительно используют отдельную H2 `project_test`, сбрасывают схему
перед каждым сценарием и не подключаются к продакшену. Удалённые сайты
имитирует локальный MockWebServer: внешних сетевых запросов в тестах нет.

## Деплой на Render

В репозитории есть `Dockerfile` и `render.yaml`.
Образ собирает Tailwind и JAR, затем запускает приложение на JDK 21.
Веб-сервис и PostgreSQL должны находиться в одном регионе.
`JDBC_DATABASE_URL` передаётся через настройки Render, без секретов в Git.

Для нового окружения можно создать Blueprint из `render.yaml`.
Схема автоматически создаётся при первом старте; повторный деплой
сохраняет данные PostgreSQL.

Текущая бесплатная PostgreSQL на Render действует до **24 ноября 2026 года**.
Для сохранения данных после этой даты потребуется заранее продлить хранение
на подходящем тарифе или перенести базу. Бесплатный веб-сервис может засыпать
при отсутствии запросов, поэтому первое открытие иногда занимает больше времени.
