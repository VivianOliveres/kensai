package com.kensai.market;

import java.util.Set;

import com.google.common.collect.Sets;

public final class IdGenerator {

	private static Set<Long> ids = Sets.newHashSet();

	private IdGenerator() {
		// Not instanciable
	}

	/**
	 * @return an unique identifier based on timestamp
	 */
	public static synchronized long generateId() {
		long timestamp = System.currentTimeMillis();
		while (ids.contains(timestamp)) {
			timestamp++;
		}

		// add this timestamp and return it
		ids.add(timestamp);
		return timestamp;
	}

}
