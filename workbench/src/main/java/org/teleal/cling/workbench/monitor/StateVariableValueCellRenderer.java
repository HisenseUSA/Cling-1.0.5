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

package org.teleal.cling.workbench.monitor;

import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.workbench.datatable.BooleanArgumentValueCellComponent;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import java.awt.Component;


public class StateVariableValueCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int columns) {

        StateVariableValuesTableModel model = (StateVariableValuesTableModel)table.getModel();


        StateVariableValue stateVariableValue = model.getValueAt(row);
        StateVariable stateVariable = stateVariableValue.getStateVariable();

        switch (stateVariable.getTypeDetails().getDatatype().getBuiltin()) {
            case BOOLEAN:

                return new BooleanArgumentValueCellComponent(stateVariableValue);

            default:
                String stringValue;
                if (stateVariableValue != null && stateVariableValue.toString().length() > 0) {
                    stringValue = stateVariableValue.toString();
                } else {
                    stringValue = "<<NULL>>";
                }

                return super.getTableCellRendererComponent(
                        table, stringValue, isSelected, hasFocus, row, columns
                );
        }
    }

}
