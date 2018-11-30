**Edit a file, create a new file, and clone from Bitbucket in under 2 minutes**

The idea of the application is to create a smart baseball bat what allows to register several information about a swing performed by a baseball hitter.

In order to realize this app they were needed both a smartwatch and a smartphone.

- Smartwatch: it aims to detect the information to show to the user and it is mounted on the bat.

- Smartphone: it aims to appropriately show the information recorded by the smartwatch, in a more elaborate and graphically relevant way
 
 These are only the information that is collected by the sensors mounted on the watch during a swing:

- The angle between the bat and the axis parallel to the playing field

- The swing time: start of the turn towards the ball until impact

- The swing force: expressed in Newton is the product of the formula F = ma, where 'a' is the acceleration made by the swing, while 'm' is the sum of the weight of the ball with that of the bat.

For the study of the swing, the signals of 2 sensors were analyzed: accelerometer and geomagnetic rotation vector

The 2 signals were very similar to each other but the accelerometer was clearer and with less noise.

The accelerometric signal produced by a swing is as follows.

Taking into account the direction in which the smartwatch is mounted on the bat, only 2 axes were analyzed on 6, respectively the Y axis for the accelerometer and the X axis for rotation.

To detect the swing on the accelerometric signal, a threshold has been defined above which the acceleration is analyzed, as shown by the red line in the graph.

An increasing monotone sequence of samples and a subsequent decreasing monotone sequence allow to assert that a swing has occurred.

For the purpose of filtering various outliers, a sequence of minimum 3/4 samples for the descent is considered.

Once the swing has been detected, it is sent via the Wearable Data Layer provided by the Google API.

Once the smartphone receives the swing, it saves it in the local database through the DB Manager Service.

The service is the only component of the application that has access to the DB.

The Service also makes use of the AsyncTask to perform the swing recovery (via a SELECT query) in order not to burden the usability latency of the graphical interface. In fact, as we all know, the service is not a separate thread! So when running it does not work the main UI Thread, so if the data and charge recovery time on the UI takes a long time, we could risk that the application is stopped and closed by the Android operating system.

When the AsyncTask is launched it executes the actual query, retrieving the data, and once the sampling is complete, I upload it to the UI. After which it ends.

We used a class called GlobalInfoContainer that extended from Application as a container of global information that was convenient for multiple activities.

So both the task and the async task can modify variables within this class.
---

# Smartphone

---

## Main Activity

On the left there is the Home activity which shows an overall view of the total oscillation performed.

There are two possibilities:

- show the best information about the swing (maximum force, minimum angle and minimum duration)

- show the average of each object

Clicking on the 3 dots on the left shows the menu in the image on the bottom right.

This class uses an Async Task to retrieve the best swing and average swing, including the number of swing from the DB, so as to implement the most modular approach possible. At the end of the recovery of the desired information: ends.
---

##  List Activity

Clicking on the button launches the Calendar activity that contains a CalendarView object that allows you to select the date. The item containing the period and the selected date are passed to the previous activity via an Intent.

In the List Activity there is a ListView object that is attached to an adapter that updates the object with new entries and deletions.

You use an Async Task to populate the list and recover the DB swings because it could be a very expensive operation if there are thousands of swing ... so you have chosen this path for the most modular approach possible.

By clicking on an item it is possible to delete it. Also in this case an asyn task is launched. The Confirmation Dialog is realized through the use of the Fragment technique, for greater compatibility with the IU (it is defined in a special class, while the implementation of the methods launched on the positive or negative response is realized within the activity list.

---

##  Setting Activity

The SharedPreferences are used which save the user preferences shown in the image on a preference file.

Using a fragment for the SharedPreference class, it is also created using Fragments. Selecting a different club changes the multiplicative factor of the acceleration in the calculation of the strength in the Watch class.

Clicking on the preference with the basket appears an Alert Dialog that says asks to confirm to delete all the swings. In this case also an AsyncTask is used here to perform the deletion.

The confirmation Dialog is realized through the use of the Fragment technique, for a greater compatibility with the IU (it is defined a special class, while the implementation of the methods launched on the positive or negative response is realized within the activity list.

---
# The watch has only 2 activities: Main & Measuring.


##  Watch Activity
It is the activity in listening to the swing. To receive the swing from the smartwatch you must have started this activity. When he receives a swing he provides direct access to the service without running any AsyncTask because the operation is very fast ... and does not occupy the GU at all. Each time a swing is received, a confirmation Toast is issued with the measurements! 

In addition, it is checked whether the new measurements are better than the current ones and if not, a notification is issued.

---

##  Main Activity

It's very simple, it contains only buttons. Start send an intent by creating the Measuring activity, by pressing stop you stop sampling and sending data.
---

##  Measuring Activity

It has the class structure of a class that receives events from 2 types of sensors: accelerometer and geomagnetic rotation vector. There are 2 specific management functions for each of the sensors. The complexity to detect a swing is all done with the accelerometer. SI defines a minimum threshold after which one looks at whether a peak is received ie a series of always increasing samples, and the timer is started the first time that a 1st sample is taken above the threshold. When it detects a series of samples of value always describing it is said that the swing has been detected and sent to the smartphone!

If no significant samples are found (above the threshold) for a total of samples, all parameters are reset.

Swiping with the finger from the left to the right you can go back to the main activity to stop the measurements.