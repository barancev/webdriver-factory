/*
 * Copyright 2016 Alexei Barantsev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.stqa.selenium.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An utility that helps to create, reuse and dismiss WebDriver instances.
 *
 * The WebDriver instances created by the pool and stored in the pool are called "managed instances".
 *
 * There are three ways to manage instances, three classes that implement WebDriverPool interface:
 * <ul>
 * <li>{@link SingleWebDriverPool} allows a single managed instance of WebDriver to exist at any given moment,</li>
 * <li>{@link ThreadLocalSingleWebDriverPool} allows a single managed instance of WebDriver to exist for each thread,</li>
 * <li>{@link LooseWebDriverPool} does not impose any restrictions, it creates a new managed instance on each request</li>
 * </ul>
 *
 * See documentation at https://github.com/barancev/webdriver-factory/
 * See usage examples at https://github.com/barancev/webdriver-factory-samples/
 */
public interface WebDriverPool {

  /**
   * Default pool
   */
  WebDriverPool DEFAULT = new ThreadLocalSingleWebDriverPool();

  /**
   * Returns a managed local instance of WebDriver for the given browser.
   * @param browser The desired browser
   */
  default WebDriver getDriver(String browser) {
    return getDriver((URL) null, browser);
  }

  /**
   * Returns a managed remote instance of WebDriver for the given browser.
   * @param hub The Selenium Server Hub address
   * @param browser The desired browser
   */
  default WebDriver getDriver(URL hub, String browser) {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setBrowserName(browser);
    return getDriver(capabilities);
  }

  /**
   * Returns a managed local instance of WebDriver with the given capabilities.
   * @param capabilities The desired driver capabilities
   */
  default WebDriver getDriver(Capabilities capabilities) {
    return getDriver((URL) null, capabilities);
  }

  /**
   * Returns a managed remote instance of WebDriver with the given capabilities.
   * @param hub The Selenium Server Hub address
   * @param capabilities The desired driver capabilities
   */
  WebDriver getDriver(URL hub, Capabilities capabilities);

  @Deprecated
  default WebDriver getDriver(String hub, Capabilities capabilities) {
    try {
      return getDriver(new URL(hub), capabilities);
    } catch (MalformedURLException e) {
      throw new DriverCreationError(e);
    }
  }

  /**
   * Quits the driver and removes it from the pool, if it is a managed instance.
   * Throws an Error on attempt to dismiss an unmanaged instance of WebDriver.
   * @param driver The driver that is not in use anymore and should be dismissed
   */
  void dismissDriver(WebDriver driver);

  /**
   * Quits all the managed drivers and empties the pool.
   */
  void dismissAll();

  /**
   * Checks if the pool is empty.
   */
  boolean isEmpty();

  void setDriverAlivenessChecker(DriverAlivenessChecker alivenessChecker);

  void setLocalDriverProvider(LocalDriverProvider localDriverProvider);
  void setRemoteDriverProvider(RemoteDriverProvider remoteDriverProvider);
}
