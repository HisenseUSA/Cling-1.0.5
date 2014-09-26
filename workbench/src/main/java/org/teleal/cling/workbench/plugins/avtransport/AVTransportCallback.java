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

package org.teleal.cling.workbench.plugins.avtransport;

import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.teleal.cling.support.avtransport.lastchange.AVTransportVariable;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;
import org.teleal.common.util.Exceptions;

import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class AVTransportCallback extends SubscriptionCallback {
    private static Logger log = Logger.getLogger(AVTransportCallback.class.getName());

    final protected AVTransportController controller;

    public AVTransportCallback(Service service, AVTransportController controller) {
        super(service);
        this.controller = controller;
    }

    @Override
    protected void failed(GENASubscription subscription,
                          UpnpResponse responseStatus,
                          Exception exception,
                          String defaultMsg) {
        log.severe(defaultMsg);
    }

    protected void established(GENASubscription subscription) {
        Workbench.APP.log(new LogMessage(
                Level.INFO,
                "AVTransport ControlPointAdapter",
                "Subscription with service established, listening for events."
        ));
    }

    protected void ended(GENASubscription subscription, final CancelReason reason, UpnpResponse responseStatus) {
        Workbench.APP.log(new LogMessage(
                reason != null ? Level.WARNING : Level.INFO,
                "AVTransport ControlPointAdapter",
                "Subscription with service ended. " + (reason != null ? "Reason: " + reason : "")
        ));
        controller.disconnect(reason);
    }

    protected void eventReceived(GENASubscription subscription) {
        log.finer("Event received, sequence number: " + subscription.getCurrentSequence());

        final LastChange lastChange;
        try {
            lastChange = new LastChange(
                    new AVTransportLastChangeParser(),
                    subscription.getCurrentValues().get("LastChange").toString()
            );
        } catch (Exception ex) {
            log.warning("Error parsing LastChange event content: " + ex);
            log.warning("Cause: " + Exceptions.unwrap(ex));
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (UnsignedIntegerFourBytes instanceId : lastChange.getInstanceIDs()) {

                    log.finer("Processing LastChange event values for instance: " + instanceId);

                    AVTransportVariable.TransportState transportState =
                            lastChange.getEventedValue(
                                    instanceId,
                                    AVTransportVariable.TransportState.class
                            );

                    if (transportState != null) {
                        log.finer("AVTransport service state changed to: " + transportState.getValue());
                        controller.getInstanceController(instanceId).forceState(transportState.getValue());
                    }

                    AVTransportVariable.CurrentTrackURI currentTrackURI =
                            lastChange.getEventedValue(instanceId, AVTransportVariable.CurrentTrackURI.class);
                    if (currentTrackURI != null) {
                        log.fine("AVTransport service CurrentTrackURI changed to: " + currentTrackURI.getValue());
                        controller.getInstanceController(instanceId).getUriPanel().getUriTextField().setText(
                                currentTrackURI.getValue().toString()
                        );
                    }
                }
            }
        });
    }

    protected void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        log.warning("Events missed (" + numberOfMissedEvents + "), consider restarting this control point!");
    }

}
