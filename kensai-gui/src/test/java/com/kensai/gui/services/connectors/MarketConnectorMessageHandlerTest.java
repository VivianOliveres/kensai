package com.kensai.gui.services.connectors;

import static com.kensai.gui.assertions.Assertions.assertThat;
import static com.kensai.protocol.Trading.OrderAction.INSERT;
import static com.kensai.protocol.Trading.OrderStatus.ON_MARKET;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.kensai.gui.services.ApplicationContext;
import com.kensai.gui.services.model.ModelService;
import com.kensai.gui.services.model.instruments.InstrumentModel;
import com.kensai.gui.services.model.instruments.InstrumentsModel;
import com.kensai.gui.services.model.instruments.SummaryModel;
import com.kensai.gui.services.model.market.MarketConnectionModel;
import com.kensai.gui.services.model.orders.OrdersModel;
import com.kensai.gui.services.task.TaskService;
import com.kensai.protocol.Trading.BuySell;
import com.kensai.protocol.Trading.Instrument;
import com.kensai.protocol.Trading.InstrumentType;
import com.kensai.protocol.Trading.InstrumentsSnapshot;
import com.kensai.protocol.Trading.MarketStatus;
import com.kensai.protocol.Trading.Order;
import com.kensai.protocol.Trading.OrdersSnapshot;
import com.kensai.protocol.Trading.SummariesSnapshot;
import com.kensai.protocol.Trading.Summary;

public class MarketConnectorMessageHandlerTest {
	
	private MarketConnectorMessageHandler handler;
	
	private MarketConnectionModel swx = new MarketConnectionModel("SWX", "localhost", 1664, false);

	private TaskService taskService = mock(TaskService.class);
	private ScheduledExecutorService scheduledExecutor = mock(ScheduledExecutorService.class);

	private ApplicationContext context = mock(ApplicationContext.class);
	private InstrumentsModel instrumentsModel = mock(InstrumentsModel.class);
	private SummaryModel summaryModel = mock(SummaryModel.class);
	private OrdersModel ordersModel = mock(OrdersModel.class);
	
	private static final Instrument SANOFI = Instrument.newBuilder().setIsin("FR0000120578").setName("Sanofi").setDescription("SAN").setType(InstrumentType.STOCK).build();
	private static final Summary SANOFI_SNAPSHOT_1 = Summary.newBuilder().setInstrument(SANOFI).setClose(68.08).setLast(68.16).setMarketStatus(MarketStatus.OPEN).setOpen(68.08).setTimestamp(System.currentTimeMillis()).build();
	private static final Summary SANOFI_SNAPSHOT_2 = Summary.newBuilder().setInstrument(SANOFI).setClose(69.09).setLast(69.17).setMarketStatus(MarketStatus.OPEN).setOpen(69.09).setTimestamp(System.currentTimeMillis() + 1).build();

	private static final Order SANOFI_ORDER = Order.newBuilder().setId(123).setSide(BuySell.BUY).setInstrument(SANOFI).setAction(INSERT).setOrderStatus(ON_MARKET).setPrice(456.789).setExecPrice(456.789).setInitialQuantity(159).setExecutedQuantity(15).setUserData("blabla").setUser("toto").setInsertTime(System.currentTimeMillis()).setLastUpdateTime(System.currentTimeMillis()).build();

	@Before
	public void init() {
		given(context.getTaskService()).willReturn(taskService);

		given(taskService.getScheduledExecutor()).willReturn(scheduledExecutor);

		given(instrumentsModel.getSummary(eq(SANOFI), eq(swx.getConnectionName()))).willReturn(summaryModel);

		ModelService modelService = mock(ModelService.class);
		given(modelService.getInstruments()).willReturn(instrumentsModel);
		given(modelService.getOrders()).willReturn(ordersModel);
		given(context.getModelService()).willReturn(modelService);

		handler = new MarketConnectorMessageHandler(context, swx);
	}

	@Test
	public void should_refresh_gui_every_750_ms() {
		verify(scheduledExecutor).scheduleAtFixedRate(any(Runnable.class), eq(750L), eq(750L), eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void should_update_InstrumentsModel_when_receive_onSnapshot_instruments() {
		// GIVEN: InstrumentsSnapshot with 2 instruments
		Instrument instrument = Instrument.newBuilder().setIsin("isin")
																		.setName("name")
																		.setDescription("description")
																		.setType(InstrumentType.STOCK)
																		.build();
		InstrumentsSnapshot snapshot = InstrumentsSnapshot.newBuilder().addInstruments(instrument).addInstruments(SANOFI).build();

		// WHEN: connector receive snapshot
		handler.doOnSnapshot(snapshot);

		// THEN: update InstrumentsModel
		verify(instrumentsModel).add(instrument, swx.getConnectionName());
		verify(instrumentsModel).add(SANOFI, swx.getConnectionName());
	}

	@Test
	public void should_update_SummaryModel_when_receive_onSnapshot_summary() {
		// GIVEN: SummariesSnapshot
		SummariesSnapshot summaries = SummariesSnapshot.newBuilder().addSummaries(SANOFI_SNAPSHOT_1).build();

		// AND: model
		InstrumentModel instrumentModel = new InstrumentModel(SANOFI, swx.getConnectionName());
		given(instrumentsModel.getSummary(eq(SANOFI), eq(swx.getConnectionName()))).willReturn(instrumentModel.getSummary());

		// WHEN: connector receive snapshot
		handler.doOnSnapshot(summaries);

		// AND: doUpdateGUI
		handler.doUpdateGui();

		// THEN: update InstrumentsModel
		assertThat(instrumentModel.getSummary()).isEqualTo(SANOFI_SNAPSHOT_1);
	}

	@Test
	public void should_update_summary_when_receiving_summary() {
		// GIVEN: New summary has been received
		handler.onSummary(SANOFI_SNAPSHOT_1);

		// WHEN: doUpdateGui
		handler.doUpdateGui();

		// THEN: update summaryModel
		verify(summaryModel).update(SANOFI_SNAPSHOT_1);
	}

	@Test
	public void should_update_summary_with_last_summary_received() {
		// GIVEN: 2 summaries has been received
		handler.onSummary(SANOFI_SNAPSHOT_1);
		handler.onSummary(SANOFI_SNAPSHOT_2);

		// WHEN: doUpdateGui
		handler.doUpdateGui();

		// THEN: update summaryModel
		verify(summaryModel, never()).update(SANOFI_SNAPSHOT_1);
		verify(summaryModel).update(SANOFI_SNAPSHOT_2);
	}

	@Test
	public void should_update_OrdersModel_when_receive_onSnapshot_Order() {
		// GIVEN: OrdersSnapshot
		OrdersSnapshot orders = OrdersSnapshot.newBuilder().addOrders(SANOFI_ORDER).build();

		// WHEN: connector receive snapshot
		handler.onSnapshot(orders);

		// AND: doUpdateGUI
		handler.doUpdateGui();

		// THEN: update OrdersModel
		verify(ordersModel).add(eq(SANOFI_ORDER), any(InstrumentModel.class));
	}

	@Test
	public void should_add_orders_when_receiving_order() {
		// GIVEN: New order has been received
		handler.onOrder(SANOFI_ORDER);

		// WHEN: doUpdateGui
		handler.doUpdateGui();

		// THEN: update OrdersModel
		verify(ordersModel).add(eq(SANOFI_ORDER), any(InstrumentModel.class));
	}
}
