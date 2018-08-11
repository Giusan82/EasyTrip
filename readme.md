# EasyTrip

<p>App created for Udacity.com in the course: Google Challenge Scholarship: Android Developer Nanodegree Program.
<br>
<i>Skill level:</i> <b>Advanced</b>

## Project Overview
This project consists to make an app using this [sample mock](https://d17h27t6h515a5.cloudfront.net/topher/2017/March/58dee986_bakingapp-mocks/bakingapp-mocks.pdf), taking it from a functional state to a production-ready state. 
This will involve finding and handling error cases, adding accessibility features, allowing for localization, adding a widget, and adding a library.

## App Description
App that will allow a user to select a recipe and see video-guided steps for how to complete it.
[The recipe listing is located here.](https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json)



## What I Learned

* Use MediaPlayer/Exoplayer to display videos.
* Handle error cases in Android.
* Add a widget to your app experience.
* Leverage a third-party library in your app.
* Use Fragments to create a responsive design that works on phones and tablets.



# Rubric

### Required Components

* App is written solely in the Java Programming Language
* App utilizes stable release versions of all libraries, Gradle, and Android Studio.
* App should display recipes from provided network resource. 
* App should allow navigation between individual recipes and recipe steps.
* App uses RecyclerView and can handle recipe steps that include videos or images.
* App conforms to common standards found in the [Android Nanodegree General Project Guidelines.](http://udacity.github.io/android-nanodegree-guidelines/core.html) 
* Application uses Master Detail Flow to display recipe steps and navigation between them.
* Application uses Exoplayer to display videos.
* Application properly initializes and releases video assets when appropriate.
* Application should properly retrieve media assets from the provided network links. It should properly handle network requests.
* Application makes use of Espresso to test aspects of the UI.
* Application sensibly utilizes a third-party library to enhance the app's features. That could be helper library to interface with ContentProviders if you choose to store the recipes, a UI binding library to avoid writing findViewById a bunch of times, or something similar.
* Application has a companion homescreen widget.
* Widget displays ingredient list for desired recipe.
