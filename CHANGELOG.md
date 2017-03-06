3.1
=======================

* Updating dependencies to the latest versions
* Updating source and target compilation level to Java 8

3.0
=======================

* WebDriverFactory -> WebDriverPool
* More object-oriented design instead of static methods

2.1
=======================

* Breaking dependency on selenium server

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
* Defaut mode is set to THREADLOCAL_SINGLETON
* Samples are updated to call WebDriverFactory.dismisslAll() explicitly; yes, there is a shutdown hook, but it is "the last resort", not the recommended practice

1.1.41 (released 28.03.2014)
============================

Inital release, with Selenium 2.41 support
