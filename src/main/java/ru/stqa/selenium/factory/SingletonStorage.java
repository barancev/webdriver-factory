/*
 * Copyright 2014 Alexei Barantsev
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

public class SingletonStorage extends WebDriverFactoryInternal {

  private String key;
  private WebDriver driver;

  @Override
  public WebDriver getDriver(String hub, Capabilities capabilities) {
    String newKey = createKey(capabilities, hub);
    if (driver == null) {
      createNewDriver(hub, capabilities);

    } else {
      if (!newKey.equals(key)) {
        // A different flavour of WebDriver is required
        dismissDriver();
        createNewDriver(hub, capabilities);

      } else {
        // Check the browser is alive
        if (! alivenessChecker.isAlive(driver)) {
          createNewDriver(hub, capabilities);
        }
      }
    }

    return driver;
  }

  @Override
  public void dismissDriver(WebDriver driver) {
    if (driver != this.driver) {
      throw new Error("The driver is not owned by the factory: " + driver);
    }
    dismissDriver();
  }

  @Override
  public void dismissAll() {
    dismissDriver();
  }

  @Override
  public boolean isEmpty() {
    return driver == null;
  }

  private void createNewDriver(String hub, Capabilities capabilities) {
    String newKey = createKey(capabilities, hub);
    driver = newDriver(hub, capabilities);
    key = newKey;
  }

  private void dismissDriver() {
    if (driver != null) {
      driver.quit();
      driver = null;
      key = null;
    }
  }
}
