Web Driver Factory
====================

[![Run Status](https://api.shippable.com/projects/58be80a4e4b9a205009c617c/badge?branch=master)](https://app.shippable.com/projects/58be80a4e4b9a205009c617c)

This library provides an utility to manage WebDriver instances. It helps to create, reuse and dismiss WebDriver instances.

To use this library in a maven project you have to add these dependencies:

```xml
<dependency>
    <groupId>ru.stqa.selenium</groupId>
    <artifactId>webdriver-factory</artifactId>
    <version>4.2</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>3.5.1</version>
</dependency>
```

The library implements [Object Pool design pattern](http://sourcemaking.com/design_patterns/object_pool), but for a historical reason it is called "factory".

The instances created by the pool and stored in the pool are called "managed instances".

The library provides three ways to manage instances:
* `SingleWebDriverPool` allows a single managed instance of WebDriver to exist at any given moment,
* `ThreadLocalSingleWebDriverPool` allows a single managed instance of WebDriver to exist for each thread,
* `LooseWebDriverPool` does not impose any restrictions, it creates a new managed instance on each request.

You can use as many separate pools as you like, but there is also `WebDriverPool.DEFAULT` that is an instance of `ThreadLocalSingleWebDriverPool`.

**1) The simplest use case**

```java
Capabilities firefox = DesiredCapabilities.firefox();
// create a new managed instance
WebDriver driver = WebDriverPool.DEFAULT.getDriver(firefox);
// do something with the driver
driver.get("http://seleniumhq.org/");
// destroy the instance (calls driver.quit() implicitly)
WebDriverPool.DEFAULT.dismissDriver(driver);
```

**2) If one requests a new driver with the same capabilities** the existing instance should be reused by `SingleWebDriverPool` and `ThreadLocalSingleWebDriverPool`:

```java
Capabilities firefox = DesiredCapabilities.firefox();
// create a new managed instance
WebDriver driver = WebDriverPool.DEFAULT.getDriver(firefox);
// do something with the driver
driver.get("http://seleniumhq.org/");

// obtain the same instance from the pool of the managed instances
driver = WebDriverPool.DEFAULT.getDriver(firefox);
// do something with the driver
driver.get("http://selenium2.ru/");

// destroy the driver
WebDriverPool.DEFAULT.dismissDriver(driver);
```

Additionally, the pool checks availability of the browser (by default it checks that `driver.getWindowHandles().size() > 0`) before returning the instance to the client. If the browser is not available the pool dismisses the "broken" driver and creates a new WebDriver instance as a replacement. 

**3) If one requests a new driver with different capabilities** a new WebDriver instance should be created 

What happens to the previous instances depends on the pool implementation:
* `SingleWebDriverPool` destroys and dismisses the previous managed instance of the driver,
* `ThreadLocalSingleWebDriverPool` destroys and dismisses the previous managed instance of the driver associated with the current thread; managed instances created in other threads are kept untouched,
* `LooseWebDriverPool` does nothing to all the running instances.

4) One can destroy all managed WebDriver instances in a pool at once:

```java
@Test
public void testSomething() {
  Capabilities firefox = DesiredCapabilities.firefox();
  WebDriver driver = WebDriverPool.DEFAULT.getDriver(firefox);
  // do something with the driver
  driver.get("http://seleniumhq.org/");
}

@Test
public void testSomethingElse() {
  Capabilities chrome = DesiredCapabilities.chrome();
  WebDriver driver = WebDriverPool.DEFAULT.getDriver(chrome);
  // do something with the driver
  driver.get("http://seleniumhq.org/");
}

@AfterSuite
public void stopAllDrivers() {
  WebDriverPool.DEFAULT.dismissAll();
}
```

(Ability to destroy all managed instances at once is probably the only usable feature of `LooseWebDriverPool`)

There are [several samples](https://github.com/barancev/webdriver-factory-samples/tree/master/src/test/java/ru/stqa/selenium/factory/samples) that show how to use WebDriverFactory with test frameworks JUnit and TestNG.
