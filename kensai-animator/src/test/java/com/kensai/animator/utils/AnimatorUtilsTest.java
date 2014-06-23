package com.kensai.animator.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class AnimatorUtilsTest {

	@Test
	public void should_round_do_nothing_if_price_is_already_rounded() {
		// GIVEN: Price
		double price = 25.48;

		// WHEN: round
		double rounded = AnimatorUtils.round(price);

		// THEN: price are same
		assertThat(rounded).isEqualTo(price);
	}

	@Test
	public void should_round_down() {
		// GIVEN: Price
		double price = 25.481;

		// WHEN: round
		double rounded = AnimatorUtils.round(price);

		// THEN: price are same
		assertThat(rounded).isEqualTo(25.48);
	}

	@Test
	public void should_round_up() {
		// GIVEN: Price
		double price = 25.489;

		// WHEN: round
		double rounded = AnimatorUtils.round(price);

		// THEN: price are same
		assertThat(rounded).isEqualTo(25.49);
	}
}
