package ru.stqa.selenium.factory;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.server.DriverProvider;

public class WebDriverFactory {

  private static WebDriverFactoryInternal factoryInternal = new ThreadLocalSingletonStorage();

  public static void setMode(WebDriverFactoryMode newMode) {
    if (! factoryInternal.isEmpty()) {
      throw new Error("Mode can't be changed because there are active WebDriver instances");
    }
    factoryInternal = createFactoryInternal(newMode);
  }

  public static void setDriverAlivenessChecker(DriverAlivenessChecker alivenessChecker) {
    factoryInternal.setDriverAlivenessChecker(alivenessChecker);
  }

  private static WebDriverFactoryInternal createFactoryInternal(WebDriverFactoryMode mode) {
    switch (mode) {
      case SINGLETON:
        return new SingletonStorage();
      case THREADLOCAL_SINGLETON:
        return new ThreadLocalSingletonStorage();
      case UNRESTRICTED:
        return new UnrestrictedStorage();
      default:
        throw new Error("Unsupported browser factory mode: " + mode);
    }
  }

  public static void addDriverProvider (DriverProvider provider) {
    factoryInternal.addDriverProvider(provider);
  }

  public static void addRemoteDriverProvider (RemoteDriverProvider provider) {
    factoryInternal.addRemoteDriverProvider(provider);
  }

  public static void setDefaultHub(String defaultHub) {
    factoryInternal.setDefaultHub(defaultHub);
  }

  public static WebDriver getDriver(String hub, Capabilities capabilities) {
    return factoryInternal.getDriver(hub, capabilities);
  }

  public static WebDriver getDriver(Capabilities capabilities) {
    return factoryInternal.getDriver(capabilities);
  }

  public static void dismissDriver(WebDriver driver) {
    factoryInternal.dismissDriver(driver);
  }

  public static void dismissAll() {
    factoryInternal.dismissAll();
  }

  public static boolean isEmpty() {
    return factoryInternal.isEmpty();
  }

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        factoryInternal.dismissAll();
      }
    });
  }

}
