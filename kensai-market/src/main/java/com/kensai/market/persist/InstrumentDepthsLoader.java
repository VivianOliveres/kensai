package com.kensai.market.persist;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Splitter;
import com.kensai.market.core.InstrumentDepth;
import com.kensai.protocol.Trading.Depth;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Summary;

public class InstrumentDepthsLoader {
	public static final Logger log = LogManager.getLogger(InstrumentDepthsLoader.class);

	public List<InstrumentDepth> load(Path path) throws IOException {
		// Check path validity
		if (path == null) {
			throw new IllegalArgumentException("Null path are forbidden");

		} else if (!Files.exists(path)) {
			throw new IllegalArgumentException("Non existing path are forbiden: [" + path.toString() + "]");

		} else if (!Files.isRegularFile(path)) {
			throw new IllegalArgumentException("Only regular file (like non directory) can be read: [" + path.toString() + "]");
		}

		List<InstrumentDepth> instruments = newArrayList();
		List<String> allLines = Files.readAllLines(path, Charset.defaultCharset());
		if (allLines == null || allLines.isEmpty()) {
			return instruments;
		}

		for (String line : allLines) {
			line = line.trim();

			// Escape empty line
			if (line.isEmpty()) {
				continue;
			}

			// Escape comment line
			if (line.startsWith("#")) {
				continue;
			}

			instruments.add(parseLine(line));
		}

		return instruments;
	}

	protected InstrumentDepth parseLine(String line) {
		// Split values and trim
		Iterable<String> splitted = Splitter.on(";").trimResults().split(line);

		// Name;Raw;Isin;Last;Close;High;Low;Open
		Iterator<String> iterator = splitted.iterator();
		String desc = iterator.next();
		String name = iterator.next();
		String isin = iterator.next();

		String tmp = iterator.next();
		double last = 0.0;
		try {
			last = Double.parseDouble(tmp);
		} catch (NumberFormatException e) {
			log.error("Can not parse last from [{}] in line: {}", tmp, line);
		}

		tmp = iterator.next();
		double close = 0.0;
		try {
			close = Double.parseDouble(tmp);
		} catch (NumberFormatException e) {
			log.error("Can not parse close from [{}] in line: {}", tmp, line);
		}

		tmp = iterator.next();
		double high = 0.0;
		try {
			high = Double.parseDouble(tmp);
		} catch (NumberFormatException e) {
			log.error("Can not parse high from [{}] in line: {}", tmp, line);
		}

		tmp = iterator.next();
		double low = 0.0;
		try {
			low = Double.parseDouble(tmp);
		} catch (NumberFormatException e) {
			log.error("Can not parse low from [{}] in line: {}", tmp, line);
		}

		tmp = iterator.next();
		double open = 0.0;
		try {
			open = Double.parseDouble(tmp);
		} catch (NumberFormatException e) {
			log.error("Can not parse open from [{}] in line: {}", tmp, line);
		}

		long now = System.currentTimeMillis();
		Instrument instr = Instrument.newBuilder().setDescription(desc).setName(name).setIsin(isin).build();
		Depth buyDepth = Depth.newBuilder().setPrice(low).setQuantity(8).setDepth(0).build();
		Depth sellDepth = Depth.newBuilder().setPrice(high).setQuantity(8).setDepth(0).build();
		Summary summary = Summary.newBuilder().setInstrument(instr).setOpen(open).setClose(close).setLast(last).addBuyDepths(buyDepth)
			.addSellDepths(sellDepth).setMarketStatus(MarketStatus.OPEN).setTimestamp(now).build();
		return new InstrumentDepth(summary);
	}
}
