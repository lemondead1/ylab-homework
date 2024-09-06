Y_LAB homework
==============

Домашняя работа для интенсива Y_LAB.
Проект собирается командой `./gradlew build`.

CarShop-Service
---------------

Реализация сервиса на сервлетах. Предыдущие задания находятся на соответствующих ветках.

Сервер запускается в Докере командой `./gradlew runDocker` на порте 8080.
По умолчанию добавлены сгенерированные данные и пользователь admin с паролем password.
Аутентификация происходит по заголовку Basic.

### Эндпоинты
* `/cars` - добавление автомобиля
  * `POST` - получает в теле JSON вида `{"brand": "Renault", "model": "Captur", "production_year": 2019, "price": 2000000, "condition": "good"}` и возвращает созданный автомобиль.
* `/cars/search` - поиск автомобилей
  * `POST` - получает в теле JSON-запрос вида `{"brand": "Ren", "model": "cap", "production_year": {"min": 2010, "max": 2020}, "price": {"min": 1000000}, "condition": "o"}` и возвращает список автомобилей.
* `/cars/{id}` - доступ к автомобилям по id
  * `POST` - изменяет автомобиль по JSON вида `{"price": 1500000, "condition": "poor"}` и возвращает обновленный автомобиль.
  * `GET` - возвращает автомобиль по id.
  * `DELETE` - удаляет автомобиль.
* `/users` - добавление пользователя
  * `POST` - получает JSON вида `{"username": "user", "phone_number": "81234567890", "email": "user@example.com", "password": "password", "role": "client"}` и возвращает созданного пользователя.
* `/users/search` - поиск пользователей
  * `POST` - получает JSON вида `{"username": "u", "phone_number": "812", "purchases": {"min": 0, "max": 3}, "sorting": "username"}` и возвращает список пользователей.
* `/users/{id или me}` - доступ к профилю по id или к своему профилю
  * `POST` - изменяет пользователя по JSON вида `{"phone_number": "88005553535", "password": "new_password"}` и возвращает обновленного пользователя.
  * `GET` - возвращает пользователя по id.
  * `DELETE` - удаляет пользователя по id.
* `/users/{id или me}/orders?sorting={сортировка, напр. latest_first}` - доступ к заказам пользователя
  * `GET` - возвращает список покупок пользователя.
* `/orders` - добавление заказа
  * `POST` - получает в теле JSON вида `{"kind": "purchase", "client_id": 1, "car_id": 1, "comment": "ASAP"}` и возвращает созданный заказ.
* `/orders/search` - поиск заказов
  * `POST` - получает JSON вида `{"dates": {"min": "2020-11-29T11:37:21Z"}, "username": "ad", "car_brand": "ren", "car_model": "cap", "kind": "purchase", "state": ["new"], "sorting": "latest_first"}` и возвращает список заказов
* `/orders/{id}` - доступ к заказам по id
  * `POST` - изменяет заказ по JSON `{"state": "performing", "append_comment": "Arrival on Friday"}` и возвращает обновленный заказ.
  * `GET` - возвращает заказ по id.
  * `DELETE` - удаляет заказ по id.
* `/signup` - Регистрация
  * `POST` - создает нового клиента по JSON `{"username": "username", "phone_number": "+18772274669", "email": "client@example.com", "password": "password"}`
* `/events/search` - поиск событий аудита
  * `POST` - принимает JSON вида `{"types": ["user_logged_in", "user_signed_up"], "dates": {"min": "2020-11-29T11:37:21Z"}, "username": "in", "sorting": "latest_first"}` и возвращает список событий.