Android - приложение для поставщиков Wildberries
([google play](https://play.google.com/store/apps/details?id=com.automotivecodelab.wbgoodstracker)).
Позволяет отслеживать различную информацию для товаров на маркетплейсе: продажи, остатки на складах,
цены, отзывы, динамику изменения продаж и т.д. В качестве источников данных используется
недокументированный API Wildberries, представляющий собой набор endpoint'ов, "добываемых" с помощью
реверс-инжиниринга сайта Wildberries.ru. Ввиду нестабильности и недокументированности источников,
работа с ними осуществляется на собственном бэкенде, реализующем JSON API для клиентов.

#### Детали реализации
- Clean architecture + MVVM

- UI построен на классических View, с использованием single-activity и фрагментов.

- Многопоточность на Kotlin Coroutines и Kotlin Flows

- Применен offline-first подход с сохранением всех полученных с сервера данных в Room и Jetpack
  DataStore. Локальные хранилища также выступают в качестве single-source-of-truth для приложения,
  автоматически синхронизируя экраны и различные элементы UI между собой. Для доставки данных из
  data - слоя в UI широко применяется реактивный подход с Kotlin Flows.

- Авторизация не является обязательной и включается пользователем в настройках. Авторизация
  реализована с помощью Google OAuth (One Tap sign-in). Для авторизованного пользователя сервер
  хранит добавленные товары, реализуя таким образом механизмы синхронизации между устройствами
  пользователя и создания автоматических бэкапов.

- На главном экране приложения - "стероидный" `RecyclerView` с поиском, различными режимами
  сортировки, выделением по долгому нажатию (`androidx.recyclerview.selection`), группировкой
  элементов в пользовательские группы, анимациями, swipe-to-refresh и swipe-to-dismiss.
  ###### GIF
  <details>
  <summary>Поиск</summary>
  <img src="screenshots/search.gif" width="300">
  </details>
  <details>
  <summary>Сортировка</summary>
  <img src="screenshots/sorting.gif" width="300">
  </details>
  <details>
  <summary>Action mode</summary>
  <img src="screenshots/selection.gif" width="300">
  </details>
  <details>
  <summary>Группы</summary>
  <img src="screenshots/groups.gif" width="300">
  </details>
  <details>
  <summary>Swipe-to-dismiss</summary>
  <img src="screenshots/swipetodismiss.gif" width="300">
  </details>
- Анимации переходов между экранами из Material Motion
  <details>
  <summary>Transitions</summary>
  <img src="screenshots/transitions.gif" width="300">
  </details>
- Навигация реализована через Jetpack Navigation Component.

- DataBinging, LiveData

- Дизайн выполнен на компонентах из Material 3

- DI на Dagger 2

- Взаимодействие с JSON API с помощью Retrofit

- OkHttp - интерсептор для отлавливания и отображения в UI ошибки отсутствия интернет-соединения

- Junit 4 для unit - тестов

- Espresso и UIAutomator - для UI - тестов

- Picasso

- Firebase Crashlytics

- Google Play In-App Reviews

- Splash-screen с использованием xml-темы

- Ktlint

- Темная тема, поддержка альбомной ориентации, `minSdk = 21`

#### Инструкция по сборке
- В файл local.properties добавить строковую переменную SERVER_URL с URL сервера (`https` или
  прописать `usesCleartextTraffic = true` в манифесте).
- В файл local.properties добавить строковую переменную SERVER_CLIENT_ID, представляющий собой
  токен, полученный из Google APIs Console при конфигурации Google OAuth
- В `/app` добавить файл `google-services.json`, полученный при конфигурации firebase-проекта
