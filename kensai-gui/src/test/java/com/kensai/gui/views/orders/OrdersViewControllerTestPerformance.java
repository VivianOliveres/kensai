package com.kensai.gui.views.orders;

import static com.kensai.protocol.Trading.CommandStatus.ACK;
import static com.kensai.protocol.Trading.OrderAction.INSERT;
import static com.kensai.protocol.Trading.OrderStatus.ON_MARKET;
import static com.kensai.protocol.Trading.Role.ADMIN;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.orders.OrdersModel;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.User;

/**
 * Simple performance test
 *
 */
public class OrdersViewControllerTestPerformance extends Application {
	private static Logger log = LogManager.getLogger(OrdersViewControllerTestPerformance.class);

	private static final String USER_NAME = "user";
	private static final String USER_GROUP = "default";
	private static final User USER = User.newBuilder().setName(USER_NAME).addGroups(USER_GROUP).setOrderListeningRole(ADMIN)
		.setExecListeningRole(ADMIN).setIsListeningSummary(true).build();

	private static final Instrument INSTRUMENT = Instrument.newBuilder().setName("name").setIsin("isin").build();
	private static final InstrumentModel INSTRUMENT_MODEL = new InstrumentModel(INSTRUMENT, "MARKET_CONNECTION_NAME");

	public static void main(String[] args) {
		launch(args);
	}

	private ConcurrentLinkedQueue<Order> ordersUpdates = new ConcurrentLinkedQueue<>();
	private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	@Override
	public void start(Stage stage) throws Exception {
		log.info("Start application");
		OrdersModel model = new OrdersModel();

		log.info("Start creating order");
		int max = 100_000;
		startOrderFeeder(createOrderFeeder(max, BuySell.BUY));
		startOrderFeeder(createOrderFeeder(max, BuySell.SELL));

		// Init stage
		log.info("Init stage");
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(10));

		OrdersViewController ordersViewController = new OrdersViewController(model);
		root.setBottom(ordersViewController.getView());

		Scene scene = new Scene(root);
		// scene.getStylesheets().add("style-dark.css");
		stage.setScene(scene);
		stage.setTitle("Kensai GUI");
		stage.show();
		log.info("Application started");

		log.info("Start updating model order");
		scheduledExecutor.scheduleAtFixedRate(() -> Platform.runLater(() -> doUpdateGui(model)), 1000, 1000, MILLISECONDS);
	}

	private void doUpdateGui(OrdersModel model) {
		if (ordersUpdates.size() > 0) {
			log.info("doUpdateGui for " + ordersUpdates.size() + " orders");
		} else {
			return;
		}

		int i = 0;
		while (!ordersUpdates.isEmpty()) {
			Order order = ordersUpdates.remove();
			model.add(order, INSTRUMENT_MODEL);

			if (i % 1_000 == 0) {
				log.info("Add {} orders into model", i);
			}
			i++;
		}

		log.info("doUpdateGui done");
	}

	private void startOrderFeeder(Runnable orderFeeder) {
		Thread t = new Thread(orderFeeder);
		t.start();
	}

	private Runnable createOrderFeeder(int max, BuySell side) {
		Runnable runner = new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < max; i++) {
					int id = side.equals(BuySell.BUY) ? i * 2 : i * 2 + 1;
					Order order = createOrder(id, side);
					ordersUpdates.add(order);

					if (i % 1_000 == 0) {
						log.info("Orders [{}]: {}/{}", side, i, max);
					}
				}
			}
		};

		return runner;
	}

	private Order createOrder(int i, BuySell side) {
		long now = System.currentTimeMillis();
		return Order.newBuilder()
						.setAction(INSERT)
						.setCommandStatus(ACK)
						.setExecPrice(0)
						.setExecutedQuantity(0)
						.setId(i).
						setInitialQuantity(i)
						.setInsertTime(now)
						.setInstrument(INSTRUMENT)
						.setLastUpdateTime(now)
						.setOrderStatus(ON_MARKET)
						.setPrice(i)
						.setSide(side)
						.setUser(USER)
						.setUserData(Integer.toString(i))
						.build();
	}

}
