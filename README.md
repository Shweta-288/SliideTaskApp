A simple Android application demonstrating user list management with authentication via Bearer tokens, built with Jetpack Compose, Ktor, and Hilt.


Features

List Users: Fetches the last page of users from the API using the X-Pagination-Pages header.

Add User: Opens a dialog to create a new user with name and email; gender and status are set by default.

Delete User: Long-press a user item to delete.

Pull to Refresh: Swipe down to refresh the user list.

Bearer Token Authentication: All HTTP requests include an Authorization: Bearer <token> header.



Setup
1. Clone the repository
   https://github.com/Shweta-288/SliideTaskApp

2. Configure API Token & Base URL

The app reads two BuildConfig fields:

BuildConfig.BEARER_TOKEN (need to add token in local.properties file) 

BuildConfig.BASE_URL (the base URL of the endpoint)

3. Build & Run

Open the project in Android Studio.
Sync Gradle.
Run on an emulator or physical device with internet.


Usage

Launch App: Automatically fetches users.
Pull to Refresh: Swipe down.
Add User: Tap the + FAB, fill in Name and Email, then confirm.
Delete User: Long-press a user card, confirm deletion.


what's not done

not added user creation validation for text field.
not add custom theme used however it is.