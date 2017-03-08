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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.BrowserType;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultLocalDriverProvider implements LocalDriverProvider {

  private static class Entry {
    private String browserName;
    private final String className;

    public Entry(String browserName, String className) {
      this.browserName = browserName;
      this.className = className;
    }
  }

  private Map<String, ReflectionBasedInstanceCreator> creators = Stream.of(
    new Entry(BrowserType.CHROME, "org.openqa.selenium.chrome.ChromeDriver"),
    new Entry(BrowserType.FIREFOX, "org.openqa.selenium.firefox.FirefoxDriver"),
    new Entry(BrowserType.IE, "org.openqa.selenium.ie.InternetExplorerDriver"),
    new Entry(BrowserType.EDGE, "org.openqa.selenium.edge.EdgeDriver"),
    new Entry(BrowserType.OPERA_BLINK, "org.openqa.selenium.opera.OperaDriver"),
    new Entry(BrowserType.OPERA, "com.opera.core.systems.OperaDriver"),
    new Entry(BrowserType.SAFARI, "org.openqa.selenium.safari.SafariDriver"),
    new Entry(BrowserType.PHANTOMJS, "org.openqa.selenium.phantomjs.PhantomJSDriver"),
    new Entry(BrowserType.HTMLUNIT, "org.openqa.selenium.htmlunit.HtmlUnitDriver")
  ).collect(Collectors.toMap(e -> e.browserName, e -> new ReflectionBasedInstanceCreator(e.className)));

  public WebDriver createDriver(Capabilities capabilities) {
    ReflectionBasedInstanceCreator creator = creators.get(capabilities.getBrowserName());
    if (creator !=  null) {
      return creator.createDriver(capabilities);
    }
    throw new DriverCreationError("Can't find local driver provider for capabilities " + capabilities);
  }

}
