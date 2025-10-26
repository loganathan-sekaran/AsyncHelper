# Async-Helper

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.vishag/async-helper)
[![Build Status](https://travis-ci.org/loganathan001/AsyncHelper.svg?branch=master)](https://travis-ci.org/loganathan001/AsyncHelper)
[![Coverage Status](https://coveralls.io/repos/github/loganathan001/AsyncHelper/badge.svg?branch=master)](https://coveralls.io/github/loganathan001/AsyncHelper?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a2fef06aa2e946ca86a5ea05fbfccdc3)](https://www.codacy.com/app/loganathan001/AsyncHelper?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=loganathan001/AsyncHelper&amp;utm_campaign=Badge_Grade)
[![HitCount](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper.svg)](http://hits.dwyl.io/loganathan001/loganathan001/AsyncHelper)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/loganathan001/AsyncHelper/issues)

Async-Helper is a Java utility (also an OSGi bundle) to invoke/schedule tasks or fetch data asynchronously using tags/flags in a functional way. This internally utilizes ForkJoin pool to submit the tasks.

This contains various helper classes such as  [AsyncContext](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncContext.java), [AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java), [AsyncSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncSupplier.java), [SchedulingTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingTask.java) and [SchedulingSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingSupplier.java) to perform various asynchronous operations.

Please refer to the [JavaDocs](http://www.javadoc.io/doc/org.vishag/async-helper/4.1.0)  also.  

### Below are the some of the operations that can be perfomed using this utility:
1. Submitting one or more Runnable(s) to run asynchronously.
2. Submitting one or more Supplier(s) to fetch some data asynchronously, which can be then obtained by a tags(key made of one or more Objects)
4. Wait for some flag in one thread until that flag is notified in another thread.
3. Schedule Runnable(s) and Supplier(s) one time or rotating until a flag.
5. Some of the above operations also support option to submit/schedule asynchronously and then wait untill all asynchronous tasks are compete.


Please look into the <a href="https://github.com/loganathan001/AsyncHelper/tree/master/Project/asynchelper/src/test/java/org/vishag/async">Unit tests</a> for all the use-cases and examples.

Also refer to the <a href="https://github.com/loganathan001/AsyncHelper/wiki/Some-Example-Uses-of-Async-Helper">Wiki page</a> for some example usages.

### What is new in Async-Helper-4.1.0

This release introduces **22 new enhanced features** with comprehensive test coverage and documentation:

#### 🎯 Major Enhancements
- **Timeout Support** - Operations with configurable timeouts for better control
- **Batch Processing** - Process collections concurrently with optional concurrency limits
- **Resilience Patterns** - Retry logic, fallback values, and custom error handlers
- **Functional Composition** - Chain operations and combine results elegantly
- **Competitive Execution** - Race conditions and fastest result selection
- **Modern Integration** - CompletableFuture support for seamless integration
- **Monitoring & Control** - Check pending status and cancel operations
- **Multi-Flag Coordination** - Wait for multiple flags with all/any semantics
- **Advanced Scheduling** - Execute N times with exponential backoff support

#### 📚 Documentation
- Comprehensive README with 10+ real-world usage examples
- Full unit test coverage for all 22 new methods
- Migration guide documenting backward compatibility

See the [New Enhanced Features](#new-enhanced-features-latest-update) section below for detailed API documentation and the [Advanced Usage Examples](#advanced-usage-examples) section for comprehensive code examples.

### What is new in Async-Helper-4.0.0

* Async-Helper is an **OSGi bundle** now :), to use it directly in OSGi applications.
* Renamed *Async* helper class to [AsyncContext](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncContext.java) so that there is option to limit the context of Asynchronous operations. The global context can be obtained using `AsyncContext.getDefault()`.

* All the existing helper classes and their methods are now converted from static to instances, so that,

   Either their default instances can be obtained using their *getDefault()* methods, 

   - [AsyncContext](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncContext.java) ==> `AsyncContext.getDefault()`
   - [AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java) ==> `AsyncTask.getDefault()`
   - [AsyncSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncSupplier.java) ==> `AsyncSupplier.getDefault()`
   - [SchedulingTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingTask.java) ==> `SchedulingTask.getDefault()`
   - [SchedulingSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingSupplier.java) ==> `SchedulingSupplier.getDefault()`

   Or they can be instantiated with a specific arguments. 


  -  [AsyncContext](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncContext.java) ==> `AsyncContext.newInstance()`
   - [AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java) ==> `AsyncTask.of(ExecutorService)` or `AsyncTask.of(ExecutorService, AsyncContext)`
   - [AsyncSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncSupplier.java) ==> `AsyncSupplier.of(ExecutorService)` or `AsyncSupplier.of(ExecutorService, AsyncContext)`
   - [SchedulingTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingTask.java) ==> `SchedulingTask.of(ScheduledExecutorService)` or `SchedulingTask.of(ScheduledExecutorService, AsyncContext)`
   - [SchedulingSupplier](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/SchedulingSupplier.java) ==> `SchedulingSupplier.of(ScheduledExecutorService)` or `SchedulingSupplier.of(ScheduledExecutorService, AsyncContext)`

* The default instances of `AsyncTask` and `AsyncSupplier` use a common `ForkJoinPool`. But it is possible to get customized instances of these can be obtained by passing a new `ExecutorService` instance.

* The default instances of `SchedulingTask` and `SchedulingSupplier` use a common `ScheduledExecutorService`. But it is possible to get customized instances of these can be obtained by passing a new `ScheduledExecutorService` instance.

* [AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java) includes a new static helper method `AsyncTask.submitTaskInNewThread(Runnable)` to submit a task by spawning a new thread.

### New Enhanced Features (Latest Update)

Async-Helper now includes **22 powerful new methods** across AsyncSupplier, AsyncContext, SchedulingTask, and SchedulingSupplier classes, providing production-ready features for enterprise applications:

#### 🔹 Timeout Support
- **`submitAndGetWithTimeout`** - Execute and retrieve results with a timeout, preventing indefinite waits
- **`waitForFlagWithTimeout`** - Wait for flags with timeout control in AsyncContext
- **`waitAndGetFromSupplierWithTimeout`** - Retrieve supplier results with timeout protection

#### 🔹 Batch Processing
- **`submitAndProcessAll`** - Process collections in parallel with automatic result aggregation
- **`submitAndProcessAllWithLimit`** - Batch process with concurrency limits to prevent resource exhaustion

#### 🔹 Resilience Patterns
- **`submitSupplierWithRetry`** - Automatic retry with configurable attempts and delays
- **`submitSupplierWithFallback`** - Graceful fallback values when operations fail
- **`submitSupplierWithErrorHandler`** - Custom error handling with recovery logic

#### 🔹 Functional Composition
- **`submitChained`** - Chain async operations with dependent transformations
- **`submitAndCombine`** - Combine multiple async operations into a single result

#### 🔹 Competitive Execution
- **`submitRace`** - Race multiple operations, return the fastest result
- **`submitAndGetFastest`** - Execute multiple suppliers, get the first successful completion

#### 🔹 Modern Java Integration
- **`submitAsCompletableFuture`** - Bridge to Java 8+ CompletableFuture API for advanced composition

#### 🔹 Monitoring & Control
- **`isPending`** - Check if async operations are still running
- **`cancelSupplier`** - Cancel running async suppliers

#### 🔹 Multi-Flag Coordination
- **`waitForAllFlags`** - Wait for multiple flags before proceeding
- **`waitForAnyFlag`** - Proceed when any flag is set

#### 🔹 Advanced Scheduling
- **`scheduleTaskNTimes`** - Execute scheduled tasks exactly N times
- **`scheduleTaskWithBackoff`** - Exponential backoff scheduling for resilient polling
- **`scheduleSupplierNTimes`** - Schedule suppliers N times with result collection

### Internal Code Quality Improvements

The following internal improvements have been made to enhance code quality and reliability. **These changes are fully backward compatible** and require no migration:

#### ✅ Logger Performance Optimization
- Changed logger calls from eager string concatenation to lazy evaluation using lambda suppliers
- Example: `logger.config(() -> e.getMessage())` instead of `logger.config(e.getMessage())`
- **Impact**: Improved performance when logging is disabled, no API changes

#### ✅ Proper Thread Interrupt Handling
- Added `Thread.currentThread().interrupt()` after catching `InterruptedException` 
- Ensures interrupted status is properly restored per Java best practices
- **Impact**: Better thread interrupt propagation, no API changes

#### ✅ Enhanced Immutability
- Made internal fields `final` where appropriate (DEFAULT_INSTANCE, executor, scheduler, asyncContext, etc.)
- **Impact**: Improved thread safety and code reliability, no API changes

#### ✅ Javadoc Corrections
- Fixed class-level Javadoc descriptions for accuracy
- Improved grammar and clarity in documentation
- **Impact**: Better documentation quality, no API changes

**Migration Required**: ❌ **None** - All improvements are internal implementation enhancements that maintain full backward compatibility with existing code.

### To install the latest version, add the below pom dependency entry:
```
<dependency>
  <groupId>org.vishag</groupId>
  <artifactId>async-helper</artifactId>
  <version>4.1.0</version>
</dependency>
```
## Some Example Usages of Async-Helper

If it is desired to run a set of method calls or code blocks asynchronously, the **[Async-Helper](https://github.com/loganathan001/AsyncHelper)** library includes an useful helper method **[AsyncTask](https://github.com/loganathan001/AsyncHelper/blob/master/Project/asynchelper/src/main/java/org/vishag/async/AsyncTask.java).submitTasks** as in below snippet.

```
AsyncTask.getDefault().submitTasks(
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
   AsyncSupplier.getDefault().submitSuppliers(
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
Supplier<String> aResultSupplier = AsyncSupplier.getDefault().submitSupplier(() -> getMethodParam1(arg1, arg2));
Supplier<Integer> bResultSupplier = AsyncSupplier.getDefault().submitSupplier(() -> getMethodParam2(arg3, arg4));
Supplier<Object> cResultSupplier = AsyncSupplier.getDefault().submitSupplier(() -> getMethodParam3(arg5, arg6));

myBigMethod(aResultSupplier.get(), bResultSupplier.get(), cResultSupplier.get());
```

The result of the asynchronous method calls/code blocks can also be obtained at a different point of code in the same thread or a different thread as in the below snippet.

```
AsyncSupplier.getDefault().submitSupplierForSingleAccess(() -> getMethodParam1(arg1, arg2), "a");
AsyncSupplier.getDefault().submitSupplierForSingleAccess(() -> getMethodParam2(arg3, arg4), "b");
AsyncSupplier.getDefault().submitSupplierForSingleAccess(() -> getMethodParam3(arg5, arg6), "c");


//Following can be in the same thread or a different thread
Optional<String> aResult = AsyncSupplier.getDefault().waitAndGetFromSupplier(String.class, "a");
Optional<Integer> bResult = AsyncSupplier.getDefault().waitAndGetFromSupplier(Integer.class, "b");
Optional<Object> cResult = AsyncSupplier.getDefault().waitAndGetFromSupplier(Object.class, "c");

 myBigMethod(aResult.get(),bResult.get(),cResult.get());
```

## Advanced Usage Examples - New Features

### Timeout Support

Prevent indefinite waits with timeout-enabled operations:

```java
// Execute with timeout (returns Optional)
Optional<String> result = AsyncSupplier.getDefault()
    .submitAndGetWithTimeout(() -> fetchDataFromAPI(), 5, TimeUnit.SECONDS);

if (result.isPresent()) {
    System.out.println("Got result: " + result.get());
} else {
    System.out.println("Operation timed out");
}

// Wait for flag with timeout in AsyncContext
boolean flagSet = AsyncContext.getDefault()
    .waitForFlagWithTimeout(3, TimeUnit.SECONDS, "operationComplete");
```

### Batch Processing

Process collections in parallel efficiently:

```java
List<Integer> ids = Arrays.asList(1, 2, 3, 4, 5);

// Process all items in parallel
List<User> users = AsyncSupplier.getDefault()
    .submitAndProcessAll(ids, id -> userService.fetchUser(id));

// Process with concurrency limit (max 3 parallel operations)
List<Result> results = AsyncSupplier.getDefault()
    .submitAndProcessAllWithLimit(largeList, item -> processItem(item), 3);
```

### Resilience Patterns

Build fault-tolerant applications:

```java
// Automatic retry with delays
Supplier<String> resilientOp = AsyncSupplier.getDefault()
    .submitSupplierWithRetry(() -> {
        return callUnreliableService();
    }, 3, 1000); // max 3 retries, 1 second delay

// Fallback value on failure
Supplier<Config> configSupplier = AsyncSupplier.getDefault()
    .submitSupplierWithFallback(
        () -> loadConfigFromRemote(),
        getDefaultConfig() // fallback value
    );

// Custom error handling
Supplier<Data> dataSupplier = AsyncSupplier.getDefault()
    .submitSupplierWithErrorHandler(
        () -> fetchData(),
        ex -> {
            logger.error("Failed to fetch data", ex);
            return getCachedData();
        }
    );
```

### Functional Composition

Chain and combine async operations:

```java
// Chain dependent operations
Supplier<String> chained = AsyncSupplier.getDefault()
    .submitChained(
        () -> fetchUserId(),
        userId -> fetchUserProfile(userId)
    );

// Combine multiple independent operations
Supplier<Report> report = AsyncSupplier.getDefault()
    .submitAndCombine(
        results -> generateReport((Data1)results.get(0), (Data2)results.get(1)),
        () -> fetchData1(),
        () -> fetchData2()
    );
```

### Competitive Execution

Get fastest results from multiple sources:

```java
// Race multiple operations
Supplier<String> fastest = AsyncSupplier.getDefault()
    .submitRace(
        () -> fetchFromCache(),
        () -> fetchFromDatabase(),
        () -> fetchFromAPI()
    );

String result = fastest.get(); // Returns result from fastest source

// Or get fastest directly as Optional
Optional<Data> fastestData = AsyncSupplier.getDefault()
    .submitAndGetFastest(
        () -> source1.getData(),
        () -> source2.getData(),
        () -> source3.getData()
    );
```

### CompletableFuture Integration

Bridge to modern Java async APIs:

```java
CompletableFuture<String> future = AsyncSupplier.getDefault()
    .submitAsCompletableFuture(() -> performOperation());

// Now use CompletableFuture's rich API
future.thenApply(String::toUpperCase)
      .thenAccept(System.out::println)
      .exceptionally(ex -> {
          System.err.println("Error: " + ex.getMessage());
          return null;
      });
```

### Monitoring & Control

Check and control async operations:

```java
// Submit a long-running operation
AsyncSupplier.getDefault()
    .submitSupplierForSingleAccess(() -> longRunningTask(), "taskKey");

// Check if still running
if (AsyncSupplier.getDefault().isPending("taskKey")) {
    System.out.println("Task is still running...");
    
    // Cancel if needed
    boolean cancelled = AsyncSupplier.getDefault().cancelSupplier("taskKey");
}
```

### Multi-Flag Coordination

Coordinate multiple async operations:

```java
// Start multiple operations that set flags when complete
AsyncTask.getDefault().submitTask(() -> {
    processStep1();
    AsyncContext.getDefault().notifyAllFlag("step1");
}, "task1");

AsyncTask.getDefault().submitTask(() -> {
    processStep2();
    AsyncContext.getDefault().notifyAllFlag("step2");
}, "task2");

// Wait for all steps to complete
AsyncContext.getDefault().waitForAllFlags(
    new String[]{"step1"}, 
    new String[]{"step2"}
);

// Or wait for any step to complete
String[] firstCompleted = AsyncContext.getDefault().waitForAnyFlag(
    new String[]{"step1"}, 
    new String[]{"step2"}
);
```

### Advanced Scheduling

Schedule tasks with fine-grained control:

```java
// Execute exactly N times
SchedulingTask.getDefault().scheduleTaskNTimes(
    5,              // execute 5 times
    100,            // initial delay 100ms
    500,            // interval 500ms
    TimeUnit.MILLISECONDS,
    () -> sendHeartbeat()
);

// Exponential backoff for polling
SchedulingTask.getDefault().scheduleTaskWithBackoff(
    100,            // initial delay 100ms
    10000,          // max delay 10 seconds
    2.0,            // double delay each time
    TimeUnit.MILLISECONDS,
    () -> checkForUpdates(),
    "pollingTask"
);

// Schedule supplier N times and collect results
Stream<Status> statuses = SchedulingSupplier.getDefault()
    .scheduleSupplierNTimes(
        10,             // collect 10 samples
        0,              // start immediately
        1000,           // every 1 second
        TimeUnit.MILLISECONDS,
        () -> getSystemStatus()
    );

List<Status> collectedStatuses = statuses.collect(Collectors.toList());
```

### Real-World Example: Resilient API Call with Timeout

```java
public Optional<UserData> fetchUserDataResilient(String userId) {
    try {
        // Combine retry, timeout, and fallback
        Optional<UserData> userData = AsyncSupplier.getDefault()
            .submitAndGetWithTimeout(() -> {
                Supplier<UserData> resilient = AsyncSupplier.getDefault()
                    .submitSupplierWithRetry(() -> {
                        return apiClient.getUserData(userId);
                    }, 3, 500); // 3 retries, 500ms delay
                
                return resilient.get();
            }, 10, TimeUnit.SECONDS); // 10 second total timeout
        
        return userData;
    } catch (Exception e) {
        logger.error("Failed to fetch user data", e);
        return Optional.empty();
    }
}
```
