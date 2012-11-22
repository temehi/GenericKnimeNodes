/**
 * Copyright (c) 2012, Björn Kahlert.
 *
 * This file is part of GenericKnimeNodes.
 * 
 * GenericKnimeNodes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.genericworkflownodes.knime.generic_node.dialogs.param_dialog;

import javax.swing.JLabel;

import com.genericworkflownodes.knime.generic_node.dialogs.UIHelper;
import com.genericworkflownodes.knime.generic_node.dialogs.param_dialog.itemlist.ListParameterModel;
import com.genericworkflownodes.knime.generic_node.dialogs.param_dialog.list_editor.ListEditorDialog;
import com.genericworkflownodes.knime.parameter.ListParameter;
import com.genericworkflownodes.knime.parameter.Parameter;
import com.genericworkflownodes.util.StringUtils;

/**
 * This component allows to edit lists of values in a separate window.
 * 
 * @author Björn Kahlert
 */
public class JListEditorComponent extends JLabel {

	private static final long serialVersionUID = 1L;
	private final ListParameter parameter;

	public JListEditorComponent(ListParameterModel listParameterModel,
			ListParameter p) {
		super("Editing...");
		parameter = p;
		/*
		 * ItemListFillerDialog itemListFillerDialog = new ItemListFillerDialog(
		 * this.listParameterModel);
		 * UIHelper.resizeAndCenter(itemListFillerDialog, 0.5);
		 * itemListFillerDialog.setVisible(true);
		 */
		ListEditorDialog led = new ListEditorDialog(p);
		led.setVisible(true);

		UIHelper.simulateEnterKeyPressed(this, 50);
	}

	public String getParameterValue() {
		return StringUtils.join(parameter.getStrings(),
				Parameter.SEPARATOR_TOKEN);
	}
}