package com.kensai.animator.sdk;

import java.util.UUID;

public final class UserDataGenerator {

	private UserDataGenerator() {
		// Not instanciable
	}

	public static String generate() {
		return UUID.randomUUID().toString().substring(0, 8);
	}

}
