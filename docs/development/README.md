# Руководство по разработке

## Установка и настройка

### Предварительные требования

- Java 17+ (предпочтительно OpenJDK)
- Apache Maven 3.6+
- PostgreSQL 12+
- Git
- Node.js 14+ (для разработки frontend части)

### Установка зависимостей

1. Клонируйте репозиторий:
```bash
git clone https://github.com/maks2134/maxify.git
cd maxify
```

2. Установите backend зависимости:
```bash
cd server
mvn clean install
```

## Структура проекта

```
maxify/
├── server/                 # Backend часть (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/translator/main/  # Основной код
│   │   │   │   ├── controller/    # REST контроллеры
│   │   │   │   ├── service/       # Бизнес-логика
│   │   │   │   ├── model/         # Модели данных
│   │   │   │   ├── repository/    # Репозитории JPA
│   │   │   │   ├── dto/           # Объекты передачи данных
│   │   │   │   ├── config/        # Конфигурации
│   │   │   │   └── exception/     # Обработка исключений
│   │   │   └── resources/         # Настройки и статические файлы
│   └── pom.xml                    # Зависимости Maven
├── client/                        # Frontend часть
│   └── src/                       # HTML, CSS, JavaScript файлы
└── docs/                          # Документация
```

## Настройка окружения

### База данных

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE translator_db;
CREATE USER translator_user WITH PASSWORD 'translator_password';
GRANT ALL PRIVILEGES ON DATABASE translator_db TO translator_user;
```

### Переменные окружения

Создайте или обновите файл `server/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/translator_db
spring.datasource.username=translator_user
spring.datasource.password=translator_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Gemini API
ai.gemini.apiKey=ваш_ключ_к_API_Gemini
ai.gemini.model=gemini-2.5-flash

# Server Configuration
server.port=8080
```

## Запуск приложения

### Backend

```bash
cd server
mvn spring-boot:run
```

Приложение будет доступно по адресу `http://localhost:8080`

### Frontend

Frontend файлы автоматически обслуживаются Spring Boot приложением из папки `server/src/main/resources/static/`

## Тестирование

### Unit-тесты

```bash
cd server
mvn test
```

### Integration-тесты

```bash
cd server
mvn verify
```

## Код-стайл и форматирование

### Java

- Используем Google Java Style Guide
- Именование классов в PascalCase
- Именование методов и переменных в camelCase
- Используем Lombok для уменьшения boilerplate кода
- Классы с бизнес-логикой аннотируем @Service
- Контроллеры аннотируем @Controller или @RestController

### JavaScript

- Используем современный ES6+ синтаксис
- Именование функций и переменных в camelCase
- Используем const и let вместо var
- Форматируем код с помощью Prettier (при настройке)

## Принципы разработки

### Модульность

- Разделяем приложение на логические модули
- Каждый класс имеет одну ответственность
- Используем паттерны проектирования

### Тестирование

- Пишем unit-тесты для всех бизнес-логик
- Покрываем критические пути интеграционными тестами
- Используем Mockito для мокирования зависимостей

## Паттерны и практики

- Model-View-Controller (MVC) для организации кода
- Dependency Injection через Spring Framework
- JPA Repository для работы с базой данных
- DTO для передачи данных между слоями
- Global Exception Handler для обработки ошибок

## Вклад в проект

1. Форкните репозиторий
2. Создайте feature ветку (`git checkout -b feature/AmazingFeature`)
3. Сделайте изменения
4. Закоммитьте их (`git commit -m 'Add some AmazingFeature'`)
5. Запушьте в ветку (`git push origin feature/AmazingFeature`)
6. Откройте Pull Request