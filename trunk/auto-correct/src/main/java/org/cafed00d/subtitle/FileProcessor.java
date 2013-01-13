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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Processes a single SRT file, auto-correcting common OCR mistakes.
 */
public class FileProcessor {

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(FileProcessor.class);

  /**
   * If true, generates an corrections log file.
   */
  private boolean generateLog = false;

  /**
   * The file being processed.
   */
  private File infile;

  /**
   * The file containing the auto-corrections.
   */
  private File outfile;

  /**
   * Records the number of lines processed.
   */
  private int lineCount;

  /**
   * Records the number of words processed.
   */
  private int wordCount;

  /**
   * Records the number of words corrected.
   */
  private int correctedCount;

  /**
   * If the user want a log of corrections, we'll keep track of the corrections
   * in this map and create the log after we are done.
   */
  private TreeMap<String, String> correctedWords = new TreeMap<String, String>();

  /**
   * Constructor
   * 
   * @param file
   *          The SRT file to process.
   * @param generateLog
   *          If true, generate a log file listing changed words.
   */
  public FileProcessor(File file, boolean generateLog) {
    this.generateLog = generateLog;
    this.infile = file.getAbsoluteFile();
  }

  /**
   * Processes the file by:
   * <ol>
   * <li>Backing up the file via a rename</li>
   * <li>Recreating the file and copying the contents over</li>
   * </ol>
   */
  public void process() {
    String fileName = infile.getAbsolutePath();
    log.info("processing file: " + fileName);
    String backupFileName = generateFileName(infile, ".bak");
    log.info("backing up as: " + backupFileName);
    File backupFile = new File(backupFileName);
    if (backupFile.exists()) {
      backupFile.delete();
    }
    if (!infile.renameTo(new File(backupFileName))) {
      Reporter.INSTANCE.displayError("Unable to rename file " + fileName + " to " + backupFileName);
    } else {
      Reporter.INSTANCE.displayMessage("Correcting " + fileName);
      outfile = new File(fileName);
      infile = new File(backupFileName);
      try {
        copyContents();
      } catch (Exception e) {
        Reporter.INSTANCE.displayError("Error while processing file " + fileName + ": " + e.getMessage());
        log.error(e.getMessage(), e);
      }
      reportStatistics();
      if (generateLog) {
        reportCorrections();
      }
    }
  }

  /**
   * Generates an absolute file name with the given extension using the given
   * file name as the base. Follows these conventions:
   * <ul>
   * <li>If the simple file name contains no dots, appends
   * <code>extension</code></li>
   * <li>If the simple file name contains a dot only as the first character (ex:
   * .log), appends <code>extension</code></li>
   * <li>If the simple file names has a name part and an extension part (the
   * extension part is the part after the last dot), then it replaces the
   * existing extension with <code>extension</code></li>
   * </ul>
   * <p>
   * Note the assumption that file names will not end in a dot.
   * 
   * @param file
   *          The file for which to generate a name.
   * @return The generate absolute path name.
   */
  private String generateFileName(File file, String extension) {
    StringBuilder backupName = new StringBuilder(file.getName());
    int inx = backupName.toString().lastIndexOf('.');
    if (inx > 0) {
      backupName.setLength(inx);
    }
    backupName.append(extension);
    String fullName = file.getParent() + File.separator + backupName.toString();
    return fullName;
  }

  /**
   * Copies the contents from the backup file to the newly-created file with the
   * original file's name one line at a time. Calls {@link #processLine(String)}
   * for each line.
   * 
   * @throws Exception
   *           Something went wrong.
   */
  private void copyContents() throws Exception {
    PrintStream out = null;
    BufferedReader in = null;
    try {
      out = new PrintStream(new FileOutputStream(outfile));
      in = new BufferedReader(new FileReader(infile));
      String line = null;
      while ((line = in.readLine()) != null) {
        lineCount++;
        log.debug("***Line #" + lineCount + ": " + line);
        try {
          out.println(processLine(line));
        } catch (Exception e) {
          Reporter.INSTANCE.displayError("Error encountered processing line #"
              + lineCount
              + ", may be only partially corrected: "
              + line);
          log.error(e.getMessage(), e);
          out.println(line);
        }
      }
    } finally {
      if (out != null) {
        out.close();
      }
      if (in != null) {
        in.close();
      }
    }
  }

  /**
   * Examines the line of text given looking for words. It recognizes that a
   * word is starting when it comes across a letter. Once it finds a letter, it
   * passes control to {@link WordProcessor} which will extract the current word
   * and determine if it can be corrected.
   * 
   * @param line
   *          The line of text to process.
   * @return The processed, corrected, line of text.
   */
  private String processLine(String line) {
    StringBuilder result = new StringBuilder(line);
    for (int i = 0; i < result.length(); i++) {
      if (Character.isLetter(result.charAt(i))) {
        WordProcessor word = new WordProcessor(result, i);
        i = word.process();
        if (word.isCorrectionMade()) {
          correctedCount++;
          if (generateLog) {
            if (!correctedWords.containsKey(word.getOriginalWord())) {
              correctedWords.put(word.getOriginalWord(), word.getCorrectedWord());
            }
          }
        }
        wordCount++;
      }
    }
    return result.toString();
  }

  /**
   * Display statistics for the file that was processed.
   */
  private void reportStatistics() {
    Reporter.INSTANCE.displayMessage("# Lines: " + lineCount);
    Reporter.INSTANCE.displayMessage("# Words: " + wordCount);
    Reporter.INSTANCE.displayMessage("# Corrections: " + correctedCount);
  }

  /**
   * Generated the corrections log file.
   */
  private void reportCorrections() {
    File corFile = new File(generateFileName(infile, ".log"));
    PrintStream out = null;
    try {
      out = new PrintStream(new FileOutputStream(corFile));
      Set<String> keys = correctedWords.keySet();
      for (String key : keys) {
        String value = correctedWords.get(key);
        out.println(key + "=" + value);
      }
    } catch (FileNotFoundException e) {
      Reporter.INSTANCE.displayError("Unable to create corrections log file", e);
    } finally {
      if (out != null) {
        out.close();
      }
    }

  }
}
