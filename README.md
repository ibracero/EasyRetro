# EasyRetro

This is a 100% working app which I use as a playground for the emerging technologies, patterns and practices I feel attracted by.

## Context
This app is a tool for building Sprint retros in an easy, fast and collaborative (realtime) way.

Some considerations for a broader context:
- Authentication is required in order to store the users' retros.
- Users will be able to create as many retros as they want.
- Once the retro is created, the users will be able to share the link of the retro to their colleagues. If the app is not installed, they will be taken to Google Play Store.
- The user who created the retro will be the owner. She will be able to block disable editing when the retro ends.
- A list of users' thumbnails who accessed the retro will be displayed on top of the statements.
- A retro will have three different panels (positive / negative / actions) in order to track the retro statements.
- These panels will update automatically when any of the collaborators add / removes a statement.
- On small screens one panel will be shown at a time. All panels will be shown on tablets (landscape).


![board](https://github.com/ibracero/EasyRetro/blob/master/images/easyretro_tablet_board.png)

## Technologies used
The main technologies I'm using in this project:
- **Firebase Cloud Firestore** for remote realtime database (https://firebase.google.com/docs/firestore) 
- **Firebase Analytics** for analytics (https://firebase.google.com/docs/android/setup)
- **Firebase Crashlytics** for reporting crashes (https://firebase.google.com/docs/crashlytics)
- **Firebase Dynamic Links** for deeplink management (https://firebase.google.com/docs/dynamic-links)
- **Firebase Auth** for session management (https://firebase.google.com/docs/auth)
- **Dagger Hilt** for dependency injection (https://dagger.dev/hilt/). Initially Koin (https://insert-koin.io/), recently migrated to Hilt.
- **Coroutines & Flow** for async communication. *Coroutines* for one shot operations ((https://developer.android.com/kotlin/coroutines) and *Flow* for realtime database changes ((https://developer.android.com/kotlin/flow)
- **Room** for offline support (https://developer.android.com/training/data-storage/room)
- **Arrow** for either encapsulation (https://github.com/arrow-kt/arrow)

## Architecture
- Fragment based navigation using Jetpack Navigation (https://developer.android.com/guide/navigation)
- The arthitecture used in this project is MVI (Model-View-Intent) & Repository pattern.


The data flow would be like this:
![schema](https://github.com/ibracero/EasyRetro/blob/master/images/easyretro_schema.png)
