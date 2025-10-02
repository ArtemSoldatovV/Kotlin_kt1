# Kotlin_kt2
## Запуск

1. Склонировать репозиторий
2. Построить проект: ./gradlew build
3. Запустить сервер: Сервер будет доступен на `http://localhost:8080`

## Маршруты:
- `GET /items` — получить все элементы
- `GET /items/{id}` — получить элемент по id (path параметр)
- `POST /adding_a_password вести пароль (JSON тело запроса, формат Passwod_html - {passwod: String})
- `POST /items` — добавить элемент (JSON тело запроса, формат Item - {id: Int, name: String, passwod: String})
- `DELETE /items?id={id}` — удалить элемент по id (query параметр) (JSON тело запроса, формат Passwod_html - {passwod: String})
