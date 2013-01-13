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
 * This class knows how to process a word to determine if it can be corrected
 * and also corrects the word. A word is defined as a contiguous series of
 * letters and embedded apostrophes (').
 * <p>
 * Note that this class processes the word in the context of the line being
 * processed.
 */
public class WordProcessor {

  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(WordProcessor.class);

  /**
   * The line being processed.
   */
  private StringBuilder line;

  /**
   * The index of the first letter of the word to process.
   */
  private int first;

  /**
   * Used as the index to scan the word in the line.
   */
  private int current;

  /**
   * Statistic: the number of upper case letters in the word.
   */
  private int upperCount;

  /**
   * Statistic: the number of lower case letters in the word.
   */
  private int lowerCount;

  /**
   * Statistic: the number of apostrophes in the word.
   */
  private int apostropheCount;

  /**
   * Statistic: the number of lower case l's in the word.
   */
  private int lCount;

  /**
   * Statistic: the number of upper case I's in the word.
   */
  private int ICount;

  /**
   * Set to true if a correction was made to the word. Used by logging, if the
   * word was corrected its correction is logged.
   */
  private boolean correctionMade;

  /**
   * The original word.
   */
  private String originalWord;

  /**
   * The corrected word. Is null if the word was not corrected.
   */
  private String correctedWord;

  /**
   * Gets the flag as to whether a correction was made.
   * 
   * @return the correctionMade flag
   */
  public final boolean isCorrectionMade() {
    return correctionMade;
  }

  /**
   * Gets the original word.
   * 
   * @return the original word
   */
  public final String getOriginalWord() {
    return originalWord;
  }

  /**
   * Gets the corrected word
   * 
   * @return the corrected word or null is the original word was not corrected
   */
  public final String getCorrectedWord() {
    return correctedWord;
  }

  /**
   * Constructor.
   * 
   * @param line
   *          The line of text containing the word to process.
   * @param first
   *          The index of the fist letter in the word.
   */
  public WordProcessor(StringBuilder line, int first) {
    this.line = line;
    this.first = first;
    this.current = first;
  }

  /**
   * Processes the word, correcting it if needed.
   * 
   * @return The index after the last letter in the word.
   */
  public int process() {
    gatherStatistics();
    log.debug("=> " + this);
    originalWord = line.substring(first, current);

    /*
     * There are some words that are exceptions to the general algorithms, so
     * first check is we have one of those words and ignore it if we do.
     */
    if (!Dictionary.INSTANCE.exceptionCase(originalWord)) {
      fixMismatch();
      if (!correctionMade) {
        fixMisspelling();
      }
    }
    if (correctionMade) {
      correctedWord = line.substring(first, current);
      log.debug("=> fixed: " + line.substring(first, current));
    }
    return current;
  }

  /**
   * Examines the word and gather various statistics.
   */
  private void gatherStatistics() {
    while (current < line.length() && (Character.isLetter(line.charAt(current)) || line.charAt(current) == '\'')) {
      char ch = line.charAt(current);
      log.trace("@" + current + ":" + ch);
      if (Character.isUpperCase(ch)) {
        upperCount++;

        /*
         * We are interested in upper case I's only if they are not at the first
         * character position. Thus we will ignore words such as "I'll" and
         * words that start sentences.
         */
        if (current != first && ch == 'I') {
          ICount++;
        }
      } else if (ch == '\'') {
        apostropheCount++;
      } else {
        lowerCount++;
        if (ch == 'l') {
          lCount++;
        }
      }
      current++;
    }
  }

  /**
   * Looks at the statistics to determine if we should try correcting I's and
   * l's.
   */
  private void fixMismatch() {
    int size = current - first;
    if (ICount > 0 || lCount > 0) {

      /*
       * If the word is mostly lower case with some upper case I's, we will
       * correct it. Note that we ignore the first letter when comparing the
       * size - it might have been upper case.
       */
      if (ICount > 0 && ICount + lowerCount >= size - 1) {
        fixUpperI();
      }

      /*
       * If the word is made up of upper case letters and lower case l's (), we
       * will correct it.
       */
      else if (lCount > 0 && lCount + upperCount == size) {
        fixLowerl();
      }
    }
  }

  /**
   * We have determined that the word contains one or more lower case l's that
   * should be upper case I's. Convert them.
   */
  private void fixLowerl() {
    log.trace("fixing lower l");
    for (int i = first; i < current; i++) {
      if (line.charAt(i) == 'l') {
        line.setCharAt(i, 'I');
        log.trace("l->I @ " + i);
        correctionMade = true;
      }
    }
  }

  /**
   * We have determined that the word contains one or more upper case I's that
   * should be lower case l's. Convert them.
   */
  private void fixUpperI() {
    log.trace("fixing upper I");
    /*
     * Ignore the first letter; the word could be the start if a sentence or a
     * contraction for the pronoun I.
     */
    for (int i = first + 1; i < current; i++) {
      if (line.charAt(i) == 'I') {
        line.setCharAt(i, 'l');
        log.trace("I->l @ " + i);
        correctionMade = true;
      }
    }
  }

  /**
   * Compares the word to the auto correct properties contents and if the word
   * appears, corrects it.
   */
  private void fixMisspelling() {
    correctionMade = Dictionary.INSTANCE.spellcheck(line, first, current);
  }

  /**
   * Returns a string the includes the extracted word and its statistics. Note
   * that the output is really only meaningful after the word and its statistics
   * have been gathered.
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return String.format("%s (@:%d, X:%d, x:%d, I:%d, l:%d, ':%d)",
                         line.substring(first, current),
                         first,
                         upperCount,
                         lowerCount,
                         ICount,
                         lCount,
                         apostropheCount);
  }

  // ===========================================================================
  //
  // Unit test helper methods. These methods are not intended for general use.
  //
  // ===========================================================================

  /**
   * Gets the number of upper case letters found.
   * 
   * @return the upperCount
   */
  final int getUpperCount() {
    return upperCount;
  }

  /**
   * Gets the number of upper case letters found.
   * 
   * @return the lowerCount
   */
  final int getLowerCount() {
    return lowerCount;
  }

  /**
   * Gets the number of apostrophes found.
   * 
   * @return the apostropheCount
   */
  final int getApostropheCount() {
    return apostropheCount;
  }

  /**
   * Gets the number of lower case l's found.
   * 
   * @return the lCount
   */
  final int getlCount() {
    return lCount;
  }

  /**
   * Gets the number of upper case I's found.
   * 
   * @return the ICount
   */
  final int getICount() {
    return ICount;
  }
}
