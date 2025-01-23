LinkedBeacons app
=================================

This app includes beacon scanning and monitoring functionalities provided by AltBeacon.
However, the user interface is entirely developed using Jetpack Compose, with the exception of 
the PermissionsActivity, which is retained from the AltBeacon library. 
Configuration details are ultimately displayed on a summary screen, where they can be shared with 
another app for further transmission.


requirements for modifying the app
--------------
* Experience with Kotlin syntax.
* Experience with Jetpack Compose.
* Experience with Android development.
* Experience with Android Studio.


Getting Started
---------------
1. Install Android Studio, if you don't already have it.
2. Download this project.
3. Import the project into Android Studio.
4. Build and run this project.
5. If you want to use your own beacon, you need to add the Parser name, layout and manufacturer code
in the DataSource object like the other ones.
6. Scan some beacons and see the results!
