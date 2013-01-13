/*
 *  Copyright 2013, Peter Johnson
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy
 *  of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */
package org.cafed00d.subtitle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This singleton is responsible for all messages displayed to the user. It
 * handles the quiet and verbose flags, and also logs all messages.
 */
public enum Reporter {

  /**
   * The singleton instance.
   */
  INSTANCE;

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(Reporter.class);

  /**
   * True if the user want to see additional console output.
   */
  private static boolean verbose;

  /**
   * True if the user does not want to see any console output.
   */
  private static boolean quiet;

  /**
   * Sets the verbose and quiet flags.
   * 
   * @param verbose
   *          the verbose flag value. Ignored if <code>quiet</code> is true.
   * @param quiet
   *          the quiet flag value
   */
  public static final void setOptions(boolean verbose, boolean quiet) {
    Reporter.quiet = quiet;
    if (quiet) {
      verbose = false;
    } else {
      Reporter.verbose = verbose;
    }
  }

  /**
   * Displays a message to the user.
   * <p>
   * Also logs the message at the info level.
   * 
   * @param message
   *          The text of the error message.
   */
  public void displayMessage(String message) {
    log.info(message);
    if (!quiet) {
      System.out.println(message);
    }
  }

  /**
   * Displays a verbose message to the user.
   * <p>
   * Also logs the message at the info level.
   * 
   * @param message
   *          The text of the error message.
   */
  public void displayVerboseMessage(String message) {
    log.info(message);
    if (!verbose) {
      System.out.println(message);
    }
  }

  /**
   * Write a single character to the console to denote progress. This is
   * considered to be verbose output.
   * <p>
   * Nothing is written to the log file.
   * 
   * @param major
   *          If true, indicates major progress in which case a "+" is written
   *          to the console. If false, indicates minor progress in which case a
   *          "." is written to the console.
   */
  public void displayProgress(boolean major) {
    if (!verbose) {
      System.out.print(major? '+' : '.');
    }
  }

  /**
   * Displays an error to the user. The displayed error messages used the
   * format: </p>
   * <p>
   * <blockquote>ERROR: &lt;message&gt;</blockquote>
   * </p>
   * <p>
   * Keep this format in mind when construction the error text.
   * <p>
   * Also logs the error at the error level.
   * 
   * @param message
   *          The text of the error message.
   */
  public void displayError(String message) {
    log.error(message);
    if (!quiet) {
      System.err.println("ERROR: " + message);
    }
  }

  /**
   * Displays an error caused by an exception to the user. The displayed error
   * messages used the format: </p>
   * <p>
   * <blockquote>ERROR: &lt;message&gt;: &lt;exception&gt;</blockquote>
   * </p>
   * <p>
   * Keep this format in mind when construction the error message.
   * <p>
   * Also logs the error and exception at the error level.
   * 
   * @param message
   *          The text of the error message. Do not include the exception's
   *          message - it will be appended automatically.
   * @param exception
   *          The exception to log.
   */
  public void displayError(String message, Exception exception) {
    log.error(message, exception);
    if (!quiet) {
      System.err.println("ERROR: " + message + ": " + exception.getLocalizedMessage());
    }
  }
}
