# Trailer-Buzz
* Trailer Buzz is online Video Streaming Application. It helps you view trailers for different movies and recommend similar movie trailers based on your preferences. 
* It has all the necessary features that an online video streaming application needs. People can register and login to the application using their email addresses. 
* The application also recommends similar movie trailers to the user based on the preferences selected by the user during login. 
* It also recommends movies based on similarities between two movies.
* User has the option to update their profile information and also like the videos. Trending vidoes get updated on the basis of likes on vidoes.

## Libraries / Technologies Used

### Firebase

Firebase is Google’s mobile application development platform. It provides developers with a variety of tools and services to help them develop quality apps and grow their user base. Firebase suppores features like realtime database, storage on the cloud, authentication etc. We have used all the three mentioned features in our application. The database supported by firebase is categorized as NoSQL database. It stores data in JSON like formats. We have stored the vidoes and photos on firebase storage and we have also used firebase based authentication in our application.

### Exoplayer

ExoPlayer is an open source project that is not part of the Android framework and is distributed separately from the Android SDK. ExoPlayer is a library that provides an alternative to Android’s MediaPlayer API for playing audio and video both locally and over the Internet. ExoPlayerView is one of the most used UI components in many apps such as YouTube, Netflix, and many video streaming platforms. We have used exoplayer to stream vidoes in our application and we have customized it according to our requirements.

### Volley

Volley is an HTTP library that makes networking for Android apps easier and most importantly, faster. Volley is available on GitHub. To use Volley, you must add the android.permission.INTERNET permission to your app's manifest. Without this, your app won't be able to connect to the network. We have used Volley to fetch results from our API that we have hosted on heroku platform. The API helps us to fetch recommendations of trailers for the users.

### Heroku and Flask

We have a rest API that we have used to fetch recommendations to the users based on the genres as well as similarity of movies. The API is hosted on the heroku platform. Heroku is a cloud platform as a service supporting several programming languages. We have built our API using the different machine learning algorithms and we have used flask, a micro web framework written in Python. It is classified as a microframework because it does not require particular tools or libraries. More of this API can be found on the git repository:

https://github.com/ujwalkumar1995/Movie-Recommendation-API

### Glide

Glide is an Image Loader Library for Android developed by bumptech and is a library that is recommended by Google. We have used Glide to load the thumbnails of the trailers in our application. Glide uses memory by default to avoid unnecessary network requests. By this way we are able to save network resources as thumbnail are loaded from local cache whenever needed.

## Project Structure Description

### HomeActivity

* This activity checks if the user is logged in or not and directs the user to the RegisterActivity or the VideosListActivity.

### LoginActivity

* The activity allows the user to login to his existing account. User can also opt to register by switching over the register screen from this activity.
* User also has the feature to reset his password incase he has forgotten his password.

### RegisterActivity

* User can enter his details like phone number, name email and password to register to the application.
* He can also switch over to the login screen to login to his existing account.

### GenrePreferenceActivity

* This Activity is loaded when user registers to the application for the first time. User has the ability to choose 3 Genres. The user gets recommendation of trailers based on his selection.

### Profile Activity

* This Activity can be accessed using the navigation drawer in the VideosListActivity. User has the ability to update his name, phone number or password in this activity.

### VideoPlayerActivity

* This activity contains the main video player where we are able to watch our videos. 
* The user has the ability to switch to fullscreen mode in this activity. 
* User can also like videos in this activity. 
* The activity displays basic information about the movie like cast, description etc. On the bottom half of the screen the user is also recommended similar movie trailers of the current movie.

### VideosListActivity

* This activity dispalys videos in three different containers. The top most recycler videos in generic order that does not follow any pattern. The middle recycler view recommends videos to the user based on his genre preferences. The third container displays the top like videos. 
* This activity also has a search feature embedded in the action bar that can allow user to search for trailers. 
* There is also a navigation drawer that allows the user to switch to the profile screen and also has a button to logout from the application.
