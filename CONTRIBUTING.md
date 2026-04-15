# Contributing to FreelanceFlow

Thank you for your interest in contributing to **FreelanceFlow**! We welcome contributions that help make this SaaS backend even more robust, secure, and helpful for freelancers.

## 🛠ï¸ Development Standards

To maintain a "God-Level" codebase, we follow strict standards:

- **Clean Architecture:** Keep business logic in Services, not Controllers.
- **RESTful API Patterns:** Use proper HTTP status codes and JSON response structures.
- **Testing:** Every new feature must include JUnit 5 / Mockito unit tests.
- **Migrations:** Never change the database schema without a Flyway migration script.
- **Expert Patterns:** Use async (Kafka) for non-blocking IO and Redis for distributed state.

## 🚀 Getting Started

1.  **Fork the repo** and create your branch from `main`.
2.  **Docker Setup:** Run `docker-compose up -d` to get the infrastructure ready.
3.  **Checkstyle:** Ensure your code follows the Spring Boot style guide.
4.  **Run Tests:** Ensure all tests pass with `mvn test`.

## 🤝 Code of Conduct

Be helpful, stay professional, and focus on technical excellence. We value deep technical discussions that help our community grow.
