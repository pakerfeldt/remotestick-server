---
layout: start
title: Timestamp issue
backbutton: true
---

h2. {{ page.title }}

If you get an error directing you to this page it typically means you have an issue with the time on your devices. Make sure the time is correctly set on all your devices communicating with Telldus Live!. First, make sure that you are using the correct time zone, then ensure the time is correctly set.

If one request to Remotestick is done using a timestamp, let's say 2012-12-31 09:00:00, then Telldus Live! will reject any new request which has a lower timestamp. This means that if you have two devices where one device is ahead (or behind) in time, you will have problems. However, Telldus Live! allows for (as of writing this) 10 minutes skew. This means that you can do a second successful request with the time 2012-12-31 08:50:00, even though it is behind in time.

Also note this. Let's say your device is ahead of time with one day and you are sucessfully communicating with Telldus Live!. If you device to correct your time on the device you will not be able to communicate with Telldus Live! until one day (in this example) have passed. You can get around this by contacting Telldus support and ask them to reset your API timestamp.
  