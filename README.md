# Audition API

A Spring Boot application built as part of a coding assessment. It demonstrates clean architecture, API error handling,
logging, unit testing, and observability integrations.

---

## ✅ Features

- REST API for interacting with mock post and comment data
- Clean service and client architecture
- Global exception handling using `@ControllerAdvice`
- Structured logging and tracing
- Configurable external API URLs via YAML
- 80%+ test coverage with JUnit and Mockito
- Code quality tools: PMD, SpotBugs, Checkstyle, JaCoCo

---

## 📦 Technologies Used

- Java 17
- Spring Boot 3.x
- Spring Web, Spring MVC
- JUnit 5, Mockito
- Jackson (custom `ObjectMapper`)
- Micrometer Tracing (optional)
- SpotBugs, PMD, Checkstyle
- IntelliJ IDEA for coverage reports

---

## 🚀 How to Run the App

```bash
./gradlew clean build
./gradlew bootRun

```

## Application Starting

![start.png](start.png)

## 📡 Available API Endpoints

You can view and test all available endpoints interactively using Swagger UI:

👉 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## ✅ Test Coverage

![coverage1.png](coverage1.png)
![coverage2.png](coverage2.png)
![coverage3.png](coverage3.png)

## Code Quality Reports

![check1.png](check1.png)