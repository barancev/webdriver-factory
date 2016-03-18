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

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.server.DefaultDriverProvider;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ThreadLocalSingletonModeTest {

  private WebDriverFactoryInternal factory;
  private DesiredCapabilities fakeCapabilities;

  @Before
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    factory = new ThreadLocalSingletonStorage();

    factory.addDriverProvider(new DefaultDriverProvider(
        fakeCapabilities, FakeWebDriver.class.getName()));
  }

  private boolean isActive(WebDriver driver) {
    return ((FakeWebDriver) driver).isActive();
  }

  @Test
  public void testCanInstantiateAndDismissADriver() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertFalse(isActive(driver));
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanDismissAllDrivers() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));
    assertFalse(factory.isEmpty());

    factory.dismissAll();
    assertFalse(isActive(driver));
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testShouldReuseADriverWithSameCapabilities() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));

    WebDriver driver2 = factory.getDriver(fakeCapabilities);
    assertSame(driver2, driver);
    assertTrue(isActive(driver));
  }

  @Test
  public void testShouldRecreateADriverWithDifferentCapabilities() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));

    fakeCapabilities.setCapability("foo", "bar");
    WebDriver driver2 = factory.getDriver(fakeCapabilities);
    assertNotSame(driver2, driver);
    assertTrue(isActive(driver2));
    assertFalse(isActive(driver));
  }

  @Test
  public void testShouldRecreateAnInactiveDriver() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));
    driver.quit();

    WebDriver driver2 = factory.getDriver(fakeCapabilities);
    assertNotSame(driver2, driver);
    assertTrue(isActive(driver2));
    assertFalse(isActive(driver));
  }

  @Test(expected = Error.class)
  public void testShouldDismissOwnedDriversOnly() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));

    WebDriver driver2 = new FakeWebDriver(fakeCapabilities);
    assertNotSame(driver2, driver);

    factory.dismissDriver(driver2);
  }

  @Test
  public void testShouldCreateADriverForEachThread() throws InterruptedException {
    final WebDriver driver = factory.getDriver(fakeCapabilities);

    Thread t = new Thread() {
      @Override
      public void run() {
        WebDriver driver2 = factory.getDriver(fakeCapabilities);
        assertNotSame(driver2, driver);
      }
    };
    t.start();
    t.join();
  }

  @Test
  public void testShouldNotDismissADriverFromAnotherThread() throws InterruptedException {
    final WebDriver driver = factory.getDriver(fakeCapabilities);
    final List<Throwable> thrown = new ArrayList<Throwable>();

    Thread t = new Thread() {
      @Override
      public void run() {
        factory.dismissDriver(driver);
      }
    };
    t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        thrown.add(e);
      }
    });
    t.start();

    t.join();

    assertTrue(isActive(driver));
    assertFalse(factory.isEmpty());
    assertFalse(thrown.isEmpty());
  }

  @Test
  public void testDismissAllCanDismissDriversFromAllThreads() {
    final WebDriver driver = factory.getDriver(fakeCapabilities);

    new Thread() {
      @Override
      public void run() {
        WebDriver driver2 = factory.getDriver(fakeCapabilities);
        assertNotSame(driver2, driver);
      }
    }.start();

    factory.dismissAll();
    assertFalse(isActive(driver));
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testShouldRecreateADriverAfterDismissAll() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    factory.dismissAll();

    WebDriver driver2 = factory.getDriver(fakeCapabilities);
    assertNotSame(driver2, driver);
    assertTrue(isActive(driver2));
    assertFalse(isActive(driver));
  }

}
