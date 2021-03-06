package com.kensai.gui.assertions;

import org.assertj.core.api.AbstractAssert;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.SummaryModel;

// Assertions is needed if an assertion for Iterable is generated


/**
 * {@link InstrumentModel} specific assertions - Generated by CustomAssertionGenerator.
 */
public class InstrumentModelAssert extends AbstractAssert<InstrumentModelAssert, InstrumentModel> {

  /**
   * Creates a new </code>{@link InstrumentModelAssert}</code> to make assertions on actual InstrumentModel.
   * @param actual the InstrumentModel we want to make assertions on.
   */
  public InstrumentModelAssert(InstrumentModel actual) {
    super(actual, InstrumentModelAssert.class);
  }

  /**
   * An entry point for InstrumentModelAssert to follow AssertJ standard <code>assertThat()</code> statements.<br>
   * With a static import, one's can write directly : <code>assertThat(myInstrumentModel)</code> and get specific assertion with code completion.
   * @param actual the InstrumentModel we want to make assertions on.
   * @return a new </code>{@link InstrumentModelAssert}</code>
   */
  public static InstrumentModelAssert assertThat(InstrumentModel actual) {
    return new InstrumentModelAssert(actual);
  }

  /**
   * Verifies that the actual InstrumentModel's connectionName is equal to the given one.
   * @param connectionName the given connectionName to compare the actual InstrumentModel's connectionName to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's connectionName is not equal to the given one.
   */
  public InstrumentModelAssert hasConnectionName(String connectionName) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected connectionName of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualConnectionName = actual.getConnectionName();
    if (!org.assertj.core.util.Objects.areEqual(actualConnectionName, connectionName)) {
      failWithMessage(assertjErrorMessage, actual, connectionName, actualConnectionName);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's description is equal to the given one.
   * @param description the given description to compare the actual InstrumentModel's description to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's description is not equal to the given one.
   */
  public InstrumentModelAssert hasDescription(String description) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected description of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualDescription = actual.getDescription();
    if (!org.assertj.core.util.Objects.areEqual(actualDescription, description)) {
      failWithMessage(assertjErrorMessage, actual, description, actualDescription);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's isin is equal to the given one.
   * @param isin the given isin to compare the actual InstrumentModel's isin to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's isin is not equal to the given one.
   */
  public InstrumentModelAssert hasIsin(String isin) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected isin of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualIsin = actual.getIsin();
    if (!org.assertj.core.util.Objects.areEqual(actualIsin, isin)) {
      failWithMessage(assertjErrorMessage, actual, isin, actualIsin);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's market is equal to the given one.
   * @param market the given market to compare the actual InstrumentModel's market to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's market is not equal to the given one.
   */
  public InstrumentModelAssert hasMarket(String market) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected market of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualMarket = actual.getMarket();
    if (!org.assertj.core.util.Objects.areEqual(actualMarket, market)) {
      failWithMessage(assertjErrorMessage, actual, market, actualMarket);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's name is equal to the given one.
   * @param name the given name to compare the actual InstrumentModel's name to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's name is not equal to the given one.
   */
  public InstrumentModelAssert hasName(String name) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected name of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualName = actual.getName();
    if (!org.assertj.core.util.Objects.areEqual(actualName, name)) {
      failWithMessage(assertjErrorMessage, actual, name, actualName);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's summary is equal to the given one.
   * @param summary the given summary to compare the actual InstrumentModel's summary to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's summary is not equal to the given one.
   */
  public InstrumentModelAssert hasSummary(SummaryModel summary) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected summary of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    SummaryModel actualSummary = actual.getSummary();
    if (!org.assertj.core.util.Objects.areEqual(actualSummary, summary)) {
      failWithMessage(assertjErrorMessage, actual, summary, actualSummary);
    }

    // return the current assertion for method chaining
    return this;
  }

  /**
   * Verifies that the actual InstrumentModel's type is equal to the given one.
   * @param type the given type to compare the actual InstrumentModel's type to.
   * @return this assertion object.
   * @throws AssertionError - if the actual InstrumentModel's type is not equal to the given one.
   */
  public InstrumentModelAssert hasType(String type) {
    // check that actual InstrumentModel we want to make assertions on is not null.
    isNotNull();

    // overrides the default error message with a more explicit one
    String assertjErrorMessage = "\nExpected type of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";
    
    // null safe check
    String actualType = actual.getType();
    if (!org.assertj.core.util.Objects.areEqual(actualType, type)) {
      failWithMessage(assertjErrorMessage, actual, type, actualType);
    }

    // return the current assertion for method chaining
    return this;
  }

}
