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

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The main class for the utility that auto-corrects spelling mistakes made by
 * <a href="http://www.videohelp.com/tools/SupRip">SupRip</a> when it converts a
 * <code>.sub</code> int a <code>.srt</code> file. The most common mistake is
 * that SupRip confuses lower case "l" with upper case "I" which is apparent
 * with dialog words like "Isn't" (which gets converted to "lsn't") and in
 * descriptive terms that appear in all upper case letters such as "[SlNGlNG]".
 * <p>
 * This utility takes several approaches to correcting these mistakes:
 * <ol>
 * <li>If a word is mostly upper case except for lower case l's, then the lower
 * case l's are converted to upper case I's.</li>
 * <li>If a word is mostly lower case letters except for upper case I's
 * appearing in any position other than the first letter, then the offending
 * upper case I's are converted to lower case l's.</li>
 * <li>If a word appears in the common mistakes list, it is corrected. The
 * common mistakes list can be found in the autocorrect.properties file that
 * comes with the utility. It is in the JAR file but you can extract it into the
 * utilities base directory and edit that file to provide for additional cases.</li>
 * </ol>
 * <p>
 * You should run this utility before loading the <code>.srt</code> file into <a
 * href="http://www.nikse.dk/subtitleedit">Subtitle Edit</a> and running a spell
 * check.
 * <p>
 * For a description of the process of extracting subtitles from a Blu-ray dis,
 * see <a href=
 * "http://forum.videohelp.com/threads/313620-How-to-extract-subtitles-from-a-Blu-ray-and-convert-to-srt-or-sub-idx"
 * >How to extract subtitles from Blu-ray</a>.
 * <p>
 * <b>Usage:</b> <code>autocorrect [-&lt;options&gt;] &lt;sup-file(s)&gt;</code>
 * <p>
 * where <code>&lt;options&gt;</code> is one of:
 * <table>
 * <tr>
 * <th>Option</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <th>v</th>
 * <td>Outputs additional information while processing the files. Ignored if the
 * "q" option is also present.</td>
 * </tr>
 * <tr>
 * <th>q</th>
 * <td>Suppresses program output while the files are being processed, including
 * error messages. Note that this option does not kick in until the options
 * passed are validated, thus error messages regarding invalid command line
 * options will be displayed regardless of this setting.</td>
 * </tr>
 * <tr>
 * <th>a</th>
 * <td>Generates a LOG file which contains all words converted. The format of
 * the file is the same as the autocorrect.properties file and contains only
 * unique entries. The base name is the same as for the SRT file.</td>
 * </tr>
 * </table>
 * <p>
 * The utility accepts one of more <code>*.srt</code> files and processes each
 * once. It first backs up the <code>*.srt</code> file as <code>*.bak</code> and
 * then creates a new <code>.srt</code> file the corrected text.
 * <p>
 * <b>Example</b>
 * 
 * <pre>
 * autocorrect c:/users/john/desktop/movie.srt
 * </pre>
 * <p>
 * Auto-corrects the <code>movie.srt</code> file on John's desktop, and backs up
 * the original file as <code>movie.bak</code> on John's desktop.
 * 
 * <pre>
 * autocorrect -av TopHat.srt Casablanca.srt
 * </pre>
 * <p>
 * Auto-corrects the <code>TopHat.srt</code> and <code>Casablanca.srt</code>
 * files in the current directory, and backs up the original files as
 * <code>TopHat.bak</code> and <code>Casablanca.bak</code>in the current
 * directory. Also outputs additional information to the command window while
 * processing the file and generates the <code>TopHat.log</code> and
 * <code>Casablanca.log</code> files in the current directory.
 */
public class AutoCorrect {

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(AutoCorrect.class);

  /**
   * If true, runs in quiet mode. Set if <code>-q</code> option passed.
   */
  private boolean quietMode = false;

  /**
   * If true, runs in verbose mode. Set if <code>-v</code> option passed.
   */
  private boolean verboseMode = false;

  /**
   * If true, generates an corrections log file. Set if <code>-a</code> option
   * passed.
   */
  private boolean generateLog = false;

  /**
   * The files to process.
   */
  private ArrayList<File> files = new ArrayList<File>();

  /**
   * Main body of program.
   * 
   * @param args
   *          The arguments passed by the user. See the class comment for
   *          allowed values.
   */
  public static void main(String[] args) {
    AutoCorrect ac = new AutoCorrect();
    if (ac.validate(args)) {
      ac.process();
    } else {
      displayUsage();
    }
  }

  /**
   * Displays usage instructions to the user.
   */
  private static void displayUsage() {
    Reporter.INSTANCE.displayMessage("Usage: autocorrect [-aqv] srt-file(s)");
    Reporter.INSTANCE.displayMessage("Where:");
    Reporter.INSTANCE.displayMessage("  -a  Generate autocorrect.log file");
    Reporter.INSTANCE.displayMessage("  -q  Run in quite mode: suppress all output");
    Reporter.INSTANCE.displayMessage("  -v  Run in verbose mode: output additonal info");
    Reporter.INSTANCE.displayMessage("  str-file(s)  One of more subtitle files, space separated");
  }

  /**
   * Validates the arguments that were passed. Examines the command options to
   * see which ones were selected. Verifies that a file was passed and that it
   * is writable.
   * <p>
   * This method does not care what order the options and files appear. Every
   * argument that starts with a dash (-) must contains only valid option
   * letters, and any other arguments must be writable files.
   * 
   * @param args
   *          The arguments that were passed to the utility.
   * @return True if the arguments are acceptable. False if an invalid option
   *         was passed (thereby allowing -? to be passed to get the usage) or
   *         no writable file given.
   */
  private boolean validate(String[] args) {
    boolean result = true;
    if (args.length > 0) {
      for (String arg : args) {
        if (arg.startsWith("-")) {
          if (!validateOptions(arg)) {
            result = false;
          }
        } else if (!validateFile(arg)) {
          result = false;
        }
      }
    }

    /*
     * If there were no errors configure the reporter.
     */
    if (result) {
      Reporter.setOptions(verboseMode, quietMode);
    }

    return result;
  }

  /**
   * Validates the options given.
   * 
   * @param arg
   *          An argument containing options to validate.
   * @return True iff the options are acceptable.
   */
  private boolean validateOptions(String arg) {
    log.debug("options=" + arg);
    boolean result = true;
    if (arg.length() == 0) {
      result = false;
      Reporter.INSTANCE.displayError("no options given in argument " + arg);
    } else {
      char[] chars = arg.toCharArray();
      for (char c : chars) {
        switch (c) {
          case 'a':
          case 'A':
            generateLog = true;
            log.debug("found generateLog option");
            break;

          case 'v':
          case 'V':
            verboseMode = true;
            log.debug("found vebose option");
            break;

          case 'q':
          case 'Q':
            quietMode = true;
            log.debug("found quiet option");
            break;

          default:
            Reporter.INSTANCE.displayError("unknown option " + c + " in argument " + arg);
            result = false;
            break;
        }
      }
    }
    return result;
  }

  /**
   * Validates the file given.
   * 
   * @param arg
   *          An argument containing a file name to validate.
   * @return True iff the named file exists and is writable.
   */
  private boolean validateFile(String arg) {
    boolean result = false;
    File file = new File(arg).getAbsoluteFile();
    if (file.exists()) {
      if (file.isFile()) {
        if (file.canWrite()) {
          files.add(file);
          result = true;
        } else {
          Reporter.INSTANCE.displayError("file is not writeable: " + file.getAbsolutePath());
        }
      } else {
        Reporter.INSTANCE.displayError("cannot convert a directory: " + file.getAbsolutePath());
      }
    } else {
      Reporter.INSTANCE.displayError("no such file: " + file.getAbsolutePath());
    }
    return result;
  }

  /**
   * Processes the SRT files.
   */
  private void process() {
    for (File file : files) {
      FileProcessor fp = new FileProcessor(file, generateLog);
      fp.process();
    }
  }
}
