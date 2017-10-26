# Async-Helper

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper)
[![Build Status](https://travis-ci.org/loganathan001/AsyncHelper.svg?branch=master)](https://travis-ci.org/loganathan001/AsyncHelper)
[![Coverage Status](https://coveralls.io/repos/github/loganathan001/AsyncHelper/badge.svg?branch=master)](https://coveralls.io/github/loganathan001/AsyncHelper?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a2fef06aa2e946ca86a5ea05fbfccdc3)](https://www.codacy.com/app/loganathan001/AsyncHelper?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=loganathan001/AsyncHelper&amp;utm_campaign=Badge_Grade)
[![HitCount](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper.svg)](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/loganathan001/AsyncHelper/issues)

Async-Helper is a Java utility to invoke/schedule tasks or fetch data asynchronously using tags/flags in a functional way. This internally utilizes ForkJoin pool to submit the tasks.

This contains various utility classes such as [Async](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Async.java), [Submit](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Submit.java), [Schedule](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Schedule.java), [Wait](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Wait.java) and [Notify](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/Notify.java) to perform various asynchronous operations.

Below are the some of the operations that can be perfomed using this utility:
1. Submitting one or more Runnable(s) to run asynchronously.
2. Submitting one or more Supplier(s) to fetch some data asynchronously, which can be then obtained by a tags(key made of one or more Objects)
4. Wait for some flag in one thread until that flag is notified in another thread.
3. Schedule Runnable(s) and Supplier(s) one time or rotating until a flag.
5. Some of the above operations also support option to submit/schedule asynchronously and then wait untill all asynchronous tasks are compete.


Please look into the <a href="https://github.com/loganathan001/AsyncHelper/tree/master/Project/asynchelper/src/test/java/org/vishag/async">Unit tests</a> for all the use-cases and examples.

To install the latest version, add the below pom dependency entry:
```
<dependency>
  <groupId>org.vishag</groupId>
  <artifactId>async-helper</artifactId>
  <version>2.0.0</version>
</dependency>
```
