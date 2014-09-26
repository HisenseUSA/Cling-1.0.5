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

package org.teleal.cling.workbench.plugins.avtransport.state;

import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.workbench.Workbench;
import org.teleal.cling.workbench.plugins.avtransport.ui.InstanceController;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BorderFactory;
import java.util.logging.Logger;

/**
 *
 */
public class Playing extends AVTransportClientState {

    public static final Logger log = Logger.getLogger(Playing.class.getName());

    final protected PositionUpdater positionUpdater;

    public Playing(InstanceController instanceController) {
        super(instanceController);
        positionUpdater = new PositionUpdater(getInstanceController());
    }

    public void onEntry() {

        Workbench.APP.log(new LogMessage(
                "AVTransport ControlPointAdapter",
                "Entering Playing state, starting to poll PositionInfo in " +
                        "background every " + positionUpdater.getSleepIntervalMillis() + "ms..."
        ));
        synchronized (positionUpdater) {
            positionUpdater.breakLoop();
            positionUpdater.notifyAll();
            new Thread(positionUpdater).start();
        }

        new UserInterfaceUpdate() {
            protected void run(InstanceController controller) {
                controller.getPlayerPanel().setBorder(BorderFactory.createTitledBorder("PLAYING"));
                controller.getPlayerPanel().setAllButtons(true);
                controller.getPlayerPanel().togglePause();
                controller.getProgressPanel().getPositionSlider().setEnabled(true);
            }
        };
    }

    public void onExit() {

        Workbench.APP.log(new LogMessage(
                "AVTransport ControlPointAdapter",
                "Exiting Playing state, stopping background PositionInfo polling..."
        ));
        synchronized (positionUpdater) {
            positionUpdater.breakLoop();
            positionUpdater.notifyAll();
        }

        new UserInterfaceUpdate() {
            protected void run(InstanceController controller) {
                controller.getPlayerPanel().togglePause();
                controller.getProgressPanel().getPositionSlider().setEnabled(false);
            }
        };
    }

    protected class PositionUpdater implements Runnable {

        private final Service service;
        private volatile boolean stopped = false;

        protected PositionUpdater(InstanceController instanceController) {
            this.service = instanceController.getCallback().getService();
        }

        public int getSleepIntervalMillis() {
            return 2000;
        }

        public void breakLoop() {
            log.fine("Setting stopped status on thread");
            stopped = true;
        }

        public void run() {
            stopped = false;

            log.fine("Running position updater loop every milliseconds: " + getSleepIntervalMillis());
            while (!stopped) {

                try {

                    // TODO: Well, we could do this once and then just increment the seconds instead of querying...

                    getInstanceController().getControlPoint().execute(
                            new GetPositionInfo(getInstanceController().getInstanceId(), service) {

                                @Override
                                public void received(ActionInvocation actionInvocation, final PositionInfo positionInfo) {
                                    log.finer("Updating position and progress: " + positionInfo);
                                    new UserInterfaceUpdate() {
                                        protected void run(InstanceController controller) {
                                            controller.getProgressPanel().setProgress(positionInfo);
                                        }
                                    };
                                }

                                @Override
                                public void failure(ActionInvocation invocation,
                                                    UpnpResponse operation,
                                                    String defaultMsg) {
                                    // We can ignore this, it's just the progress display
                                    log.fine("Failed updating position info: " + defaultMsg);
                                }
                            }
                    );

                    synchronized (this) {
                        this.wait(getSleepIntervalMillis());
                    }

                } catch (Exception ex) {
                    breakLoop();
                    log.fine("Failed updating position info, polling stopped: " + ex);
                }

            }
            log.fine("Stopped status on thread received, ending position updater loop");
        }


    }


}
