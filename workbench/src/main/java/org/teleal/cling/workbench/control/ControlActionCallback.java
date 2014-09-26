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

package org.teleal.cling.workbench.control;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.SwingUtilities;
import java.util.logging.Level;


public class ControlActionCallback extends ActionCallback {

    protected final ActionController controller;

    public ControlActionCallback(ActionInvocation actionInvocation, ActionController controller) {
        super(actionInvocation);
        this.controller = controller;
    }

    @Override
    public void success(final ActionInvocation invocation) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.getOutputArgumentsTable().getArgumentValuesModel().setValues(
                        invocation.getOutput()
                );
                controller.getView().validate();
            }
        });
        Workbench.APP.log(new LogMessage(
                "Action Invocation",
                "Completed invocation: " + invocation.getAction().getName()
        ));
    }

    @Override
    public void failure(ActionInvocation invocation,
                        UpnpResponse operation,
                        String defaultMsg) {
        Workbench.APP.log(new LogMessage(
                Level.SEVERE,
                "Action Invocation",
                defaultMsg
        ));
    }

}
