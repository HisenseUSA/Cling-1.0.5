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

import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class MonitorSubscriptionCallback extends SubscriptionCallback {

    protected final MonitorController controller;

    MonitorSubscriptionCallback(Service service, MonitorController controller) {
        super(service);
        this.controller = controller;
    }

    public void eventReceived(final GENASubscription subscription) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                List<StateVariableValue> values = new ArrayList();
                for (Map.Entry<String, StateVariableValue> entry :
                        ((Map<String, StateVariableValue>) subscription.getCurrentValues()).entrySet()) {
                    values.add(entry.getValue());
                }
                controller.getStateVariablesTable().getValuesModel().setValues(values);
                controller.getView().validate();
            }
        });

        Workbench.APP.log(new LogMessage("Monitor Controller", "Event received: " + new Date()));
    }

    public void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.getView().validate();
            }
        });
        Workbench.APP.log(new LogMessage("Monitor Controller", "Events missed: " + numberOfMissedEvents));
    }

    @Override
    protected void failed(final GENASubscription subscription,
                          final UpnpResponse responseStatus,
                          final Exception exception,
                          final String defaultMsg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                String failureMessage;
                if (responseStatus == null && exception == null) {
                    failureMessage = "Subscription failed: No response and no exception received";
                } else {
                    failureMessage = responseStatus != null
                            ? "Subscription failed: " + responseStatus.getResponseDetails()
                            : "Subscription failed: " + exception.toString();
                }

                Workbench.APP.log(new LogMessage(Level.SEVERE, "Monitor Controller", failureMessage));
                controller.getStartButton().setEnabled(true);
                controller.getStopButton().setEnabled(false);
                controller.getView().validate();
            }
        });
    }

    @Override
    public void established(GENASubscription subscription) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.getStartButton().setEnabled(false);
                controller.getStopButton().setEnabled(true);
                controller.getView().validate();
            }
        });
        Workbench.APP.log(new LogMessage(
                "Monitor Controller", "Subscription established for seconds: " + subscription.getActualDurationSeconds()
        ));
    }

    @Override
    public void ended(GENASubscription subscription, final CancelReason reason, UpnpResponse responseStatus) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                controller.getStartButton().setEnabled(true);
                controller.getStopButton().setEnabled(false);
                controller.getView().validate();
            }
        });
        Workbench.APP.log(new LogMessage(
                "Monitor Controller", "Subscription ended" + (reason != null ? ": " + reason : "")
        ));
    }
}