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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WebDriverFactoryTest {

  private DesiredCapabilities fakeCapabilities;

  @Before
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    WebDriverFactory.setMode(WebDriverFactoryMode.SINGLETON);
    WebDriverFactory.addDriverProvider(new DefaultDriverProvider(
        fakeCapabilities, FakeWebDriver.class.getName()));
  }

  @Test
  public void testCanInstantiateAndDismissADriver() {
    assertTrue(WebDriverFactory.isEmpty());

    WebDriver driver = WebDriverFactory.getDriver(fakeCapabilities);
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(WebDriverFactory.isEmpty());

    WebDriverFactory.dismissDriver(driver);
    assertTrue(WebDriverFactory.isEmpty());
  }

  @Test
  public void testCanDismissAllDrivers() {
    assertTrue(WebDriverFactory.isEmpty());

    WebDriver driver = WebDriverFactory.getDriver(fakeCapabilities);
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(WebDriverFactory.isEmpty());

    WebDriverFactory.dismissAll();
    assertTrue(WebDriverFactory.isEmpty());
  }

  @Test
  public void testCanChangeModeIfEmpty() {
    assertTrue(WebDriverFactory.isEmpty());

    WebDriverFactory.setMode(WebDriverFactoryMode.SINGLETON);
    assertTrue(WebDriverFactory.isEmpty());
  }

  @Test(expected = Error.class)
  public void testCannotChangeModeIfNonEmpty() {
    assertTrue(WebDriverFactory.isEmpty());

    WebDriver driver = WebDriverFactory.getDriver(fakeCapabilities);

    try {
      WebDriverFactory.setMode(WebDriverFactoryMode.SINGLETON);
    } finally {
      WebDriverFactory.dismissDriver(driver);
      assertTrue(WebDriverFactory.isEmpty());
    }
  }

}
