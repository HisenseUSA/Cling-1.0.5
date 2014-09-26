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

package org.teleal.cling.workbench.plugins.avtransport.ui;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.GetMediaInfo;
import org.teleal.cling.support.avtransport.callback.GetPositionInfo;
import org.teleal.cling.support.avtransport.callback.GetTransportInfo;
import org.teleal.cling.support.avtransport.callback.Pause;
import org.teleal.cling.support.avtransport.callback.Play;
import org.teleal.cling.support.avtransport.callback.Seek;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.avtransport.callback.Stop;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.model.MediaInfo;
import org.teleal.cling.support.model.PositionInfo;
import org.teleal.cling.support.model.TransportInfo;
import org.teleal.cling.workbench.Workbench;
import org.teleal.cling.workbench.plugins.avtransport.AVTransportCallback;
import org.teleal.cling.workbench.plugins.avtransport.state.AVTransportClientState;
import org.teleal.cling.workbench.plugins.avtransport.state.AVTransportClientStateMachine;
import org.teleal.cling.workbench.plugins.avtransport.state.NoMediaPresent;
import org.teleal.common.statemachine.StateMachineBuilder;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class InstanceController extends AbstractController<JPanel> {

    private static Logger log = Logger.getLogger(InstanceController.class.getName());

    // Dependencies
    final protected UnsignedIntegerFourBytes instanceId;
    final protected ControlPoint controlPoint;
    final protected AVTransportCallback callback;

    // Model
    final protected AVTransportClientStateMachine clientStateMachine;

    // View
    final private PlayerPanel playerPanel = new PlayerPanel();
    final private ProgressPanel progressPanel = new ProgressPanel();
    final private URIPanel uriPanel = new URIPanel();

    public InstanceController(Controller parentController, ControlPoint controlPoint, AVTransportCallback callback, UnsignedIntegerFourBytes instanceId) {
        super(new JPanel(), parentController);

        this.instanceId = instanceId;
        this.controlPoint = controlPoint;
        this.callback = callback;

        this.clientStateMachine = StateMachineBuilder.build(
                AVTransportClientStateMachine.class,
                NoMediaPresent.class,
                new Class[]{InstanceController.class},
                new Object[]{this}
        );

        getView().setLayout(new BoxLayout(getView(), BoxLayout.Y_AXIS));
        getView().add(playerPanel);
        getView().add(progressPanel);
        getView().add(uriPanel);

        registerPlayerActions();
    }

    // All the other methods are called by the GENA subscriptions with synchcronization, this
    // is called by other people as well, so we synchronize

    synchronized public void forceState(final TransportState transportState) {
        Class<? extends AVTransportClientState> newClientState = AVTransportClientState.STATE_MAP.get(transportState);
        if (newClientState != null) {
            try {
                clientStateMachine.forceState(newClientState);
            } catch (Exception ex) {
                log.severe("Error switching client instance state: " + ex);
            }
        }
    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return instanceId;
    }

    public ControlPoint getControlPoint() {
        return controlPoint;
    }

    public AVTransportCallback getCallback() {
        return callback;
    }

    public PlayerPanel getPlayerPanel() {
        return playerPanel;
    }

    public ProgressPanel getProgressPanel() {
        return progressPanel;
    }

    public URIPanel getUriPanel() {
        return uriPanel;
    }

    @Override
    public void dispose() {
        // End everything we do (background polling)
        forceState(TransportState.STOPPED);
    }

    public void updateTransportInfo() {
        log.info("Calling GetTransportInfo...");
        controlPoint.execute(
                new GetTransportInfo(getInstanceId(), getCallback().getService()) {
                    @Override
                    public void received(ActionInvocation actionInvocation, final TransportInfo transportInfo) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                log.info("Setting initial TransportState: " + transportInfo.getCurrentTransportState());
                                forceState(transportInfo.getCurrentTransportState());
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        log.severe("Can't retrieve initial TransportInfo: " + defaultMsg);
                    }
                }
        );
    }

    public void updateMediaInfo() {
        log.info("Calling GetMediaInfo...");
        controlPoint.execute(
                new GetMediaInfo(getInstanceId(), getCallback().getService()) {
                    @Override
                    public void received(ActionInvocation actionInvocation, final MediaInfo mediaInfo) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                log.info("Setting initial CurrentURI: " + mediaInfo.getCurrentURI());
                                getUriPanel().getUriTextField().setText(mediaInfo.getCurrentURI());
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        log.severe("Can't retrieve initial MediaInfo: " + defaultMsg);
                    }
                }
        );
    }

    public void updatePositionInfo() {
        log.info("Calling GetPositionInfo...");
        controlPoint.execute(
                new GetPositionInfo(getInstanceId(), getCallback().getService()) {
                    @Override
                    public void received(ActionInvocation actionInvocation, final PositionInfo positionInfo) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                log.info("Setting initial PositionInfo: " + positionInfo);
                                getProgressPanel().setProgress(positionInfo);
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        log.severe("Can't retrieve initial PositionInfo: " + defaultMsg);
                    }
                }
        );
    }

    protected void registerPlayerActions() {

        registerAction(getPlayerPanel().getFwdButton(), PlayerPanel.ACTION_SKIP_FW[1], new SeekAction(15, true));

        registerAction(getPlayerPanel().getRewButton(), PlayerPanel.ACTION_SKIP_REW[1], new SeekAction(15, false));

        registerAction(getPlayerPanel().getPauseButton(), PlayerPanel.ACTION_PAUSE[1], new PauseAction());

        registerAction(getPlayerPanel().getStopButton(), PlayerPanel.ACTION_STOP[1], new StopAction());

        // TODO: Should "Pause" when already paused send a "Play" action or another "Pause" action?
        registerAction(getPlayerPanel().getPlayButton(), PlayerPanel.ACTION_PLAY[1], new PlayAction());
        // OR THIS: togglePlayPauseAction();

        getProgressPanel().getPositionSlider().addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                final JSlider source = (JSlider) e.getSource();
                if (!source.getValueIsAdjusting()) {
                    PositionInfo positionInfo = getProgressPanel().getPositionInfo();
                    if (positionInfo != null) {

                        int newValue = source.getValue();
                        double seekTargetSeconds = newValue * positionInfo.getTrackDurationSeconds() / 100;
                        final String targetTime =
                                ModelUtil.toTimeString(
                                        new Long(Math.round(seekTargetSeconds)).intValue()
                                );

                        new SeekAction(targetTime).seek();
                    }
                }
            }
        });

        registerAction(getUriPanel().getSetButton(), URIPanel.ACTION_SET[1], new DefaultAction() {
            public void actionPerformed(ActionEvent e) {

                // Some validation
                final String uri = getUriPanel().getUriTextField().getText();
                if (uri == null || uri.length() == 0) return;
                try {
                    URI.create(uri);
                } catch (IllegalArgumentException ex) {
                    Workbench.APP.log(new LogMessage(
                            Level.WARNING, "AVTransport ControlPointAdapter", "Invalid URI, can't set on AVTransport: " + uri
                    ));
                }

                controlPoint.execute(
                        new SetAVTransportURI(getInstanceId(), getCallback().getService(), uri) {
                            @Override
                            public void success(ActionInvocation invocation) {
                                Workbench.APP.log(new LogMessage(
                                        "AVTransport ControlPointAdapter", "New transport URI set: " + uri
                                ));
                            }

                            @Override
                            public void failure(ActionInvocation invocation,
                                                UpnpResponse operation,
                                                String defaultMsg) {
                                log.severe(defaultMsg);
                            }
                        }
                );
            }
        });

    }

/*
    public void togglePlayPauseAction() {

        if (getPlayerPanel().getPlayButton().getActionCommand().equals(PlayerPanel.ACTION_PLAY[1])) {
            registerAction(getPlayerPanel().getPlayButton(), PlayerPanel.ACTION_PAUSE[1], new PauseAction());
        } else {
            registerAction(getPlayerPanel().getPlayButton(), PlayerPanel.ACTION_PLAY[1], new PlayAction());
        }
    }
*/

    public class PauseAction extends DefaultAction {
        public void actionPerformed(ActionEvent actionEvent) {
            controlPoint.execute(
                    new Pause(getInstanceId(), getCallback().getService()) {
                        @Override
                        public void success(ActionInvocation invocation) {
                            Workbench.APP.log(new LogMessage(
                                    "AVTransport ControlPointAdapter", "Called 'Pause' action successfully"
                            ));
                        }

                        @Override
                        public void failure(ActionInvocation invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            log.severe(defaultMsg);
                        }
                    }
            );
        }
    }

    public class PlayAction extends DefaultAction {
        public void actionPerformed(ActionEvent actionEvent) {
            controlPoint.execute(
                    new Play(getInstanceId(), getCallback().getService()) {
                        @Override
                        public void success(ActionInvocation invocation) {
                            Workbench.APP.log(new LogMessage(
                                    "AVTransport ControlPointAdapter", "Called 'Play' action successfully"
                            ));
                        }

                        @Override
                        public void failure(ActionInvocation invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            log.severe(defaultMsg);
                        }
                    }
            );
        }
    }

    public class StopAction extends DefaultAction {
        public void actionPerformed(ActionEvent actionEvent) {
            controlPoint.execute(
                    new Stop(getInstanceId(), getCallback().getService()) {
                        @Override
                        public void success(ActionInvocation invocation) {
                            Workbench.APP.log(new LogMessage(
                                    "AVTransport ControlPointAdapter", "Called 'Stop' action successfully"
                            ));
                        }

                        @Override
                        public void failure(ActionInvocation invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            log.severe(defaultMsg);
                        }
                    }
            );
        }
    }

    public class SeekAction extends DefaultAction {

        private String target;
        private int deltaSeconds;
        private boolean forwards;

        public SeekAction(String target) {
            this.target = target;
        }

        public SeekAction(int deltaSeconds, boolean forwards) {
            this.deltaSeconds = deltaSeconds;
            this.forwards = forwards;
        }

        public void actionPerformed(ActionEvent actionEvent) {
            seek();
        }

        public void seek() {
            String targetString;
            if (target != null) {
                targetString = target;
            } else {
                long currentSeconds = getProgressPanel().getPositionInfo() != null
                        ? getProgressPanel().getPositionInfo().getTrackElapsedSeconds()
                        : 0;

                long targetSeconds;
                if (forwards) {
                    targetSeconds = currentSeconds + deltaSeconds;
                } else {
                    targetSeconds = Math.min(0, currentSeconds - deltaSeconds);
                }
                targetString = ModelUtil.toTimeString(targetSeconds);
            }
            log.fine("Seeking to target time: " + targetString);

            // First update the internal model, so fast clicks will trigger seeks with the right offset
            getProgressPanel().setProgress(
                    new PositionInfo(
                            getProgressPanel().getPositionInfo(),
                            targetString,
                            targetString
                    )
            );

            // Now do the asynchronous remote seek
            controlPoint.execute(
                    new Seek(getInstanceId(), getCallback().getService(), targetString) {
                        @Override
                        public void success(final ActionInvocation invocation) {
                            Workbench.APP.log(new LogMessage(
                                    "AVTransport ControlPointAdapter", "Called 'Seek' action successfully"
                            ));
                        }

                        @Override
                        public void failure(ActionInvocation invocation,
                                            UpnpResponse operation,
                                            String defaultMsg) {
                            log.severe(defaultMsg);
                        }
                    }
            );
        }
    }


}
