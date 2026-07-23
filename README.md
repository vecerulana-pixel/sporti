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

```bash
./gradlew :app:assembleDebug
./gradlew :app:bundleRelease
./gradlew testDebugUnitTest lintDebug
```

Готовый debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
Готовый release AAB: `app/build/outputs/bundle/release/app-release.aab`. Перед загрузкой в Google Play его необходимо подписать ключом приложения.

## Privacy Policy

The public English-language privacy policy for Google Play is available at:

https://vecerulana-pixel.github.io/sporti/
