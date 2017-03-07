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

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public abstract class AbstractWebDriverPool implements WebDriverPool {

  protected DriverAlivenessChecker alivenessChecker = new DefaultDriverAlivenessChecker();

  private List<LocalDriverProvider> localDriverProviders = new ArrayList<>();
  {
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.chrome(), "org.openqa.selenium.chrome.ChromeDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.firefox(), "org.openqa.selenium.firefox.FirefoxDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.internetExplorer(), "org.openqa.selenium.ie.InternetExplorerDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.edge(), "org.openqa.selenium.edge.EdgeDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.operaBlink(), "org.openqa.selenium.opera.OperaDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.opera(), "com.opera.core.systems.OperaDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.safari(), "org.openqa.selenium.safari.SafariDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.phantomjs(), "org.openqa.selenium.phantomjs.PhantomJSDriver"));
    localDriverProviders.add(new ReflectionBasedLocalDriverProvider(
      DesiredCapabilities.htmlUnit(), "org.openqa.selenium.htmlunit.HtmlUnitDriver"));
    for (LocalDriverProvider provider : ServiceLoader.load(LocalDriverProvider.class)) {
      localDriverProviders.add(provider);
    }
  }

  private List<RemoteDriverProvider> remoteDriverProviders = new ArrayList<>();
  {
    remoteDriverProviders.add(new ReflectionBasedRemoteDriverProvider());
    for (RemoteDriverProvider provider : ServiceLoader.load(RemoteDriverProvider.class)) {
      remoteDriverProviders.add(provider);
    }
  }

  public void addLocalDriverProvider(LocalDriverProvider provider) {
    localDriverProviders.add(0, provider);
  }

  public void addRemoteDriverProvider(RemoteDriverProvider provider) {
    remoteDriverProviders.add(0, provider);
  }

  protected String createKey(Capabilities capabilities, String hub) {
    return capabilities.toString() + ":" + hub;
  }

  protected WebDriver newDriver(String hub, Capabilities capabilities) {
    return (hub == null)
        ? createLocalDriver(capabilities)
        : createRemoteDriver(hub, capabilities);
  }

  private WebDriver createLocalDriver(Capabilities capabilities) {
    for (LocalDriverProvider provider : localDriverProviders) {
      WebDriver driver = provider.createDriver(capabilities);
      if (driver != null) {
        return driver;
      }
    }
    throw new DriverCreationError("Can't find local driver provider for capabilities " + capabilities);
  }

  private WebDriver createRemoteDriver(String hub, Capabilities capabilities) {
    for (RemoteDriverProvider provider : remoteDriverProviders) {
      WebDriver driver = provider.createDriver(hub, capabilities);
      if (driver != null) {
        return driver;
      }
    }
    throw new DriverCreationError("Can't find remote driver provider for capabilities " + capabilities);
  }

  public void setDriverAlivenessChecker(DriverAlivenessChecker alivenessChecker) {
    this.alivenessChecker = alivenessChecker;
  }
}
