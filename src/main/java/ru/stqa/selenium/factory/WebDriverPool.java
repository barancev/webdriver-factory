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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * See documentation at https://github.com/barancev/webdriver-factory/
 */
public interface WebDriverPool {

  WebDriverPool DEFAULT = new ThreadLocalSingleWebDriverPool();

  default WebDriver getDriver(String browser) {
    return getDriver((URL) null, browser);
  }

  default WebDriver getDriver(URL hub, String browser) {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setBrowserName(browser);
    return getDriver(capabilities);
  }

  default WebDriver getDriver(Capabilities capabilities) {
    return getDriver((URL) null, capabilities);
  }

  WebDriver getDriver(URL hub, Capabilities capabilities);

  @Deprecated
  default WebDriver getDriver(String hub, Capabilities capabilities) {
    try {
      return getDriver(new URL(hub), capabilities);
    } catch (MalformedURLException e) {
      throw new DriverCreationError(e);
    }
  }

  void dismissDriver(WebDriver driver);

  void dismissAll();

  boolean isEmpty();

  void setDriverAlivenessChecker(DriverAlivenessChecker checker);

  void addLocalDriverProvider(LocalDriverProvider provider);

  void addRemoteDriverProvider(RemoteDriverProvider provider);
}
