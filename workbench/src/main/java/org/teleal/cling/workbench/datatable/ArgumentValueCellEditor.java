/*
 * Copyright (C) 2010 Teleal GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.teleal.cling.workbench.datatable;

import org.teleal.cling.model.action.ActionArgumentValue;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.common.swingfwk.Controller;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;


public abstract class ArgumentValueCellEditor extends AbstractCellEditor implements TableCellEditor {

    final private Controller controller;
    final private ActionArgument argument;
    private ActionArgumentValue argumentValue;

    protected ArgumentValueCellEditor(Controller controller, ActionArgument argument, ActionArgumentValue argumentValue) {
        this.controller = controller;
        this.argument = argument;
        this.argumentValue = argumentValue;
    }

    public Controller getController() {
        return controller;
    }

    public ActionArgument getArgument() {
        return argument;
    }

    public ActionArgumentValue getArgumentValue() {
        return argumentValue;
    }

    public void setArgumentValue(ActionArgumentValue argumentValue) {
        this.argumentValue = argumentValue;
    }

    public Object getCellEditorValue() {
        return getArgumentValue();
    }

    public void setStatus(String msg) {
        Workbench.APP.log(new LogMessage("Argument Editor", msg));
    }

    public abstract boolean handlesEditability();

}
