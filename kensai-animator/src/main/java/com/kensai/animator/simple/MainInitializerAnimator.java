package com.kensai.animator.simple;

import static com.kensai.protocol.Trading.Role.ADMIN;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.animator.core.AnimatorClient;
import com.kensai.animator.random.MainRandomAnimator;
import com.kensai.animator.sdk.Animator;
import com.kensai.protocol.Trading.User;

public class MainInitializerAnimator {
	private static final Logger log = LogManager.getLogger(MainRandomAnimator.class);

	private static final String DEFAULT_USER = "DefaultSimpleAnimator";

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 1664;

	public static void main(String[] args) {
		// User
		String userName;
		if (args == null || args.length == 0) {
			log.warn("No user -> use default [" + DEFAULT_USER + "]");
			userName = DEFAULT_USER;

		} else {
			// Retrieve user
			userName = args[0].trim();
		}

		User user = User.newBuilder().setName(userName).addGroups("Animator").addGroups("Init").setIsListeningSummary(true).setExecListeningRole(ADMIN)
			.setOrderListeningRole(ADMIN).build();

		// Retrieve host and port
		String host = DEFAULT_HOST;
		int port = DEFAULT_PORT;
		if (args.length >= 3) {
			host = args[1].trim();
			port = Integer.parseInt(args[2].trim());
		}

		// Create Random Animator
		Animator animator = new InitializerAnimator(user);

		// Launch client
		AnimatorClient client = new AnimatorClient(host, port, animator);
		client.run();
	}

}
