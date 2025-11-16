# Руководство по тестированию

## Обзор

Наше приложение использует многоуровневую стратегию тестирования, включающую unit-тесты, интеграционные тесты и E2E тесты.

## Типы тестов

### Unit-тесты

Unit-тесты тестируют отдельные компоненты приложения изолированно. Они находятся в `server/src/test/java/com/translator/main/`.

**Фреймворки:**
- JUnit 5 для основной структуры тестов
- Mockito для мокирования зависимостей
- TestNG для некоторых тестов

**Пример unit-теста:**
```java
@Test
@DisplayName("Проверка корректного перевода текста")
void testTranslateAndSave() {
    // given
    String originalText = "Hello";
    String sourceLang = "en";
    String targetLang = "ru";
    
    Translation expectedTranslation = new Translation();
    expectedTranslation.setOriginalText(originalText);
    expectedTranslation.setTranslatedText("Привет");
    
    when(translationRepository.save(any(Translation.class))).thenReturn(expectedTranslation);

    // when
    Translation result = translationService.translateAndSave(originalText, sourceLang, targetLang);

    // then
    assertEquals("Привет", result.getTranslatedText());
    verify(translationRepository, times(1)).save(any(Translation.class));
}
```

### Интеграционные тесты

Интеграционные тесты проверяют взаимодействие между компонентами и внешними системами (база данных, внешние API).

**Аннотации:**
- `@SpringBootTest` для тестирования Spring контекста
- `@DataJpaTest` для тестирования JPA слоя
- `@WebMvcTest` для тестирования контроллеров
- `@TestPropertySource` для тестовых свойств

**Пример интеграционного теста:**
```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TranslationServiceIntegrationTest {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TranslationRepository translationRepository;

    @Test
    @DisplayName("Сохранение перевода в базу данных")
    void testSaveTranslationToDatabase() {
        // given
        String text = "Test translation";
        String sourceLang = "en";
        String targetLang = "ru";

        // when
        Translation result = translationService.translateAndSave(text, sourceLang, targetLang);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getOriginalText()).isEqualTo(text);
        
        List<Translation> translationsInDB = translationRepository.findAll();
        assertThat(translationsInDB).isNotEmpty();
        assertThat(translationsInDB.get(0).getOriginalText()).isEqualTo(text);
    }
}
```

## Структура тестов

```
server/src/test/java/com/translator/main/
├── service/           # Тесты для сервисных классов
│   ├── TranslationServiceTest.java
│   ├── QuizServiceTest.java
│   └── ...
├── controller/        # Тесты для контроллеров (если есть)
├── repository/        # Тесты для репозиториев
└── TranslatorApplicationTests.java  # Основной тест приложения
```

## Запуск тестов

### Maven команды

```bash
# Запуск всех тестов
mvn test

# Запуск конкретного теста
mvn -Dtest=QuizServiceTest test

# Запуск тестов с определенной меткой
mvn -Dtest="*IntegrationTest" test

# Запуск тестов без создания JAR
mvn test -DskipTests=false
```

### IDE

Тесты можно запускать прямо из IDE (IntelliJ IDEA, Eclipse) кликая на зеленый треугольник рядом с тестом или классом тестов.

## Покрытие кода

Мы используем JaCoCo для измерения покрытия кода тестами:

```bash
# Генерация отчета о покрытии
mvn jacoco:prepare-agent test jacoco:report

# Отчет будет доступен в server/target/site/jacoco/
```

Цель: достичь не менее 80% покрытия unit-тестами для бизнес-логики.

## Тестовые данные

### Тестовые профили

Для тестов используем отдельные профили:

`src/test/resources/application-test.properties`:
```properties
# Использование in-memory базы данных для тестов
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=create-drop

# Отключение внешних API для тестов
ai.gemini.apiKey=test_key
```

### Фикстуры

Для подготовки тестовых данных используем JUnit 5 `@BeforeEach` и `@AfterEach`:

```java
@BeforeEach
void setUp() {
    // Подготовка тестовых данных
    MockitoAnnotations.openMocks(this);
    
    // Инициализация тестовых объектов
    quizRequest = new QuizRequest();
    quizRequest.setOriginalText("Hello");
    quizRequest.setTranslatedText("Привет");
    quizRequest.setSourceLang("en");
    quizRequest.setTargetLang("ru");
}

@AfterEach
void tearDown() {
    // Очистка после теста
    reset(mockService);
}
```

## Тестирование API

Для тестирования REST API используем `MockMvc`:

```java
@WebMvcTest(TranslationController.class)
class TranslationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TranslationService translationService;

    @Test
    void shouldReturnAllTranslations() throws Exception {
        // given
        List<Translation> translations = Arrays.asList(
            new Translation("Hello", "Привет", "en", "ru"),
            new Translation("World", "Мир", "en", "ru")
        );
        when(translationService.getAllTranslations()).thenReturn(translations);

        // when & then
        mockMvc.perform(get("/api/translations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].originalText", is("Hello")))
            .andExpect(jsonPath("$[1].originalText", is("World")));
    }
}
```

## Тестирование Frontend

Для тестирования frontend (HTML, CSS, JavaScript) рекомендуется использовать:

- Jest для unit-тестирования JavaScript
- Selenium WebDriver для E2E тестирования
- Cypress для интеграционного тестирования frontend

## Лучшие практики тестирования

1. **Именование тестов**: используем формат `methodName_expectedBehavior_condition`
2. **AAA Pattern**: Arrange, Act, Assert
3. **Тестирование одного поведения за раз**
4. **Изолированность тестов**: каждый тест должен быть независим
5. **Быстродействие**: тесты должны выполняться быстро
6. **Детерминированность**: тест должен давать одинаковый результат при каждом запуске
7. **Читаемость**: название теста должно описывать, что именно тестируется
8. **Тестирование граничных условий**: минимальные и максимальные значения, null, пустые строки

## Тестирование бизнес-логики

Для тестирования сложных бизнес-процессов используем:

- Parameterized Tests для тестирования с разными наборами данных
- Exception Testing для проверки корректной обработки ошибок
- Property-based Testing (через junit-quickcheck) для тестирования с случайными данными

## Отчеты о тестировании

После выполнения тестов Maven генерирует отчеты:

- `server/target/surefire-reports/` - отчеты о результатах тестов
- `server/target/site/jacoco/` - отчет о покрытии кода