/*
 * Copyright (C) 2020 Deque Systems Inc.,
 *
 * Your use of this Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This entire copyright notice must appear in every copy of this file you
 * distribute or in any file that contains substantial portions of this source
 * code.
 */

package com.deque.html.axecore.extensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.OperationNotSupportedException;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.deque.html.axecore.providers.IAxeScriptProvider;

/**
 * Holds the Web driver injection extension methods.
 */
public final class WebDriverInjectorExtensions {
  /**
   * initializes the WebDriverInjectorExtensions class.
   */
  private WebDriverInjectorExtensions() {
  }

  /**
   * Execute an asynchronous JavaScript command.
   *
   * @param webDriver for the page to be scanned
   * @param command The command to be executed.
   * @param args Additional arguments to be provided to the command.
   * @return the results that would normally be provided to the asynchronous commands callback.
   */
  public static Object executeAsyncScript(final WebDriver webDriver, final String command,
      final Object... args) {
    return ((JavascriptExecutor) webDriver).executeAsyncScript(command, args);
  }

  /**
   * Injects Axe script into frames.
   * If a frame (not top-level) errors when injecting due to not being displayed, the error is ignored.
   * @param driver WebDriver instance to inject into
   * @param scriptProvider Provider that get the aXe script to inject
   * @throws OperationNotSupportedException if the operation errors out
   * @throws IOException if an IO exception occurs
   */
  public static void inject(final WebDriver driver,
      final IAxeScriptProvider scriptProvider, boolean disableIframeTesting)
      throws OperationNotSupportedException, IOException {
    if (scriptProvider == null) {
      throw new NullPointerException("the Script provider is null");
    }

    String script = scriptProvider.getScript();
    inject(driver, script, disableIframeTesting);
  }

  /**
   * Injects Axe script into frames.
   * If a frame (not top-level) errors when injecting due to not being displayed, the error is ignored.
   * @param driver WebDriver instance to inject into
   * @param script The script to inject
   */
  public static void inject(final WebDriver driver,
      final String script, boolean disableIframeTesting) {
    List<WebElement> parents = new ArrayList<>();
    JavascriptExecutor js = (JavascriptExecutor) driver;

    driver.switchTo().defaultContent();
    js.executeScript(script);
    if (!disableIframeTesting) {
      try {
        injectIntoFrames(driver, script, parents);
      } catch (Exception e) {
        // Ignore all errors except those caused by the injected javascript itself
        if (e instanceof JavascriptException) {
          throw e;
        }
      }
      driver.switchTo().defaultContent();
    }
  }

  /**
   * Injects script into frames to be run asynchronously.
   * @param driver WebDriver instance to inject into
   * @param script The script to inject
   */
  public static void injectAsync(final WebDriver driver,
      final String script, boolean disableIframeTesting) {
    List<WebElement> parents = new ArrayList<>();
    JavascriptExecutor js = (JavascriptExecutor) driver;

    driver.switchTo().defaultContent();
    js.executeAsyncScript(script);

    if (!disableIframeTesting) {
      try {
        injectIntoFramesAsync(driver, script, parents);
      } catch (Exception e) {
        // Ignore all errors except those caused by the injected javascript itself
        if (e instanceof JavascriptException) {
          throw e;
        }
      }
      driver.switchTo().defaultContent();
    }
  }

  /**
   * Recursively find frames and inject a script into them.
   * If a frame errors when injecting due to not being displayed, the error is ignored.
   * @param driver An initialized WebDriver
   * @param script Script to inject
   * @param parents A list of all top level frames
   */
  private static void injectIntoFrames(final WebDriver driver,
      final String script, final List<WebElement> parents) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    List<WebElement> frames = driver.findElements(By.tagName("iframe"));
    frames.addAll(driver.findElements(By.tagName("frame")));

    for (WebElement frame : frames) {
      try {
        driver.switchTo().defaultContent();
        if (parents != null) {
          for (WebElement parent : parents) {
            driver.switchTo().frame(parent);
          }
        }

        driver.switchTo().frame(frame);

        js.executeScript(script);

        List<WebElement> localParents = new ArrayList<>();

        if (parents == null) {
          localParents.add(null);
          throw new NullPointerException();
        } else {
          localParents.addAll(parents);
          localParents.add(frame);
        }

        injectIntoFrames(driver, script, localParents);
      } catch (Exception e) {
        // Ignore all errors except those caused by the injected javascript itself
        if (e instanceof JavascriptException) {
          throw e;
        }
      }
    }
  }

  /**
   * Recursively find frames and inject a script into them to be run asynchronously.
   * If a frame errors when injecting due to not being displayed, the error is ignored.
   * @param driver An initialized WebDriver
   * @param script Script to inject
   * @param parents A list of all top level frames
   */
  private static void injectIntoFramesAsync(final WebDriver driver,
      final String script, final List<WebElement> parents) {
    JavascriptExecutor js = (JavascriptExecutor) driver;
    List<WebElement> frames = driver.findElements(By.tagName("iframe"));
    frames.addAll(driver.findElements(By.tagName("frame")));

    for (WebElement frame : frames) {
      try {
        driver.switchTo().defaultContent();

        if (parents != null) {
          for (WebElement parent : parents) {
            driver.switchTo().frame(parent);
          }
        }

        driver.switchTo().frame(frame);

        js.executeAsyncScript(script);

        List<WebElement> localParents = new ArrayList<>();

        if (parents == null) {
          localParents.add(null);
          throw new NullPointerException();
        } else {
          localParents.addAll(parents);
          localParents.add(frame);
        }

        injectIntoFrames(driver, script, localParents);
      } catch (Exception e) {
        // Ignore all errors except those caused by the injected javascript itself
        if (e instanceof JavascriptException) {
          throw e;
        }
      }
    }
  }
}
