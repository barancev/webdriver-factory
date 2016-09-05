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

/**
 * @deprecated Use WebDriverPool.DEFAULT instead. See documentation at https://github.com/barancev/webdriver-factory/
 */
@Deprecated
public class WebDriverFactory {

  private static WebDriverPool pool = WebDriverPool.DEFAULT;

  @Deprecated
  public static void setMode(WebDriverFactoryMode newMode) {
    if (! pool.isEmpty()) {
      throw new Error("Mode can't be changed because there are active WebDriver instances");
    }
    pool = createFactoryInternal(newMode);
  }

  public static void setDriverAlivenessChecker(DriverAlivenessChecker alivenessChecker) {
    pool.setDriverAlivenessChecker(alivenessChecker);
  }

  private static WebDriverPool createFactoryInternal(WebDriverFactoryMode mode) {
    switch (mode) {
      case SINGLETON:
        return new SingleWebDriverPool();
      case THREADLOCAL_SINGLETON:
        return new ThreadLocalSingleWebDriverPool();
      case UNRESTRICTED:
        return new LooseWebDriverPool();
      default:
        throw new Error("Unsupported browser factory mode: " + mode);
    }
  }

  public static void addLocalDriverProvider (LocalDriverProvider provider) {
    pool.addLocalDriverProvider(provider);
  }

  public static void addRemoteDriverProvider (RemoteDriverProvider provider) {
    pool.addRemoteDriverProvider(provider);
  }

  public static void setDefaultHub(String defaultHub) {
    pool.setDefaultHub(defaultHub);
  }

  @Deprecated
  public static WebDriver getDriver(String hub, Capabilities capabilities) {
    return pool.getDriver(hub, capabilities);
  }

  @Deprecated
  public static WebDriver getDriver(Capabilities capabilities) {
    return pool.getDriver(capabilities);
  }

  @Deprecated
  public static void dismissDriver(WebDriver driver) {
    pool.dismissDriver(driver);
  }

  @Deprecated
  public static void dismissAll() {
    pool.dismissAll();
  }

  @Deprecated
  public static boolean isEmpty() {
    return pool.isEmpty();
  }

}
