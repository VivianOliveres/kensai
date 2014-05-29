package com.kensai.gui.assertions;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.SummaryModel;
import com.kensai.gui.xml.MarketConnectionDescriptor;
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
  public static MarketConnexionDescriptorAssert assertThat(MarketConnectionDescriptor actual) {
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
	 * Creates a new instance of <code>{@link InstrumentModelAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static InstrumentModelAssert assertThat(InstrumentModel actual) {
		return new InstrumentModelAssert(actual);
	}

	/**
	 * Creates a new instance of <code>{@link SummaryModelAssert}</code>.
	 *
	 * @param actual the actual value.
	 * @return the created assertion object.
	 */
	public static SummaryModelAssert assertThat(SummaryModel actual) {
		return new SummaryModelAssert(actual);
	}

  /**
   * Creates a new </code>{@link Assertions}</code>.
   */
  protected Assertions() {
    // empty
  }
}
