# AsyncHelper
<a href="https://github.com/loganathan001/AsyncHelper/blob/master/Project/asyncfetcher/src/main/java/org/ls/asynchelper/AsyncHelper.java">AsyncHelper</a> is a Java utility to invoke/schedule tasks or fetch data asynchronously using tags/flags in a functional way. This internally utilizes ForkJoin pool to submit the tasks.


Below are the some of the operations that can be perfomed using this utility:
1. Submitting one or more Runnable(s) to run asynchronously.
2. Submitting one or more Supplier(s) to fetch some data asynchronously, which can be then obtained by a tags(key made of one or more Objects)
4. Wait for some flag in one thread until that flag is notified in another thread.
3. Schedule Runnable(s) and Supplier(s) one time or rotating until a flag.
5. Some of the above operations also support option to submit/schedule asynchronously and then wait untill all asynchronous tasks are compete.


Please look into the <a href="https://github.com/loganathan001/AsyncHelper/blob/master/Project/asyncfetcher/src/test/java/org/ls/asynchelper/AsyncHelperTest.java">Unit tests</a> for all the use-cases and examples.

To install, add the below pom dependency entry:
```
<dependency>
   <groupId>com.github.loganathan001</groupId>
  <artifactId>async-helper</artifactId>
  <version>1.0.0</version>
</dependency>
```
