# --- Этап 1: Сборка фронтенда ---
FROM gradle:8.5-jdk17 AS frontend-builder
WORKDIR /app/frontend
# Копируем всё содержимое фронтенда
COPY frontend/ .
# Собираем production-версию. Артефакты появятся в build/dist/js/productionExecutable
RUN ./gradlew jsBrowserProductionWebpack --no-daemon

# --- Этап 2: Сборка бэкенда ---
FROM golang:1.22-alpine AS backend-builder
WORKDIR /app/backend

RUN apk add --no-cache git gcc musl-dev

ENV GOSUMDB=off
ENV GOPROXY=https://goproxy.cn,direct

COPY backend/go.mod backend/go.sum ./
RUN go mod download
COPY backend/ .

RUN go run github.com/swaggo/swag/cmd/swag@latest init -g cmd/api/main.go

RUN CGO_ENABLED=1 GOOS=linux go build -a -o transport-pay-api ./cmd/api

# --- Этап 3: Финальный образ ---
FROM alpine:latest
RUN apk add --no-cache nginx supervisor tzdata

# Создаем нужные директории
RUN mkdir -p /app /var/log/supervisor /var/log/nginx /var/log/api /run/nginx

# Копируем бинарник и миграции из сборщика бэкенда
COPY --from=backend-builder /app/backend/transport-pay-api /app/
COPY --from=backend-builder /app/backend/migrations /app/migrations

# Копируем статику из сборщика фронтенда в директорию Nginx
COPY --from=frontend-builder /app/frontend/build/dist/js/productionExecutable /usr/share/nginx/html

# Копируем конфигурации
COPY deploy/nginx.conf /etc/nginx/nginx.conf
COPY deploy/supervisord.conf /etc/supervisord.conf

# Выставляем права на выполнение
RUN chmod +x /app/transport-pay-api

EXPOSE 8888

# Запускаем менеджер процессов
ENTRYPOINT ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]