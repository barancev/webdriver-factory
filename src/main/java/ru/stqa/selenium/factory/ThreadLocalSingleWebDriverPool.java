/*
 * Copyright 2013 Alexei Barantsev
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
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ThreadLocalSingleWebDriverPool extends AbstractWebDriverPool {

  private ThreadLocal<WebDriver> tlDriver = new ThreadLocal<WebDriver>();

  private Map<WebDriver, String> driverToKeyMap = new ConcurrentHashMap<WebDriver, String>();

  public ThreadLocalSingleWebDriverPool() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        ThreadLocalSingleWebDriverPool.this.dismissAll();
      }
    });
  }

  @Override
  public WebDriver getDriver(String hub, Capabilities capabilities) {
    if (tlDriver.get() == null) {
      return createNewDriver(capabilities, hub);
    }
    String key = driverToKeyMap.get(tlDriver.get());
    if (key == null) {
      // The driver was dismissed in a wrong way
      return createNewDriver(capabilities, hub);
    }
    String newKey = createKey(capabilities, hub);
    if (!newKey.equals(key)) {
      // A different flavour of WebDriver is required
      dismissDriver(tlDriver.get());
      return createNewDriver(capabilities, hub);
    }
    // Check the browser is alive
    if (!alivenessChecker.isAlive(tlDriver.get())) {
      dismissDriver(tlDriver.get());
      createNewDriver(capabilities, hub);
    }
    return tlDriver.get();
  }

  @Override
  public void dismissDriver(WebDriver driver) {
    if (driverToKeyMap.get(driver) == null) {
      throw new Error("The driver is not owned by the factory: " + driver);
    }
    if (driver != tlDriver.get()) {
      throw new Error("The driver does not belong to the current thread: " + driver);
    }
    try {
      driver.quit();
    } catch (UnreachableBrowserException ignore) {
    } finally {
      driverToKeyMap.remove(driver);
      tlDriver.remove();
    }
  }

  @Override
  public void dismissAll() {
    for (WebDriver driver : new HashSet<WebDriver>(driverToKeyMap.keySet())) {
      try {
        driver.quit();
      } catch (UnreachableBrowserException ignore) {
      } finally {
        driverToKeyMap.remove(driver);
      }
    }
  }

  @Override
  public boolean isEmpty() {
    return driverToKeyMap.isEmpty();
  }

  private WebDriver createNewDriver(Capabilities capabilities, String hub) {
    String newKey = createKey(capabilities, hub);
    WebDriver driver = newDriver(hub, capabilities);
    driverToKeyMap.put(driver, newKey);
    tlDriver.set(driver);
    return driver;
  }
}
