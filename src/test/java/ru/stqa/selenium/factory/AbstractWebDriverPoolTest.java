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
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class AbstractWebDriverPoolTest {

  private AbstractWebDriverPool factory;
  private DesiredCapabilities fakeCapabilities;

  @Before
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    factory = new SingleWebDriverPool();

    factory.addLocalDriverProvider(new ReflectionBasedLocalDriverProvider(
        fakeCapabilities, FakeWebDriver.class.getName()));
  }

  @Test
  public void testCanInstantiateAndDismissAStandardDriver() {
    WebDriver driver = factory.getDriver(DesiredCapabilities.htmlUnit());
    assertThat(driver, instanceOf(HtmlUnitDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanInstantiateAndDismissADriverByClassName() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void throwsOnAttemptToInstantiateADriverByBadClassName() {
    DesiredCapabilities capabilities = new DesiredCapabilities();
    capabilities.setBrowserName("FAKE-2");
    factory.addLocalDriverProvider(new ReflectionBasedLocalDriverProvider(
        capabilities, "BadClassName"));

    try {
      WebDriver driver = factory.getDriver(capabilities);
      fail("Exception expected");
    } catch (Exception expected) {
    }

    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanInstantiateAndDismissADriverWithACustomDriverProvider() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanOverrideExistingDriverProvider() {
    factory.addLocalDriverProvider(
        new ReflectionBasedLocalDriverProvider(DesiredCapabilities.firefox(),
            FakeWebDriver.class.getName()));

    WebDriver driver = factory.getDriver(DesiredCapabilities.firefox());
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanHandleAlertsOnDriverAvailabilityCheck() {
    factory.addLocalDriverProvider(
        new ReflectionBasedLocalDriverProvider(DesiredCapabilities.firefox(),
            FakeAlertiveWebDriver.class.getName()));

    WebDriver driver = factory.getDriver(DesiredCapabilities.firefox());
    assertThat(driver, instanceOf(FakeAlertiveWebDriver.class));
    assertFalse(factory.isEmpty());

    WebDriver driver2 = factory.getDriver(DesiredCapabilities.firefox());
    assertSame(driver2, driver);
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanInstantiateARemoteDriver() {
    factory.addRemoteDriverProvider(new RemoteDriverProvider() {
      @Override
      public WebDriver createDriver(String hub, Capabilities capabilities) {
        return new FakeWebDriver(capabilities);
      }
    });

    factory.setDefaultHub("some url");

    WebDriver driver = factory.getDriver(DesiredCapabilities.firefox());
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanNotInstantiateARemoteDriverWithBadUrl() {
    factory.setDefaultHub("some url");

    try {
      WebDriver driver = factory.getDriver(DesiredCapabilities.firefox());
      fail("Exception expected");
    } catch (Error ignored) {
    }

    assertTrue(factory.isEmpty());
  }

}
