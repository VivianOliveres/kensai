package fr.kensai.fmk.providing;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

public class TestAbstractDataProvider {

	private AbstractDataProvider provider;

	private DataListener listener;

	@Before
	public void init() {
		listener = mock(DataListener.class);
		provider = new MyDataProvider();
	}

	@Test
	public void should_update_selection_of_new_listeners() {
		// WHEN: add listener
		provider.addListener(listener);

		// THEN: listener has changed its selection
		verify(listener).selectionChanged(anyList());
	}

	@Test
	public void should_update_selection_of_listeners() {
		// GIVEN: listener is add
		provider.addListener(listener);
		verify(listener).selectionChanged(anyList());

		// WHEN: selection has changed
		String objectToProvide = "objectToProvide";
		provider.setProvidenDatas(newArrayList(objectToProvide));
		provider.notifyListeners();

		// THEN: listener has changed its selection
		verify(listener, times(2)).selectionChanged(anyList());
	}

}

class MyDataProvider extends AbstractDataProvider {

	@Override
	public String getName() {
		return "MyDataProvider";
	}

	@Override
	public Class getProvidenClass() {
		return Object.class;
	}

}