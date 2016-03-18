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
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ThreadLocalSingletonStorage extends WebDriverFactoryInternal {

  private ThreadLocal<WebDriver> tlDriver = new ThreadLocal<WebDriver>();

  private Map<WebDriver, String> driverToKeyMap = new HashMap<WebDriver, String>();

  @Override
  public WebDriver getDriver(String hub, Capabilities capabilities) {
    String newKey = createKey(capabilities, hub);
    if (tlDriver.get() == null) {
      createNewDriver(capabilities, hub);

    } else {
      String key = driverToKeyMap.get(tlDriver.get());
      if (key == null) {
        // The driver was dismissed
        createNewDriver(capabilities, hub);

      } else {
        if (!newKey.equals(key)) {
          // A different flavour of WebDriver is required
          dismissDriver(tlDriver.get());
          createNewDriver(capabilities, hub);

        } else {
          // Check the browser is alive
          if (! alivenessChecker.isAlive(tlDriver.get())) {
            createNewDriver(capabilities, hub);
          }
        }
      }
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
    driver.quit();
    driverToKeyMap.remove(driver);
    tlDriver.remove();
  }

  @Override
  public void dismissAll() {
    for (WebDriver driver : new HashSet<WebDriver>(driverToKeyMap.keySet())) {
      driver.quit();
      driverToKeyMap.remove(driver);
    }
  }

  @Override
  public boolean isEmpty() {
    return driverToKeyMap.isEmpty();
  }

  private void createNewDriver(Capabilities capabilities, String hub) {
    String newKey = createKey(capabilities, hub);
    WebDriver driver = newDriver(hub, capabilities);
    driverToKeyMap.put(driver, newKey);
    tlDriver.set(driver);
  }
}
