package com.kensai.gui.assertions;

import com.kensai.gui.xml.MarketConnexionDescriptor;
import com.kensai.gui.xml.MarketConnexionDescriptors;


/**
 * Entry point for assertion of different data types. Each method in this class is a static factory for the
 * type-specific assertion objects.
 */
public class Assertions {

  /**
   * Creates a new instance of <code>{@link MarketConnexionDescriptorAssert}</code>.
   *
   * @param actual the actual value.
   * @return the created assertion object.
   */
  public static MarketConnexionDescriptorAssert assertThat(MarketConnexionDescriptor actual) {
    return new MarketConnexionDescriptorAssert(actual);
  }

  /**
   * Creates a new instance of <code>{@link MarketConnexionDescriptorsAssert}</code>.
   *
   * @param actual the actual value.
   * @return the created assertion object.
   */
  public static MarketConnexionDescriptorsAssert assertThat(MarketConnexionDescriptors actual) {
    return new MarketConnexionDescriptorsAssert(actual);
  }

  /**
   * Creates a new </code>{@link Assertions}</code>.
   */
  protected Assertions() {
    // empty
  }
}
