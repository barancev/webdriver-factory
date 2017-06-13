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

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public final class ThreadLocalSingleWebDriverPool extends AbstractWebDriverPool {

  private ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

  private Map<WebDriver, String> driverToKeyMap = Collections.synchronizedMap(new HashMap<>());
  private Map<WebDriver, Thread> driverToThread = Collections.synchronizedMap(new HashMap<>());

  public ThreadLocalSingleWebDriverPool() {
    Runtime.getRuntime().addShutdownHook(new Thread(ThreadLocalSingleWebDriverPool.this::dismissAll));
  }

  @Override
  public synchronized WebDriver getDriver(URL hub, Capabilities capabilities) {
    dismissDriversInFinishedThreads();
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
            dismissDriver(tlDriver.get());
            createNewDriver(capabilities, hub);
          }
        }
      }
    }
    return tlDriver.get();
  }

  @Override
  public synchronized void dismissDriver(WebDriver driver) {
    dismissDriversInFinishedThreads();
    if (driverToKeyMap.get(driver) == null) {
      throw new Error("The driver is not owned by the factory: " + driver);
    }
    if (driver != tlDriver.get()) {
      throw new Error("The driver does not belong to the current thread: " + driver);
    }
    try {
      driver.quit();
    } finally {
      driverToKeyMap.remove(driver);
      driverToThread.remove(driver);
      tlDriver.remove();
    }
  }

  private synchronized void dismissDriversInFinishedThreads() {
    List<WebDriver> stale = driverToThread.entrySet().stream()
      .filter((entry) -> !entry.getValue().isAlive())
      .map(Map.Entry::getKey).collect(Collectors.toList());

    for (WebDriver driver : stale) {
      try {
        driver.quit();
      } finally {
        driverToKeyMap.remove(driver);
        driverToThread.remove(driver);
      }
    }
  }

  @Override
  public synchronized void dismissAll() {
    for (WebDriver driver : new HashSet<>(driverToKeyMap.keySet())) {
      try {
        driver.quit();
      } finally {
        driverToKeyMap.remove(driver);
        driverToThread.remove(driver);
      }
    }
  }

  @Override
  public synchronized boolean isEmpty() {
    return driverToKeyMap.isEmpty();
  }

  private synchronized void createNewDriver(Capabilities capabilities, URL hub) {
    String newKey = createKey(capabilities, hub);
    WebDriver driver = newDriver(hub, capabilities);
    driverToKeyMap.put(driver, newKey);
    driverToThread.put(driver, Thread.currentThread());
    tlDriver.set(driver);
  }
}
