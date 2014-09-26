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

package org.teleal.cling.workbench.plugins.renderingcontrol;

import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.gena.GENASubscription;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.model.Channel;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlVariable;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RenderingControlCallback extends SubscriptionCallback {

    private static Logger log = Logger.getLogger(RenderingControlCallback.class.getName());

    protected final RenderingController controller;

    public RenderingControlCallback(Service service, RenderingController controller) {
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

    public void established(GENASubscription subscription) {
        Workbench.APP.log(new LogMessage(
                Level.INFO,
                "Rendering ControlPointAdapter",
                "Subscription with service established, listening for events."
        ));
    }

    public void ended(GENASubscription subscription, final CancelReason reason, UpnpResponse responseStatus) {
        Workbench.APP.log(new LogMessage(
                reason != null ? Level.WARNING : Level.INFO,
                "Rendering ControlPointAdapter",
                "Subscription with service ended. " + (reason != null ? "Reason: " + reason : "")
        ));
        controller.disconnect(reason);
    }

    public void eventReceived(GENASubscription subscription) {
        log.finer("Event received, sequence number: " + subscription.getCurrentSequence());

        final LastChange lastChange;
        try {
            lastChange = new LastChange(
                    new RenderingControlLastChangeParser(),
                    subscription.getCurrentValues().get("LastChange").toString()
            );
        } catch (Exception ex) {
            log.warning("Error parsing LastChange event content: " + ex);
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for (UnsignedIntegerFourBytes instanceId : lastChange.getInstanceIDs()) {

                    log.finer("Processing LastChange event values for instance: " + instanceId);
                    RenderingControlVariable.Volume volume = lastChange.getEventedValue(
                            instanceId,
                            RenderingControlVariable.Volume.class
                    );
                    if (volume != null && volume.getValue().getChannel().equals(Channel.Master)) {
                        log.finer("Received new volume value for 'Master' channel: " + volume.getValue());
                        controller.getInstanceController(instanceId)
                                .setVolumeSliderWithoutNotification(volume.getValue().getVolume());
                    }
                }
            }
        });
    }

    public void eventsMissed(GENASubscription subscription, int numberOfMissedEvents) {
        log.warning("Events missed (" + numberOfMissedEvents + "), consider restarting this control point!");
    }

}
