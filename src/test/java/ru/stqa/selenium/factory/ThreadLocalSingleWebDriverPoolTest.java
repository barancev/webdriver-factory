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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ThreadLocalSingleWebDriverPoolTest {

  private WebDriverPool factory;
  private DesiredCapabilities fakeCapabilities;

  @BeforeEach
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    factory = new ThreadLocalSingleWebDriverPool();

    factory.setLocalDriverProvider(FakeWebDriver::new);
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

    factory.dismissDriver(driver2);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testShouldDismissOwnedDriversOnly() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));

    WebDriver driver2 = new FakeWebDriver(fakeCapabilities);
    assertNotSame(driver2, driver);

    assertThrows(Error.class, () -> factory.dismissDriver(driver2));
  }

  @Test
  public void testShouldCreateADriverForEachThread() throws InterruptedException {
    final WebDriver driver = factory.getDriver(fakeCapabilities);

    Thread t = new Thread(() -> {
      WebDriver driver2 = factory.getDriver(fakeCapabilities);
      assertNotSame(driver2, driver);
    });
    t.start();

    assertTrue(isActive(driver));

    t.join();
  }

  @Test
  public void testShouldAutoDismissTheDriverIfAThreadIsOver() throws InterruptedException {
    final WebDriver driver = factory.getDriver(fakeCapabilities);

    Thread t = new Thread(() -> {
      WebDriver driver2 = factory.getDriver(fakeCapabilities);
      assertNotSame(driver2, driver);
    });
    t.start();

    assertTrue(isActive(driver));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertFalse(isActive(driver));

    t.join();
    // the thread is over, but the driver is not auto dismissed yet
    assertFalse(factory.isEmpty());

    // this action should dismiss the driver in the complete thread
    WebDriver driver3 = factory.getDriver(fakeCapabilities);
    factory.dismissDriver(driver3);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testShouldNotDismissADriverFromAnotherThread() throws InterruptedException {
    final WebDriver driver = factory.getDriver(fakeCapabilities);
    final List<Throwable> thrown = new ArrayList<>();

    Thread t = new Thread(() -> factory.dismissDriver(driver));
    t.setUncaughtExceptionHandler((t1, e) -> thrown.add(e));
    t.start();
    t.join();

    assertTrue(isActive(driver));
    assertFalse(factory.isEmpty());
    assertFalse(thrown.isEmpty());
  }

  @Test
  public void testDismissAllCanDismissDriversFromAllThreads() {
    final WebDriver driver = factory.getDriver(fakeCapabilities);

    new Thread(() -> {
      WebDriver driver2 = factory.getDriver(fakeCapabilities);
      assertNotSame(driver2, driver);
    }).start();

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

  @Test
  public void testConcurrentModificationExceptionInDismissDriversInFinishedThreadsMethod() throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    List<WebDriver> drivers = new ArrayList<>();
    for (int d = 0; d < 1000; d++) {
      threads.add(new Thread(() -> drivers.add(factory.getDriver(fakeCapabilities))));
    }
    for (Thread thread : threads) {
      thread.start();
    }
    for (Thread thread : threads) {
      thread.join();
    }
    assertEquals(threads.size(), drivers.size());
  }

  private static class BrokenFakeWebDriver extends FakeWebDriver {
    public BrokenFakeWebDriver(Capabilities capabilities) {
      super(capabilities);
    }

    public void quit() {
      throw new WebDriverException("I don't want to quit");
    }
  }

  @Test
  public void testShouldBeAbleToUseFactoryIfDriverCannotQuit() {
    factory.setLocalDriverProvider(BrokenFakeWebDriver::new);

    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertTrue(isActive(driver));
    try {
      factory.dismissDriver(driver);
      fail("Should throw");
    } catch (WebDriverException expected) {
    }

    WebDriver driver2 = factory.getDriver(fakeCapabilities);
    assertNotSame(driver2, driver);
  }

}
