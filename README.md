# Sport Tacktick Win

Sport Tacktick Win — нативное Android-приложение для спортивного расписания, новостей, заметок, избранного, тренерского секундомера и персональной аналитики.

## Что реализовано

- Главный экран с секундомером `старт / стоп / круг / сброс` и сохранением завершённых сессий.
- Ближайшие матчи четырёх футбольных лиг через бесплатный TheSportsDB API v1.
- Свежие спортивные новости из официального BBC Sport RSS.
- Офлайн-кэш матчей и новостей в Room.
- Локальные заметки: создание, редактирование и удаление.
- Избранное для матчей и новостей.
- Аналитика по тренировочному времени, сессиям, лучшему кругу, заметкам и избранному.
- Светлая и тёмная темы в фирменной красно-чёрно-белой палитре.
- Firebase-переключение на WebView с сохранением последней открытой страницы, загрузкой файлов, камерой, микрофоном, геолокацией, cookies и DownloadManager.

## Архитектура

```text
app                     — точка входа и навигация
core:domain             — модели и интерфейсы репозиториев (чистый Kotlin)
core:data               — Room, Retrofit, RSS parser, offline-first репозитории, Hilt DI
core:designsystem       — тема, компоненты и векторные иконки
feature:home            — дашборд и секундомер
feature:explore         — матчи и новости
feature:library         — заметки и избранное
feature:analytics       — персональная аналитика
```

Состояние экранов хранится в `StateFlow`, UI построен на Jetpack Compose, зависимости внедряются через Hilt. Локальная база является источником истины, сетевое обновление только дополняет кэш и не затирает пользовательское избранное.

## Источники данных

- [TheSportsDB API v1](https://www.thesportsdb.com/docs_api_guide) — бесплатный ключ `123`, расписание ближайших матчей.
- [BBC Sport RSS](https://feeds.bbci.co.uk/sport/rss.xml) — заголовки, описания и ссылки на оригинальные публикации.

Важно: бесплатный тариф TheSportsDB не предоставляет полноценный 2-минутный live-score. Поэтому приложение показывает реальные ближайшие события и статусы, полученные от API, но не выдаёт расписание за live-трансляцию.

## Сборка

Требования: JDK 17 и Android SDK 36. Приложение компилируется с `compileSdk 36` и нацелено на Android 16 через `targetSdk 36`.

Package ID: `com.tacktikcwin.app`. Firebase Realtime Database ожидает строковое значение в узле `url`; пустое или некорректное значение оставляет нативный интерфейс активным.

```bash
./gradlew :app:assembleDebug
./gradlew :app:bundleRelease
./gradlew testDebugUnitTest lintDebug
```

Готовый debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
Готовый release AAB: `app/build/outputs/bundle/release/app-release.aab`.

## Google Play production draft

Workflow `Google Play production draft` запускается вручную в GitHub Actions. Он собирает и проверяет подписанный AAB, сохраняет его как artifact на 14 дней и создаёт черновик релиза в production-треке Google Play.

Для workflow используются GitHub Actions Secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

Keystore и его пароли не хранятся в репозитории. Для локальной подписанной сборки задайте переменные окружения `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` и `ANDROID_KEY_PASSWORD`.

## Privacy Policy

The public English-language privacy policy for Google Play is available at:

https://vecerulana-pixel.github.io/sporti/
