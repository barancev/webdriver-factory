WebDriver Factory
====================

This library provides an utility to manage WebDriver instances. It helps to create, reuse and dismiss WebDriver instances.

To use this library in a maven project you have to add these dependencies:

```
<dependency>
    <groupId>ru.stqa.selenium</groupId>
    <artifactId>webdriver-factory</artifactId>
    <version>2.0</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-java</artifactId>
    <version>2.53.0</version>
</dependency>
<dependency>
    <groupId>org.seleniumhq.selenium</groupId>
    <artifactId>selenium-server</artifactId>
    <version>2.53.0</version>
</dependency>
```

It is curious that the library implements [Object Pool design pattern](http://sourcemaking.com/design_patterns/object_pool), but for historical reason it is called "a factory". The instances created by the factory are called "managed instances".

The factory implements three main strategies (or modes) to manage instances:
* SINGLETON mode allows a single managed instance of WebDriver to exist in any given moment;
* THREADLOCAL_SINGLETON mode (the default one since version 1.1.42) allows a single managed instance of WebDriver to exist for each thread;
* UNRESTRICTED mode does not impose any restrictions and creates a new managed instance on each request.

**1) The simplest use case**

```
Capabilities firefox = DesiredCapabilities.firefox();
// create a new managed instance
WebDriver driver = WebDriverFactory.getDriver(firefox);
// do something with the driver
driver.get("http://seleniumhq.org/");
// destroy the instance (calls driver.quit() implicitly)
WebDriverFactory.dismissDriver(driver);
```

**2) If one requests a new driver with the same capabilities** the existing instance should be reused in SINGLETON and THREADLOCAL_SINGLETON modes:

```
Capabilities firefox = DesiredCapabilities.firefox();
// create a new managed instance
WebDriver driver = WebDriverFactory.getDriver(firefox);
// do something with the driver
driver.get("http://seleniumhq.org/");

// obtain the same instance from the pool of the managed instances
driver = WebDriverFactory.getDriver(firefox);
// do something with the driver
driver.get("http://selenium2.ru/");

// destroy the driver
WebDriverFactory.dismissDriver(driver);
```

Additionaly, the factory checks availability of the browser (calls getCurrentUrl) before returning the instance to the client. If the browser is not available a new WebDriver instance should be created (and a new browser should be started) instead of the broken one.

**3) If one requests a new driver with different capabilities** a new WebDriver instance should be created 

What happens to the previous instances depends on the factory mode:
* in SINGLETON mode the previous managed instance of the driver should be destroyed,
* in THREADLOCAL_SINGLETON mode the previous managed instance created in the current thread should be destroyed, managed instances created in other threads should be kept untouched,
* in UNRESTRICTED mode all running instances are kept untouched.

4) One should not care about destroying each single WebDriver instance in each single test case, they can be destroyed all at once in the end of the test suite:

```
@Test
public void testSomething() {
  Capabilities firefox = DesiredCapabilities.firefox();
  WebDriver driver = WebDriverFactory.getDriver(firefox);
  // do something with the driver
  driver.get("http://seleniumhq.org/");
}

@Test
public void testSomethingElse() {
  Capabilities chrome = DesiredCapabilities.chrome();
  WebDriver driver = WebDriverFactory.getDriver(chrome);
  // do something with the driver
  driver.get("http://seleniumhq.org/");
}

@AfterSuite
public void stopAllDrivers() {
  WebDriverFactory.dismissAll();
}
```

(Ability to destroy all managed instances at once is probably the only usable feature of UNRESTRICTED mode)

5) One can change the factory mode if there are no active managed instances.

```
WebDriverFactory.setMode(WebDriverFactoryMode.SINGLETON);
```

There are [several samples](https://github.com/barancev/webdriver-extensions/tree/master/webdriver-factory-samples/src/test/java/ru/stqa/selenium/factory/samples) that show how to use WebDriverFactory with test frameworks JUnit and TestNG.