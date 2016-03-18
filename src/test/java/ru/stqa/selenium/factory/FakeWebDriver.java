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

import org.openqa.selenium.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FakeWebDriver implements WebDriver {

  private Capabilities capabilities;
  private boolean isActive = true;

  public FakeWebDriver(Capabilities capabilities) {
    this.capabilities = capabilities;
  }

  public boolean isActive() {
    return isActive;
  }

  @Override
  public void get(String s) {
  }

  @Override
  public String getCurrentUrl() {
    return null;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public List<WebElement> findElements(By by) {
    return null;
  }

  @Override
  public WebElement findElement(By by) {
    return null;
  }

  @Override
  public String getPageSource() {
    return null;
  }

  @Override
  public void close() {
  }

  @Override
  public void quit() {
    isActive = false;
  }

  @Override
  public Set<String> getWindowHandles() {
    if (isActive) {
      return new HashSet<String>(Arrays.asList("12345"));
    } else {
      throw new WebDriverException("closed");
    }
  }

  @Override
  public String getWindowHandle() {
    return null;
  }

  @Override
  public TargetLocator switchTo() {
    return null;
  }

  @Override
  public Navigation navigate() {
    return null;
  }

  @Override
  public Options manage() {
    return null;
  }
}
