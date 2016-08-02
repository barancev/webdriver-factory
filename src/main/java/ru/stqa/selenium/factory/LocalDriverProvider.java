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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface LocalDriverProvider {

  /**
   * Checks that the browser name set in the provided capabilities matches the browser name
   * set in the desired capabilities.
   * @param capabilities The desired capabilities
   * @return true if the browser name is the same, false otherwise
   */
  boolean canCreateDriverInstanceFor(Capabilities capabilities);

  /**
   * Creates a new driver with the desired capabilities, or returns null if the
   * capabilities does not match the provider's ability to create drivers.
   */
  WebDriver createDriver(Capabilities capabilities);

  class Default implements LocalDriverProvider {

    private static final Logger LOG = Logger.getLogger(Default.class.getName());

    private Capabilities capabilities;
    private Class<? extends WebDriver> driverClass;
    private String driverClassName;

    public Default(Capabilities capabilities, Class<? extends WebDriver> driverClass) {
      this.capabilities = new DesiredCapabilities(capabilities);
      this.driverClass = driverClass;
    }

    public Default(Capabilities capabilities, String driverClassName) {
      this.capabilities = new DesiredCapabilities(capabilities);
      this.driverClassName = driverClassName;
    }

    private Class<? extends WebDriver> getDriverClass() {
      if (driverClass != null) {
        return driverClass;
      }
      try {
        return Class.forName(driverClassName).asSubclass(WebDriver.class);
      } catch (ClassNotFoundException e) {
        LOG.log(Level.INFO, "Driver class not found: " + driverClassName);
        return null;
      } catch (NoClassDefFoundError e) {
        LOG.log(Level.INFO, "Driver class not found: " + driverClassName);
        return null;
      } catch (UnsupportedClassVersionError e) {
        LOG.log(Level.INFO, "Driver class is built for higher Java version: " + driverClassName);
        return null;
      }
    }

    @Override
    public boolean canCreateDriverInstanceFor(Capabilities capabilities) {
      return this.capabilities.getBrowserName().equals(capabilities.getBrowserName());
    }

    @Override
    public WebDriver createDriver(Capabilities capabilities) {
      if (canCreateDriverInstanceFor(capabilities)) {
        return callConstructor(getDriverClass(), capabilities);
      } else {
        return null;
      }
    }

    private WebDriver callConstructor(Class<? extends WebDriver> from, Capabilities capabilities) {
      try {
        Constructor<? extends WebDriver> constructor = from.getConstructor(Capabilities.class);
        return constructor.newInstance(capabilities);
      } catch (NoSuchMethodException e) {
        try {
          return from.newInstance();
        } catch (InstantiationException e1) {
          throw new WebDriverException(e);
        } catch (IllegalAccessException e1) {
          throw new WebDriverException(e);
        }
      } catch (InvocationTargetException e) {
        throw new WebDriverException(e);
      } catch (InstantiationException e) {
        throw new WebDriverException(e);
      } catch (IllegalAccessException e) {
        throw new WebDriverException(e);
      }
    }
  }
}
