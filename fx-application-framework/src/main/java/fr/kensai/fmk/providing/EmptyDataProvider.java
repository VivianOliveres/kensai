package fr.kensai.fmk.providing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmptyDataProvider extends AbstractDataProvider {

	private static final Logger log = LogManager.getLogger(EmptyDataProvider.class);

	@Override
	public Class getProvidenClass() {
		return Object.class;
	}

	@Override
	public String getName() {
		return "< none >";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;

		} else {
			return obj instanceof EmptyDataProvider;
		}
	}
}
