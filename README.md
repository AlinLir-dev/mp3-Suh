# Android-приложение для случайного воспроизведения MP3-фраз

Да, такое приложение для Samsung (и любых Android-телефонов) можно сделать.

## Что приложение будет уметь

1. **Загрузка MP3-файлов** в приложение через системный выбор файлов.
2. **Автоматическое воспроизведение в случайном порядке**:
   - между запусками пауза **случайная от 5 до 15 минут**;
   - никогда чаще 1 раза в 5 минут;
   - никогда реже 1 раза в 15 минут.
3. **Ручное воспроизведение**:
   - в списке каждый файл имеет кнопку `▶`;
   - нажатие кнопки запускает именно этот звук.

## Рекомендуемый стек

- **Kotlin + Jetpack Compose** (современный UI)
- **Room** (локальная база для списка файлов)
- **WorkManager** (фоновый запуск по расписанию)
- **ExoPlayer / MediaPlayer** (проигрывание mp3)

## Важный нюанс Android

На современных Android нет гарантии, что «точно в секунду» в фоне всё отработает из-за энергосбережения.

Чтобы интервалы 5–15 минут были максимально стабильными, обычно делают:

- Foreground Service (если нужно строго и постоянно),
- уведомление о работе приложения,
- исключение из оптимизации батареи (по желанию пользователя).

## Логика случайного запуска

После каждого воспроизведения:

1. Берём случайную задержку `delayMin = Random(5..15)`.
2. Планируем следующий запуск через `delayMin` минут.
3. На следующем запуске выбираем случайный файл из сохранённых.

Псевдокод:

```kotlin
fun scheduleNextPlayback() {
    val minutes = (5..15).random()
    workManager.enqueue(
        OneTimeWorkRequestBuilder<RandomPlaybackWorker>()
            .setInitialDelay(minutes.toLong(), TimeUnit.MINUTES)
            .build()
    )
}

class RandomPlaybackWorker : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        val file = repository.getRandomFile() ?: return Result.success()
        player.play(file.uri)
        scheduleNextPlayback()
        return Result.success()
    }
}
```

## Экран приложения

- Список всех загруженных MP3:
  - название файла,
  - кнопка `▶ Воспроизвести`.
- Кнопки сверху:
  - `Добавить MP3`,
  - `Старт авто-режима`,
  - `Стоп авто-режима`.

## Следующий шаг

Если хотите, в следующем шаге я могу сгенерировать полностью готовый Android-проект (Kotlin + Compose) с:

- импортом mp3,
- сохранением списка,
- ручным воспроизведением,
- автопроигрыванием с интервалом 5–15 минут.

## GitHub и APK

Я добавил workflow `.github/workflows/android-apk.yml`, который на GitHub Actions собирает debug APK и сохраняет его как артефакт `app-debug-apk`.

### Как загрузить проект в GitHub

1. Создайте пустой репозиторий на GitHub.
2. В этом проекте выполните:

```bash
git remote add origin https://github.com/<ваш_логин>/<имя_репо>.git
git push -u origin work
```

### Как получить APK

1. Откройте вкладку **Actions** в GitHub.
2. Запустите workflow **Build Android APK**.
3. После завершения скачайте артефакт `app-debug-apk`.

Локально APK можно собрать командой:

```bash
gradle :app:assembleDebug
```

Файл будет здесь:

```text
app/build/outputs/apk/debug/app-debug.apk
```
