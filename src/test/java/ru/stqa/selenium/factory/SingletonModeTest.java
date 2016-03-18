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

import static org.junit.Assert.*;

public class SingletonModeTest {

  private WebDriverFactoryInternal factory;
  private DesiredCapabilities fakeCapabilities;

  @Before
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    factory = new SingletonStorage();

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

}
