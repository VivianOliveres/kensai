package com.kensai.market.persist;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.kensai.market.core.InstrumentDepth;

public class InstrumentDepthsLoaderTest {

	private InstrumentDepthsLoader loader;

	@Before
	public void init() {
		loader = new InstrumentDepthsLoader();
	}

	@Test
	public void should_parse_line_for_AC() {
		// GIVEN: CSV line
		// #Name;Raw;Isin;Last;Close;High;Low;Open
		String line = "Accor;AC;FR0000120404;25.48;24.77;25.49;24.86;24.77";

		// WHEN: parseLine
		InstrumentDepth depth = loader.parseLine(line);

		// THEN: depth is valid
		assertThat(depth.getLast()).isEqualTo(25.48);
		assertThat(depth.getClose()).isEqualTo(24.77);
		assertThat(depth.getOpen()).isEqualTo(24.77);
	}
}
