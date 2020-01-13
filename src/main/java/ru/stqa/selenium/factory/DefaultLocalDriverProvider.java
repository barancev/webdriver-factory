/*
 * Copyright 2016 Alexei Barantsev
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

import com.google.common.collect.ImmutableMap;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.util.Map;
import java.util.function.Function;

public class DefaultLocalDriverProvider implements LocalDriverProvider {

  private Map<String, Function<Capabilities, WebDriver>> creators = new ImmutableMap.Builder<String, Function<Capabilities, WebDriver>>()
    .put(BrowserType.CHROME, caps -> new ChromeDriver(new ChromeOptions().merge(caps)))
    .put(BrowserType.FIREFOX, caps -> new FirefoxDriver(new FirefoxOptions().merge(caps)))
    .put(BrowserType.IE, caps -> new InternetExplorerDriver(new InternetExplorerOptions().merge(caps)))
    .put(BrowserType.EDGE, caps -> new EdgeDriver(new EdgeOptions().merge(caps)))
    .put(BrowserType.SAFARI, caps -> new SafariDriver(new SafariOptions().merge(caps)))
    .put(BrowserType.OPERA_BLINK, caps -> new OperaDriver(new OperaOptions().merge(caps)))
    .build();

  private Map<String, String> externalDriverClasses = new ImmutableMap.Builder<String, String>()
    .put(BrowserType.OPERA, "com.opera.core.systems.OperaDriver")
    .put(BrowserType.HTMLUNIT, "org.openqa.selenium.htmlunit.HtmlUnitDriver")
    .build();

  public WebDriver createDriver(ChromeOptions options) {
    return new ChromeDriver(options);
  }

  public WebDriver createDriver(FirefoxOptions options) {
    return new FirefoxDriver(options);
  }

  public WebDriver createDriver(InternetExplorerOptions options) {
    return new InternetExplorerDriver(options);
  }

  public WebDriver createDriver(EdgeOptions options) {
    return new EdgeDriver(options);
  }

  public WebDriver createDriver(SafariOptions options) {
    return new SafariDriver(options);
  }

  public WebDriver createDriver(OperaOptions options) {
    return new OperaDriver(options);
  }

  public WebDriver createDriver(Capabilities capabilities) {
    String browserName = capabilities.getBrowserName();
    Function<Capabilities, WebDriver> creator = creators.get(browserName);
    if (creator != null) {
      return creator.apply(capabilities);
    }

    String className = externalDriverClasses.get(browserName);
    if (className != null) {
      return new ReflectionBasedInstanceCreator(className).createDriver(capabilities);
    }

    throw new DriverCreationError("Can't find local driver provider for capabilities " + capabilities);
  }

}
