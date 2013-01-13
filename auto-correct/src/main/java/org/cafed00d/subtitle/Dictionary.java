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

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Singleton class that reads the autocorrect.properties file and corrects any
 * words in the SRT file that are found in the autocorrect file.
 * <p>
 * Also handles exception cases where the general algorithms mistakenly assume
 * that a word is misspelled when it is not.
 */
public enum Dictionary {
  /**
   * The singleton instance
   */
  INSTANCE;

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(Dictionary.class);

  /**
   * Maps common incorrect spelling into correct spellings.
   */
  private static Properties map;

  static {
    map = new Properties();
    ClassLoader cl = Dictionary.class.getClassLoader();
    try {
      map.load(cl.getResourceAsStream("autocorrect.properties"));
      // TODO: filter out invalid entries
    } catch (IOException e) {
      Reporter.INSTANCE.displayError("Unable to load autocorrect.properties");
      log.warn(e.getMessage(), e);
    }
  }

  /**
   * Checks if the word is in the autocorrect file and if so corrects it in
   * place in the line.
   * 
   * @param line
   *          The line containing the word. The word is correct in place.
   * @param first
   *          The index of the first letter of the word in <code>line</code>.
   * @param after
   *          The index after the last letter of the word in <code>line</code>.
   * @return True if the word was corrected, false otherwise.
   */
  public boolean spellcheck(StringBuilder line, int first, int after) {
    boolean result = false;
    String word = line.substring(first, after);
    String corrected = (String) map.get(word);
    if (corrected != null) {
      correct(line, first, corrected);
      result = true;
    }
    return result;
  }

  /**
   * Corrects the word in place in <code>line</code>.
   * 
   * @param line
   *          The line containing the word to correct (updated by this method).
   * @param first
   *          The index of the first letter of the word.
   * @param word
   *          The correct spelling of the word. Used to replace the word in
   *          <code>line</code>.
   */
  private void correct(StringBuilder line, int first, String word) {
    for (int i = 0; i < word.length(); i++) {
      line.setCharAt(first + i, word.charAt(i));
    }
  }

  /**
   * Checks to see if this word is one that would be mistakenly labeled as a
   * misspelling by the {@link WordProcessor} algorithms.
   * <p>
   * Exception cases are identified by having a property key but no value.
   * 
   * @param word
   *          The word to check.
   * @return True if is an exception case, false if not.
   */
  public boolean exceptionCase(String word) {
    String value = map.getProperty(word);
    boolean result = (value != null) && (value.isEmpty());
    if (result) {
      log.debug("Encountered exception case: " + word);
    }
    return result;
  }
}
