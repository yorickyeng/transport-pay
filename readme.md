# Transport Pay System

Единый репозиторий системы авторизации и управления транспортными картами. Проект объединяет REST API бэкенд и SPA фронтенд.

## 🛠 Технологический стек

* **Frontend:** Kotlin Multiplatform, Jetpack Compose HTML, Skiko (WASM)
* **Backend:** Golang 1.22, SQLite, Gin Web Framework
* **Инфраструктура:** Docker (Multi-stage build), Nginx, Supervisord

---

## 🚀 Запуск в Production (Docker)

Проект упаковывается в единый независимый контейнер. Nginx раздает статику фронтенда и проксирует запросы к Go-бэкенду.

**1. Сборка образа**
В корневой папке проекта выполните:

```bash
docker build -t transport-pay-full .

```

**2. Запуск контейнера**

```bash
docker run -d -p 8888:8888 --name transport-pay transport-pay-full

```

**Доступные адреса:**

* Клиентское приложение: [http://localhost:8888](https://www.google.com/search?q=http://localhost:8888)
* Документация API (Swagger): [http://localhost:8888/api/v1/swagger/index.html](https://www.google.com/search?q=http://localhost:8888/api/v1/swagger/index.html)

---

## 💻 Локальная разработка (Dev Mode)

Для написания кода используются локальные сервера с поддержкой горячей перезагрузки (Hot Reload).

### Backend (Go)

Перейдите в директорию бэкенда, установите зависимости и запустите сервер:

```bash
cd backend
go mod download
go run cmd/api/main.go

```

### Frontend (Kotlin/JS)

Запускается через Webpack Dev Server.

> **Важно:** Перед локальным запуском убедитесь, что в файле `ApiClient.kt` переменная `baseUrl` указывает на абсолютный локальный адрес вашего запущенного бэкенда (например, `http://localhost:8080/api/v1`), а не на относительный путь.

```bash
cd frontend
./gradlew jsBrowserDevelopmentRun

```

---

## 📁 Структура репозитория

* `/frontend` — Исходный код пользовательского интерфейса (Kotlin).
* `/backend` — Исходный код API, файлы миграций базы данных и автосгенерированная документация Swagger.
* `/deploy` — Инфраструктурные конфигурации (`nginx.conf`, `supervisord.conf`) для контейнеризации.
* `Dockerfile` — Манифест многоэтапной сборки проекта.