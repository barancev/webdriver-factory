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

import java.net.URL;

public abstract class AbstractWebDriverPool implements WebDriverPool {

  DriverAlivenessChecker alivenessChecker = new DefaultDriverAlivenessChecker();
  private LocalDriverProvider localDriverProvider = new DefaultLocalDriverProvider();
  private RemoteDriverProvider remoteDriverProvider = new RemoteDriverProvider() {};

  protected String createKey(Capabilities capabilities, URL hub) {
    return capabilities.toString() + (hub == null ? "" : ":" + hub.toString());
  }

  protected WebDriver newDriver(URL hub, Capabilities capabilities) {
    return (hub == null)
        ? localDriverProvider.createDriver(capabilities)
        : remoteDriverProvider.createDriver(hub, capabilities);
  }

  public void setDriverAlivenessChecker(DriverAlivenessChecker alivenessChecker) {
    this.alivenessChecker = alivenessChecker;
  }

  public void setLocalDriverProvider(LocalDriverProvider localDriverProvider) {
    this.localDriverProvider = localDriverProvider;
  }

  public void setRemoteDriverProvider(RemoteDriverProvider remoteDriverProvider) {
    this.remoteDriverProvider = remoteDriverProvider;
  }
}
