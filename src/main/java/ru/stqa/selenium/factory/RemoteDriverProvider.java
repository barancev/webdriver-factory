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
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface RemoteDriverProvider {

  /**
   * Creates a new driver with the desired capabilities, or returns null if the
   * capabilities does not match the provider's ability to create drivers.
   */
  WebDriver createDriver(String hub, Capabilities capabilities);

  static class Default implements RemoteDriverProvider {
    @Override
    public WebDriver createDriver(String hub, Capabilities capabilities) {
      try {
        return new RemoteWebDriver(new URL(hub), capabilities);
      } catch (MalformedURLException e) {
        Logger.getLogger(RemoteDriverProvider.class.getName())
            .log(Level.INFO, "Could not connect to WebDriver hub", e);
        return null;
      }
    }
  }
}
