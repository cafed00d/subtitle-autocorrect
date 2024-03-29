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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 */
public class WordProcesorTest {

  /**
   * The phrase that includes numerous OCR misspellings to use for testing.
   */
  static final StringBuilder PHRASE = new StringBuilder(
      "[SlNGlNG] lsn't it a IoveIy day to get caught in the rain. NeaI's l'm l'II lt'II Well All Ioad lnitially This'II seIection Iast lf");

  /**
   * For this test case there is only a single character and it is correct.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_singleCorrect() {
    testProcess("a", null, 1, 0, 0, 0, 0, false);
  }

  /**
   * For this test case the word is correctly spelled.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_correct() {
    testProcess("Well", null, 3, 1, 2, 0, 0, false);
  }

  /**
   * For this test case the word contains an upper case I's to correct.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_upperI() {
    testProcess("seIection", "selection", 8, 1, 0, 1, 0, true);
  }

  /**
   * For this test case the word contains an upper case I to correct and has an
   * apostrophe.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_upperIwithAposthrophe() {
    testProcess("NeaI's", "Neal's", 3, 2, 0, 1, 1, true);
  }

  /**
   * For this test case the word contains lower case l's to correct.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_lowerl() {
    testProcess("SlNGlNG", "SINGING", 2, 5, 2, 0, 0, true);
  }

  /**
   * For this test case the word is in the auto correct properties file.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_dictionary() {
    testProcess("lsn't", "Isn't", 4, 0, 1, 0, 1, true);
  }

  /**
   * For this test case the word starts with an I with the next letter being a
   * vowel.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_initial_I() {
    testProcess("Iast", "last", 3, 1, 0, 0, 0, true);
  }

  /**
   * For this test case the word both contains and start with an upper case I's
   * to correct. This verifies that the converions rules are chained.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_manyUpperI() {
    testProcess("IoveIy", "lovely", 4, 2, 0, 1, 0, true);
  }

  /**
   * For this test case the word starts with an l with the next letter being a
   * consonant.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_initial_l() {
    testProcess("lnitially", "Initially", 9, 0, 3, 0, 0, true);
  }

  /**
   * For this test case the word starts with an l with the next letter being a
   * consonant, where word is two characters long.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_initial_l2() {
    testProcess("lf", "If", 2, 0, 1, 0, 0, true);
  }

  /**
   * For this test case there are multiple corrections going both directions.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_bothWays() {
    testProcess("lt'II", "It'll", 2, 2, 1, 2, 1, true);
  }

  /**
   * For this test case the word ends in 'II
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_apostropheII() {
    testProcess("This'II", "This'll", 3, 3, 0, 2, 1, true);
  }

  /**
   * For this test case the word starts with l'
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_lapostrophe() {
    testProcess("l'm", "I'm", 2, 0, 1, 0, 1, true);
  }

  /**
   * For this test case the word starts with l'
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_Iapostrophell() {
    testProcess("l'II", "I'll", 1, 2, 1, 2, 1, true);
  }

  /**
   * The word All was inadvertently corrected to AII.
   * <p>
   * Test method for {@link org.cafed00d.subtitle.WordProcessor#process()}.
   */
  @Test
  public void testProcess_All() {
    testProcess("All", "All", 2, 1, 2, 0, 0, false);
  }

  /**
   * Helper method used to perform the test and validate the results.
   * 
   * @param word
   *          The word to check.
   * @param correct
   *          The expected corrected word. May be null, in which case
   *          <code>word</code> is used as the correct spelling.
   * @param lower
   *          The expected number of lower case letters.
   * @param upper
   *          The expected number of upper case letters.
   * @param ell
   *          The expected number of lower case l's.
   * @param eye
   *          The expected number of upper case I's. (Note that upper case I's
   *          that start words are not counted.)
   * @param apos
   *          The expected number of apostrophes.
   * @param wrong
   *          True if the test expects the word to be corrected, false it not.
   */
  private void testProcess(String word, String correct, int lower, int upper, int ell, int eye, int apos, boolean wrong) {
    int first = PHRASE.indexOf(word);
    WordProcessor wp = new WordProcessor(PHRASE, first);
    int beyond = wp.process();
    assertEquals("Wrong after index for " + word, first + word.length(), beyond);
    assertEquals("Wrong lower count for " + word, lower, wp.getLowerCount());
    assertEquals("Wrong upper count for " + word, upper, wp.getUpperCount());
    assertEquals("Wrong l count for " + word, ell, wp.getlCount());
    assertEquals("Wrong I count for " + word, eye, wp.getICount());
    assertEquals("Wrong ' count for " + word, apos, wp.getApostropheCount());
    assertEquals("Wrong correction flag for " + word, wrong, wp.isCorrectionMade());
    if (correct == null) {
      correct = word;
    }
    assertEquals("Wrong correction for " + word, correct, PHRASE.substring(first, beyond));
  }

}
