WebDriver Factory
====================

This is an utility to manage WebDriver instances
* [webdriver-repeatable-actions](https://github.com/barancev/webdriver-extensions/wiki/WebDriver-Repeatable-Actions) is an alternative to "expected conditions": instead of waiting for an element to be ready for an action one can try to perform an action until succeeded
* [webdriver-wrapper](https://github.com/barancev/webdriver-extensions/wiki/WebDriver-Wrapper) allows to create WebDriver extensions following Decorator design pattern; it also contains several ready-to-use extensions: a wrapper that highlights elements before an action, a wrapper that handles unhandled alerts, a wrapper that handles "stale" elements and attempts to find them again and perform the action on the "restored" element
* [webdriver-logging-wrapper](https://github.com/barancev/webdriver-extensions/wiki/WebDriver-Logging-Wrapper) is a WebDriver wrapper that logs all commands to slf4j
* [webdriver-implicit-wait-wrapper](https://github.com/barancev/webdriver-extensions/wiki/WebDriver-Implicit-Wait-Wrapper) implements client-side waits that are similar to browser-side impicit waits, but are more intelligent and flexible
