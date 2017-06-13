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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractWebDriverPoolTest {

  private WebDriverPool factory;
  private DesiredCapabilities fakeCapabilities;

  private class CustomLocalDriverProvider extends DefaultLocalDriverProvider {
    @Override
    public WebDriver createDriver(Capabilities capabilities) {
      if (capabilities.getBrowserName().equals("FAKE")) {
        return new FakeWebDriver(capabilities);
      }
      return super.createDriver(capabilities);
    }
  }

  @BeforeEach
  public void setUp() {
    fakeCapabilities = new DesiredCapabilities();
    fakeCapabilities.setBrowserName("FAKE");

    factory = new SingleWebDriverPool();

    factory.setLocalDriverProvider(new CustomLocalDriverProvider());
  }

  @Test
  public void testCanInstantiateAndDismissAStandardDriverByName() {
    WebDriver driver = factory.getDriver(BrowserType.HTMLUNIT);
    assertThat(driver, instanceOf(HtmlUnitDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
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
  public void testCanInstantiateAndDismissADriverWithACustomDriverProvider() {
    WebDriver driver = factory.getDriver(fakeCapabilities);
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanHandleAlertsOnDriverAvailabilityCheck() {
    factory.setLocalDriverProvider(FakeAlertiveWebDriver::new);

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
  public void testCanInstantiateARemoteDriver() throws MalformedURLException {
    factory.setRemoteDriverProvider(new RemoteDriverProvider() {
      @Override
      public WebDriver createDriver(URL hub, Capabilities capabilities) {
        return new FakeWebDriver(capabilities);
      }
    });

    WebDriver driver = factory.getDriver(new URL("http://localhost/"), DesiredCapabilities.firefox());
    assertThat(driver, instanceOf(FakeWebDriver.class));
    assertFalse(factory.isEmpty());

    factory.dismissDriver(driver);
    assertTrue(factory.isEmpty());
  }

  @Test
  public void testThrowsAnErrorIfDriverCannotBeCreated() {
    try {
      WebDriver driver = factory.getDriver("BADNAME");
      fail("Exception expected");
    } catch (DriverCreationError expected) {
    }

    assertTrue(factory.isEmpty());
  }

  @Test
  public void testCanSetCustomAlivenessChecker() {
    factory.setDriverAlivenessChecker(driver -> false);

    WebDriver driver1 = factory.getDriver(fakeCapabilities);
    WebDriver driver2 = factory.getDriver(fakeCapabilities);

    assertNotSame(driver2, driver1);
  }

}
