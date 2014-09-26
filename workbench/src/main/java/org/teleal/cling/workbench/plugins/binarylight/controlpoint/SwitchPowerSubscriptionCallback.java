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

package org.teleal.cling.workbench.plugins.binarylight.controlpoint;

import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.state.StateVariableValue;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.SwingUtilities;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SwitchPowerSubscriptionCallback  extends SubscriptionCallback {

    private static Logger log = Logger.getLogger(SwitchPowerSubscriptionCallback.class.getName());

    protected final SwitchPowerController controller;

    public SwitchPowerSubscriptionCallback(Service service, SwitchPowerController controller) {
        super(service);
        this.controller = controller;
    }

    @Override
    protected void failed(GENASubscription subscription,
                          UpnpResponse responseStatus,
                          Exception exception,
                          String defaultMsg) {
        controller.onConnectFailure(defaultMsg);
    }

    public void established(GENASubscription subscription) {
        Workbench.APP.log(new LogMessage(
                Level.INFO,
                "SwitchPower ControlPointAdapter",
                "Subscription with service established, listening for events, renewing in seconds: " + subscription.getActualDurationSeconds()
        ));
        controller.onConnect();
    }

    public void ended(GENASubscription subscription, CancelReason reason, UpnpResponse responseStatus) {
        Workbench.APP.log(new LogMessage(
                reason != null ? Level.WARNING : Level.INFO,
                "SwitchPower ControlPointAdapter",
                "Subscription with service ended. " + (reason != null ? "Reason: " + reason : "")
        ));
        controller.onDisconnect();
    }

    public void eventReceived(GENASubscription subscription) {
        log.finer("Event received, sequence number: " + subscription.getCurrentSequence());

        Map<String, StateVariableValue> map = subscription.getCurrentValues();
        final StateVariableValue stateValue = map.get("Status");
        if (stateValue != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    log.finer("Received event 'Status' value, switching to: " + (Boolean)stateValue.getValue());
                    controller.getToggleButton().setSelected((Boolean)stateValue.getValue());
                }
            });
        }
    }

    public void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        log.warning("Events missed (" + numberOfMissedEvents + "), consider restarting this control point!");
    }

}

