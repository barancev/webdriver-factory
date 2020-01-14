/*
 * Copyright 2020 Alexei Barantsev
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultLocalDriverProviderTest {

  DefaultLocalDriverProvider provider;
  WebDriver driver;

  @BeforeEach
  void init() {
    provider = new DefaultLocalDriverProvider();
  }

  @Test
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  void canInstantiateFirefoxDriverWithFirefoxOptions() {
    driver = provider.createDriver(new FirefoxOptions());
    assertTrue(driver instanceof FirefoxDriver);
  }

  @Test
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  void canInstantiateChromeDriverWithChromeOptions() {
    driver = provider.createDriver(new ChromeOptions());
    assertTrue(driver instanceof ChromeDriver);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  void canInstantiateInternetExplorerDriverWithInternetExplorerOptions() {
    driver = provider.createDriver(new InternetExplorerOptions());
    assertTrue(driver instanceof InternetExplorerDriver);
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  void canInstantiateEdgeDriverWithEdgeOptions() {
    driver = provider.createDriver(new EdgeOptions());
    assertTrue(driver instanceof EdgeDriver);
  }

  @Test
  @EnabledOnOs(OS.MAC)
  @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
  void canInstantiateSafariDriverWithSafariOptions() {
    driver = provider.createDriver(new SafariOptions());
    assertTrue(driver instanceof SafariDriver);
  }

  @AfterEach
  void fin() {
    if (driver != null) {
      driver.quit();
    }
  }
}
