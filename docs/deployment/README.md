# Руководство по деплою

## Подготовка к деплою

### Сборка приложения

Для создания исполняемого JAR файла выполните:

```bash
cd server
mvn clean package
```

Это создаст JAR файл в `server/target/translator-with-educational-functions-1.0.0.jar`

### Установка зависимостей на сервере

Убедитесь, что на сервере установлены:

- Java 17+
- PostgreSQL (или другой поддерживаемый SQL сервер)
- (Опционально) Docker и Docker Compose

## Локальный деплой

### Запуск JAR файла

```bash
java -jar server/target/translator-with-educational-functions-1.0.0.jar
```

### Запуск с кастомными настройками

```bash
java -Dspring.profiles.active=production -jar server/target/translator-with-educational-functions-1.0.0.jar
```

## Docker деплой

### Сборка Docker образа

Создайте Dockerfile в корне проекта:

```Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY server/target/translator-with-educational-functions-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Соберите образ:

```bash
docker build -t translator-app .
```

Запустите контейнер:

```bash
docker run -p 8080:8080 -e DB_URL=jdbc:postgresql://db:5432/translator -e DB_USERNAME=user -e DB_PASSWORD=password translator-app
```

### Docker Compose

Пример docker-compose.yml:

```yaml
version: '3.8'
services:
  db:
    image: postgres:14
    environment:
      POSTGRES_DB: translator_db
      POSTGRES_USER: translator_user
      POSTGRES_PASSWORD: translator_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/translator_db
      SPRING_DATASOURCE_USERNAME: translator_user
      SPRING_DATASOURCE_PASSWORD: translator_password
    depends_on:
      - db

volumes:
  postgres_data:
```

Запустите с помощью:

```bash
docker-compose up -d
```

## Cloud деплой

### Heroku

1. Установите Heroku CLI
2. Авторизуйтесь: `heroku login`
3. Создайте приложение: `heroku create your-app-name`
4. Добавьте PostgreSQL: `heroku addons:create heroku-postgresql:hobby-dev`
5. Установите Heroku JAR buildpack: `heroku buildpacks:set heroku/java`
6. Деплой: `git push heroku main`

### AWS

1. Загрузите JAR файл на EC2 инстанс
2. Установите Java 17
3. Запустите приложение в background:

```bash
nohup java -jar translator-with-educational-functions-1.0.0.jar > app.log 2>&1 &
```

### Настройка production среды

В production среде рекомендуется использовать следующие настройки в application.properties:

```properties
# Production database configuration
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/translator_db}
spring.datasource.username=${DB_USERNAME:translator_user}
spring.datasource.password=${DB_PASSWORD:translator_password}

# JPA Production settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Server settings
server.error.include-message=never
server.error.include-binding-errors=never

# Logging
logging.level.root=INFO
logging.level.com.translator.main=INFO

# Security
management.endpoints.web.exposure.exclude=*
```

## Мониторинг и логирование

Приложение включает Spring Boot Actuator для мониторинга. Для настройки:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=never
```

## Ротация логов

Для настройки ротации логов создайте logback-spring.xml:

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/app.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/app.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## Backup и восстановление

### Базы данных

Для резервного копирования PostgreSQL:

```bash
pg_dump -U translator_user -h localhost translator_db > backup_$(date +%Y%m%d_%H%M%S).sql
```

Для восстановления:

```bash
psql -U translator_user -h localhost translator_db < backup_file.sql
```