4.0
=======================

* Java 8 is the requirement now
* Updating source and target compilation level to Java 8
* Updating dependencies to the latest versions
* Deleting deprecated WebDriverFactory class
* Deleting WebDriverPool.setDefaultHub, one should call two-argument getDriver to obtain a remote driver
* Changing hub address type from String to URL to fail-fast malformed URLs
* Adding convenient getDriver methods that accept browser name instead of capabilities

3.0
=======================

* WebDriverFactory -> WebDriverPool
* More object-oriented design instead of static methods

2.1
=======================

* Breaking dependency on selenium-server

2.0
=======================

* Spin-off from the incubator project https://github.com/barancev/webdriver-extensions
* Dependency on Selenium is changed to be provided

1.2.51
=======================

* Selenium 2.51 support

1.2.49
=======================

* Selenium 2.49 support
* Fixed issue #2

1.2.47
=======================

* Selenium 2.47 support
* Implemented ability to install custom driver providers
* Implemented ability to load driver providers via ServiceLoader

1.2.46
=======================

* Selenium 2.46 support

1.2.45
=======================

* Selenium 2.45 support
* Samples are moved to a separate project to get rid of TestNG dependency

1.2.43
=======================

* Selenium 2.43 support

1.2.42
=======================

* Selenium 2.42 support
* Default mode is set to THREADLOCAL_SINGLETON
* Samples are updated to call WebDriverFactory.dismissAll() explicitly; yes, there is a shutdown hook, but it is "the last resort", not the recommended practice

1.1.41 (released 28.03.2014)
============================

Initial release, with Selenium 2.41 support
