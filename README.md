# Async-Helper

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper)
[![Build Status](https://travis-ci.org/loganathan001/AsyncHelper.svg?branch=master)](https://travis-ci.org/loganathan001/AsyncHelper)
[![Coverage Status](https://coveralls.io/repos/github/loganathan001/AsyncHelper/badge.svg?branch=master)](https://coveralls.io/github/loganathan001/AsyncHelper?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a2fef06aa2e946ca86a5ea05fbfccdc3)](https://www.codacy.com/app/loganathan001/AsyncHelper?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=loganathan001/AsyncHelper&amp;utm_campaign=Badge_Grade)
[![HitCount](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper.svg)](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/loganathan001/AsyncHelper/issues)

Async-Helper is a Java utility to invoke/schedule tasks or fetch data asynchronously using tags/flags in a functional way. This internally utilizes ForkJoin pool to submit the tasks.

This contains various helper classes such as  [Async](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Async.java), [AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java), [AsyncSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncSupplier.java), [SchedulingTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingTask.java) and [SchedulingSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingSupplier.java) to perform various asynchronous operations.

Please refer to the [JavaDocs](http://www.javadoc.io/doc/org.vishag/async-helper/3.0.1)  also.  

### Below are the some of the operations that can be perfomed using this utility:
1. Submitting one or more Runnable(s) to run asynchronously.
2. Submitting one or more Supplier(s) to fetch some data asynchronously, which can be then obtained by a tags(key made of one or more Objects)
4. Wait for some flag in one thread until that flag is notified in another thread.
3. Schedule Runnable(s) and Supplier(s) one time or rotating until a flag.
5. Some of the above operations also support option to submit/schedule asynchronously and then wait untill all asynchronous tasks are compete.


Please look into the <a href="https://github.com/loganathan001/AsyncHelper/tree/master/Project/asynchelper/src/test/java/org/vishag/async">Unit tests</a> for all the use-cases and examples.

Also refer to the <a href="https://github.com/loganathan001/AsyncHelper/wiki/Some-Example-Uses-of-AsyncHelper">Wiki page</a> for some example usages.

### To install the latest version, add the below pom dependency entry:
```
<dependency>
  <groupId>org.vishag</groupId>
  <artifactId>async-helper</artifactId>
  <version>3.0.1</version>
</dependency>
```
## Some Example Usages of Async-Helper
<br>
If it is desired to run a set of method calls or code blocks asynchronously, the **[Asyn-Helper](https://github.com/loganathan001/AsyncHelper)** library includes an useful helper method **[AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java).submitTasks** as in below snippet.

```
AsyncTask.submitTasks(
    () -> getMethodParam1(arg1, arg2),
    () -> getMethodParam2(arg2, arg3)
    () -> getMethodParam3(arg3, arg4),
    () -> {
             //Some other code to run asynchronously
          }
    );
```
If it is desired to wait till all asynchronous codes are completed running, the **AsyncTask.submitTasksAndWait** varient can be used.

Also if it is desired to obtain a return value from each of the asynchronous method call or code block, the **[AsyncSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncSupplier.java).submitSuppliers** can be used so that the result can be then obtained by from the result suppliers array returned by the method. Below is the sample snippet:

```
Supplier<Object>[] resultSuppliers = 
   AsyncSupplier.submitSuppliers(
     () -> getMethodParam1(arg1, arg2),
     () -> getMethodParam2(arg3, arg4),
     () -> getMethodParam3(arg5, arg6)
   );

Object a = resultSuppliers[0].get();
Object b = resultSuppliers[1].get();
Object c = resultSuppliers[2].get();
```

These result can be then passed to the myBigMethod as below.

```
myBigMethod(a,b,c);
```

If the return type of each method differ, use the below kind of snippet.

```
Supplier<String> aResultSupplier = AsyncSupplier.submitSupplier(() -> getMethodParam1(arg1, arg2));
Supplier<Integer> bResultSupplier = AsyncSupplier.submitSupplier(() -> getMethodParam2(arg3, arg4));
Supplier<Object> cResultSupplier = AsyncSupplier.submitSupplier(() -> getMethodParam3(arg5, arg6));

myBigMethod(aResultSupplier.get(), bResultSupplier.get(), cResultSupplier.get());
```

The result of the asynchronous method calls/code blocks can also be obtained at a different point of code in the same thread or a different thread as in the below snippet.

```
AsyncSupplier.submitSupplierForSingleAccess(() -> getMethodParam1(arg1, arg2), "a");
AsyncSupplier.submitSupplierForSingleAccess(() -> getMethodParam2(arg3, arg4), "b");
AsyncSupplier.submitSupplierForSingleAccess(() -> getMethodParam3(arg5, arg6), "c");


//Following can be in the same thread or a different thread
Optional<String> aResult = AsyncSupplier.waitAndGetFromSupplier(String.class, "a");
Optional<Integer> bResult = AsyncSupplier.waitAndGetFromSupplier(Integer.class, "b");
Optional<Object> cResult = AsyncSupplier.waitAndGetFromSupplier(Object.class, "c");

 myBigMethod(aResult.get(),bResult.get(),cResult.get());
```
