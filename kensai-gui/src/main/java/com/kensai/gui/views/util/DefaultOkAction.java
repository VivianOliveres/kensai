package com.kensai.gui.views.util;

import javafx.event.ActionEvent;

import org.controlsfx.control.action.AbstractAction;
import org.controlsfx.dialog.Dialog;

public class DefaultOkAction extends AbstractAction {

	public DefaultOkAction() {
		super("OK");
	}

	@Override
	public void execute(ActionEvent event) {
		Dialog dlg = (Dialog) event.getSource();
		dlg.hide();
	}

}
