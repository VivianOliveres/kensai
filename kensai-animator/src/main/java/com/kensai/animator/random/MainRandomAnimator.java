package com.kensai.animator.random;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.AnimatorClient;
import com.kensai.animator.sdk.Animator;

public class MainRandomAnimator {
	private static final Logger log = LogManager.getLogger(MainRandomAnimator.class);

	private static final String DEFAULT_USER = "DefaultRandomAnimator";

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 1664;

	private static final int DEFAULT_MIN_QTY = 7;
	private static final int DEFAULT_MAX_QTY = 17;

	private static final int DEFAULT_DELAY = new Random().nextInt(1500);
	private static final int DEFAULT_PERIOD = 750;

	public static void main(String[] args) {
		// User
		String user;
		if (args == null || args.length == 0) {
			log.warn("No user -> use default [" + DEFAULT_USER + "]");
			user = DEFAULT_USER;

		} else {
			// Retrieve user
			user = args[0].trim();
		}

		// Retrieve host and port
		String host = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		if (args.length >= 3) {
			host = args[1].trim();
			port = Integer.parseInt(args[2].trim());
		}

		// Retrieve min & max quantity
		int minQty = DEFAULT_MIN_QTY;
		int maxQty = DEFAULT_MAX_QTY;
		if (args.length >= 5) {
			minQty = Integer.parseInt(args[3].trim());
			maxQty = Integer.parseInt(args[4].trim());
		}

		// Retrieve delay and period
		int delay = DEFAULT_DELAY;
		int period = DEFAULT_PERIOD;
		if (args.length >= 7) {
			delay = Integer.parseInt(args[5].trim());
			period = Integer.parseInt(args[6].trim());
		}

		// Create Random Animator
		Animator animator = new RandomAnimator(user, minQty, maxQty, delay, period);

		// Launch client
		AnimatorClient client = new AnimatorClient(host, port, animator);
		client.run();
	}

}
