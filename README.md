# Phonambula - Корпоративный телефонный справочник

[![CI - Build, Docker & Test](https://github.com/Meta4ora/Phonambula/actions/workflows/ci.yml/badge.svg)](https://github.com/Meta4ora/Phonambula/actions/workflows/ci.yml)
[![Java Version](https://img.shields.io/badge/Java-17-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📖 О проекте

**Phonambula** — это корпоративный веб-справочник сотрудников предприятия, разработанный для оптимизации внутренних коммуникаций и упрощения поиска контактной информации. Система предоставляет удобный интерфейс для просмотра, поиска и управления данными сотрудников с разграничением прав доступа.

### 🎯 Основные возможности

| Модуль | Описание |
|--------|----------|
| **Авторизация и аутентификация** | JWT-аутентификация с ролевой моделью (Администратор / Пользователь) |
| **Управление сотрудниками** | CRUD операции с контактами (добавление, редактирование, удаление) |
| **Поиск с подсветкой** | Мгновенный поиск по всем полям контакта с визуальной подсветкой |
| **Журнал аудита** | Полное логирование всех действий с просмотром JSON-изменений |
| **Экспорт в PDF** | Выгрузка справочника в PDF с форматированием и поддержкой многостраничных документов |
| **Справочники** | Управление должностями, отделами и корпусами |
| **Уведомления** | Всплывающие уведомления о результатах операций |

### 🏗️ Технологический стек

#### Backend
- **Java 17** — основной язык программирования
- **Spring Boot 3.2.5** — фреймворк для создания REST API
- **Spring Security + JWT** — аутентификация и авторизация
- **Spring Data JPA / Hibernate** — ORM для работы с базой данных
- **PostgreSQL 15** — реляционная база данных с поддержкой JSONB
- **Maven** — система сборки и управления зависимостями
- **Docker** — контейнеризация приложения

#### Frontend
- **Vanilla JavaScript** — без использования внешних фреймворков
- **HTML5 / CSS3** — современная адаптивная верстка
- **Flexbox / Grid** — построение адаптивных макетов
- **CSS Animations** — плавные анимации интерфейса
- **html2canvas + jsPDF** — генерация PDF документов

#### DevOps и тестирование
- **GitHub Actions** — CI/CD пайплайн с автоматическим тестированием
- **Docker Compose** — оркестрация контейнеров
- **REST API тесты** — автоматизированное тестирование эндпоинтов
- **PostgreSQL JSONB** — хранение данных аудита

---

## 🚀 Быстрый старт

### Требования к системе

| Компонент | Версия |
|-----------|--------|
| Docker | 20.10+ |
| Docker Compose | 2.0+ |
| Git | 2.0+ |
| Порт 53000 | Свободен |

### Установка и запуск

#### 1. Клонирование репозитория

```bash
git clone https://github.com/Meta4ora/Phonambula.git
cd Phonambula
```

#### 2. Запуск приложения

```bash
docker compose up -d --build
```

**Что произойдет:**
- Сборка Docker образа бэкенд-приложения
- Запуск контейнера PostgreSQL
- Запуск контейнера Spring Boot приложения
- Автоматическое применение миграций базы данных

#### 3. Проверка работоспособности

```bash
# Проверка статуса контейнеров
docker compose ps

# Просмотр логов
docker compose logs -f
```

#### 4. Доступ к приложению

| Сервис | URL | Данные для входа |
|--------|-----|------------------|
| **Главная страница** | http://localhost:53000 | — |
| **Страница входа** | http://localhost:53000/login.html | admin / admin |
| **API документация** | http://localhost:53000/api/ | — |

### Остановка приложения

```bash
# Остановка с удалением контейнеров
docker compose down

# Остановка с удалением томов (очистка БД)
docker compose down -v
```

---

## 🐳 Docker структура

### Архитектура контейнеров

```
┌─────────────────────────────────────────────────────────────┐
│                     Docker Compose                          │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐    ┌─────────────────────────────┐ │
│  │   phonambula-backend │    │   phonambula-db             │ │
│  │   (Spring Boot)      │◄───│   (PostgreSQL 15)           │ │
│  │   Порт: 53000         │    │   Порт: 5432                │ │
│  └─────────────────────┘    └─────────────────────────────┘ │
│           │                                                 │
│           ▼                                                 │
│  ┌─────────────────────┐                                   │
│  │   Frontend (Static)  │                                   │
│  │   HTML/CSS/JS        │                                   │
│  └─────────────────────┘                                   │
└─────────────────────────────────────────────────────────────┘
```

### Конфигурация Docker Compose

```yaml
services:
  backend:
    build: ./server
    ports:
      - "53000:53000"
    depends_on:
      - db
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/phonambula
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=phonambula
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

---

## 🔧 CI/CD и GitHub Actions

### Workflow: CI - Build, Docker & Test

Проект использует **GitHub Actions** для автоматизации сборки, тестирования и проверки качества кода.

#### Триггеры запуска

| Событие | Ветка |
|---------|-------|
| **push** | `main` |
| **pull_request** | `main` |

#### Этапы пайплайна

```mermaid
graph LR
    A[Checkout] --> B[Setup JDK 17]
    B --> C[Build Maven]
    C --> D[Build Docker]
    D --> E[Run Backend]
    E --> F[Wait for Backend]
    F --> G[API Tests]
    G --> H[Cleanup]
```

#### Автоматические тесты

| № | Тест | Описание |
|---|------|----------|
| 1 | GET /api/users | Получение списка пользователей |
| 2 | GET /api/roles | Получение списка ролей |
| 3 | GET /api/buildings | Получение списка зданий |
| 4 | GET /api/divisions | Получение списка отделов |
| 5 | GET /api/posts | Получение списка должностей |
| 6 | GET /api/subscribers/my | Получение абонентов |
| 7 | GET /api/users/me | Получение текущего пользователя |
| 8 | POST /auth/register | Регистрация пользователя |
| 9 | POST /auth/login | Авторизация пользователя |
| 10 | POST /auth/login (неверный пароль) | Проверка ошибки |
| 11 | Доступ без токена | Проверка защиты |
| 12 | Регистрация с дублирующимся логином | Проверка валидации |

#### Статус бейджи

[![CI - Build, Docker & Test](https://github.com/Meta4ora/Phonambula/actions/workflows/ci.yml/badge.svg)](https://github.com/Meta4ora/Phonambula/actions/workflows/ci.yml)

---

## 🌿 Работа с ветками

### Стратегия ветвления

```
main (production)
  │
  ├── feature/auth-module
  ├── feature/audit-module
  ├── feature/pdf-export
  └── hotfix/critical-bug
```

### Правила именования веток

| Тип ветки | Формат | Пример |
|-----------|--------|--------|
| Новая функциональность | `feature/название` | `feature/audit-log` |
| Исправление ошибки | `fix/описание` | `fix/search-highlight` |
| Срочное исправление | `hotfix/описание` | `hotfix/login-crash` |
| Документация | `docs/описание` | `docs/readme-update` |

### Процесс слияния (Pull Request)

1. **Создание ветки** от `main`
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/new-module
   ```

2. **Разработка и коммиты**
   ```bash
   git add .
   git commit -m "feat: add new module functionality"
   git push origin feature/new-module
   ```

3. **Создание Pull Request** через GitHub UI

4. **Проверка CI/CD** — автоматический запуск тестов

5. **Code Review** — проверка кода другим разработчиком

6. **Слияние** — после одобрения и успешных тестов

### Правила коммитов

| Тип | Описание | Пример |
|-----|----------|--------|
| `feat` | Новая функциональность | `feat: add audit log module` |
| `fix` | Исправление ошибки | `fix: resolve search highlighting bug` |
| `docs` | Обновление документации | `docs: update README` |
| `test` | Добавление тестов | `test: add API tests for audit` |
| `refactor` | Рефакторинг кода | `refactor: optimize database queries` |
| `ci` | Изменения CI/CD | `ci: update GitHub Actions workflow` |

---

## 📁 Структура проекта

```
Phonambula/
├── .github/
│   └── workflows/
│       └── ci.yml                    # GitHub Actions CI/CD
├── server/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/server/
│   │   │   │       ├── config/       # Конфигурации (Security, CORS, JWT)
│   │   │   │       ├── controller/   # REST контроллеры
│   │   │   │       ├── model/        # JPA сущности
│   │   │   │       ├── repository/   # JPA репозитории
│   │   │   │       ├── service/      # Бизнес-логика
│   │   │   │       └── util/         # Утилиты (JWT)
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/           # Frontend файлы
│   │   └── test/                     # Unit тесты
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw
├── docker-compose.yml
└── README.md
```

---

## 🧪 Тестирование

### API тесты (автоматические)

```bash
# Запуск через Maven
cd server
./mvnw test

# Запуск через CI/CD (GitHub Actions)
# Автоматически при push в main или PR
```

### Ручное тестирование

| Тест-кейс | Описание |
|-----------|----------|
| TC_AUTH_001 | Успешная авторизация администратора |
| TC_USER_001 | Создание нового сотрудника |
| TC_USER_003 | Удаление сотрудника |
| TC_SEARCH_001 | Поиск по фамилии с подсветкой |
| TC_PDF_001 | Экспорт контактов в PDF |
| TC_AUDIT_001 | Просмотр журнала аудита |

---

## ⚙️ Переменные окружения

### Backend (application.properties)

| Переменная | Значение по умолчанию | Описание |
|------------|----------------------|----------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://db:5432/phonambula` | URL подключения к БД |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Имя пользователя БД |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Пароль БД |
| `JWT_SECRET` | (автогенерируется) | Секрет для JWT токенов |

### Frontend (api.js)

| Параметр | Значение | Описание |
|----------|----------|----------|
| `baseURL` | `http://localhost:53000` | Базовый URL API |

---

## 🛠️ Устранение неполадок

### Проблема: Порт 53000 уже занят

```bash
# Проверка занятых портов
netstat -ano | findstr :53000

# Изменение порта в docker-compose.yml
ports:
  - "8081:53000"
```

### Проблема: Ошибка подключения к БД

```bash
# Проверка статуса контейнера БД
docker compose logs db

# Перезапуск контейнеров
docker compose restart
```

### Проблема: Ошибка сборки Maven

```bash
# Очистка кэша Maven
cd server
./mvnw clean

# Повторная сборка
docker compose up -d --build --no-cache
```

### Проблема: CI/CD пайплайн не запускается

1. Проверьте, что ветка называется `main`
2. Убедитесь, что в репозитории включены GitHub Actions
3. Проверьте файл `.github/workflows/ci.yml`

---

## 📊 Статистика проекта

| Метрика | Значение |
|---------|----------|
| Backend классы | 35+ |
| Frontend функции | 40+ |
| REST эндпоинты | 25+ |
| Таблицы БД | 7 |
| API тестов | 12 |
| Успешность тестов | 100% |
| Время CI/CD пайплайна | ~3 мин |

---

## 📞 Контакты

- **GitHub**: [Meta4ora](https://github.com/Meta4ora)
- **Проект**: [Phonambula](https://github.com/Meta4ora/Phonambula)
