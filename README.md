# 🚀 FreelanceFlow | Production-Grade SaaS Backend

> An industrial-strength Spring Boot backend for the modern Indian freelancer. 

FreelanceFlow is not just another API—it is a **High-Resilience Financial Engine**. It automates the entire lifecycle of a freelance project: from Client onboarding and Invoice generation to AI-Powered Cashflow Forecasting and Automated Payment Tracking via Razorpay.

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Kafka-Event_Driven-orange)](https://kafka.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow)](LICENSE)

---

## 🏗ï¸ Technical Excellence

This project demonstrates **High-Level Backend Engineering** patterns:

*   **🏆 Event-Driven Scaling:** Leveraging **Apache Kafka** for async processing of PDF generation, automated reminds, and AI analytics—ensuring a zero-latency user experience.
*   **🛡ï¸ Distributed Idempotency:** Custom **Redis** protection logic for Razorpay webhooks, ensuring 100% financial accuracy even under duplicate event load.
*   **🧠 Intelligence Layer:** Native integration with **Groq (Llama-3)** for predictive financial risk scoring and localized Indian tax (GST/TDS) advisory.
*   **🔐 Defensive Security:** JWT Refresh-Token rotation, Rate Limiting via **Bucket4j**, and strict CORS/XSS protection.

## 🛠ï¸ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.4, Spring Security |
| **Database** | PostgreSQL 15 + Flyway Migrations |
| **Messaging** | Confluent Kafka |
| **Caching** | Redis 7 |
| **AI** | Groq / Grok API |
| **Documentation** | Swagger / OpenAPI 3.0 |

---

## 📽ï¸ Live Demo & Walkthrough

Check out the full **[System Architecture Blueprint](SYSTEM_DESIGN.md)** for a deep dive into the engineering decisions.

*[Video Walkthrough Coming Soon...]*

---

## 🚀 Quick Start (Dev Environment)

1.  **Clone & Infrastructure:**
    ```bash
    git clone https://github.com/divyapratapdev/freelanceflow.git
    docker-compose up -d
    ```
2.  **Configuration:**
    Copy `.env.example` to `.env` and add your API keys.
3.  **Run:**
    ```bash
    ./mvnw spring-boot:run
    ```

Access the live documentation at: `http://localhost:8080/swagger-ui.html`

## 🤝 Contributing

We build for excellence. Please read our **[Contributing Guide](CONTRIBUTING.md)** and **[Security Policy](SECURITY.md)** before opening a PR.

---
*Built with ❤️ for the Indian Freelance Community.*
