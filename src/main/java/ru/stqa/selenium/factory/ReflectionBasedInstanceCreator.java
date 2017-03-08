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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionBasedInstanceCreator {

  private String driverClassName;

  public ReflectionBasedInstanceCreator(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  private Class<? extends WebDriver> getDriverClass() {
    try {
      return Class.forName(driverClassName).asSubclass(WebDriver.class);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      throw new DriverCreationError("Driver class not found: " + driverClassName, e);
    } catch (UnsupportedClassVersionError e) {
      throw new DriverCreationError("Driver class is built for higher Java version: " + driverClassName, e);
    }
  }

  public WebDriver createDriver(Capabilities capabilities) {
    return callConstructor(getDriverClass(), capabilities);
  }

  private WebDriver callConstructor(Class<? extends WebDriver> from, Capabilities capabilities) {
    try {
      Constructor<? extends WebDriver> constructor = from.getConstructor(Capabilities.class);
      return constructor.newInstance(capabilities);
    } catch (NoSuchMethodException e) {
      try {
        return from.newInstance();
      } catch (InstantiationException | IllegalAccessException e1) {
        throw new DriverCreationError(e);
      }
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new DriverCreationError(e);
    }
  }
}
