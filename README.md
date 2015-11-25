Template Android Project
===========

* Project is separated into two modules. The first one the the `core` module which contains the entity classes representing the JSON structure object, the http calls, and the utility classes. The second module is the `app` which contains the application. The app module implements the core project library.

* Third party libraries have been used, namely: Picasso for image loading, Retrofit for http calls, Robospice for making any http client asynchronous and thread-safe, Retrofit for Rest API, Apache Commons-Net and Commons-Lang for utility methods, and GSON for converting JSON String to Java objects or vice-versa.

* AppCompat support library of android have been used for supporting ActionBar to a wide-range of Android versions and provide APIs to lower versions.

* To build the project, run the command
```groovy
gradle clean assembleDevFlavorBeta
```
or for a release build run
```groovy
gradle clean assembleProdFlavorRelease
```
