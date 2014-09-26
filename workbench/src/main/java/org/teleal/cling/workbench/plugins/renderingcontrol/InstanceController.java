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

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.meta.StateVariableAllowedValueRange;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.renderingcontrol.callback.GetVolume;
import org.teleal.cling.support.renderingcontrol.callback.SetMute;
import org.teleal.cling.support.renderingcontrol.callback.SetVolume;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class InstanceController extends AbstractController<JPanel> {

    final private static Logger log = Logger.getLogger(InstanceController.class.getName());

    public static final ImageIcon ICON_MUTE_OFF =
            Application.createImageIcon(RenderingControlPointAdapter.class, "img/32/speaker.png");

    public static final ImageIcon ICON_MUTE_ON =
            Application.createImageIcon(RenderingControlPointAdapter.class, "img/32/speaker_mute.png");

    // Dependencies
    final protected UnsignedIntegerFourBytes instanceId;
    final protected ControlPoint controlPoint;
    final protected RenderingControlCallback callback;

    // View
    final private JToggleButton muteButton;
    final private JSlider volumeSlider;

    public InstanceController(Controller parentController, ControlPoint controlPoint,
                              RenderingControlCallback callback, UnsignedIntegerFourBytes instanceId ) {
        super(new JPanel(), parentController);

        this.instanceId = instanceId;
        this.controlPoint = controlPoint;
        this.callback = callback;

        muteButton = createMuteButton();
        volumeSlider = createVolumeSlider(callback.getService());

        getView().setLayout(new BorderLayout());
        getView().add(muteButton, BorderLayout.WEST);
        getView().add(volumeSlider, BorderLayout.CENTER);
        getView().setPreferredSize(new Dimension(300, 80));

    }

    public UnsignedIntegerFourBytes getInstanceId() {
        return instanceId;
    }

    public RenderingControlCallback getCallback() {
        return callback;
    }

    public JToggleButton getMuteButton() {
        return muteButton;
    }

    public JSlider getVolumeSlider() {
        return volumeSlider;
    }

    protected JToggleButton createMuteButton() {
        final JToggleButton muteButton = new JToggleButton("Mute", ICON_MUTE_OFF);

        muteButton.setVerticalTextPosition(JToggleButton.BOTTOM);
        muteButton.setHorizontalTextPosition(JToggleButton.CENTER);
        muteButton.setFocusable(false);
        muteButton.setPreferredSize(new Dimension(60, 50));

        muteButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    muteButton.setIcon(ICON_MUTE_ON);
                    volumeSlider.setEnabled(false);
                } else {
                    muteButton.setIcon(ICON_MUTE_OFF);
                    volumeSlider.setEnabled(true);
                }
            }
        });

        muteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                callSetMuteAction(muteButton.isSelected());
            }
        });
        return muteButton;
    }

    protected JSlider createVolumeSlider(Service service) {
        final JSlider volumeSlider;
        // Get the volume range supported by the service, if there isn't any, assume 0..100
        int minVolume = 0;
        int maxVolume = 100;
        if (service.getStateVariable("Volume") != null) {
            StateVariableAllowedValueRange volumeRange =
                    service.getStateVariable("Volume").getTypeDetails().getAllowedValueRange();

            if (volumeRange != null) {
                minVolume = new Long(volumeRange.getMinimum()).intValue();
                maxVolume = new Long(volumeRange.getMaximum()).intValue();
            }
        }

        volumeSlider = new JSlider(JSlider.HORIZONTAL, minVolume, maxVolume, maxVolume / 2);
        volumeSlider.setBorder(BorderFactory.createTitledBorder("Volume"));
        volumeSlider.setMajorTickSpacing(maxVolume / 5);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);

        volumeSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();

                if (!source.getValueIsAdjusting()) {
                    int newVolume = source.getValue();
                    callSetVolumeAction(newVolume);

                }
            }
        });

        return volumeSlider;
    }

    public void updateVolume() {
        controlPoint.execute(new GetVolume(getInstanceId(), getCallback().getService()) {
            @Override
            public void received(ActionInvocation actionInvocation, final int currentVolume) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        setVolumeSliderWithoutNotification(currentVolume);
                    }
                });
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                log.severe("Can't retrieve initial volume: " + defaultMsg);
            }
        });
    }

    // Internal re-positioning, should not fire a Seek UPnP action, so we remove
    // the listener before and add it back afterwards TODO: valueadjusting crap doesn't work!
    protected void setVolumeSliderWithoutNotification(int value) {
        if (value == volumeSlider.getValue()) return;
        ChangeListener[] listeners = volumeSlider.getChangeListeners();
        for (ChangeListener listener : listeners) {
            volumeSlider.removeChangeListener(listener);
        }
        volumeSlider.setValue(value);
        for (ChangeListener listener : listeners) {
            volumeSlider.addChangeListener(listener);
        }
        // Mute button state depends on volume state
        muteButton.setSelected(value == 0);
    }

    protected void callSetVolumeAction(final int newVolume) {

        controlPoint.execute(new SetVolume(getInstanceId(), getCallback().getService(), newVolume) {
            @Override
            public void success(ActionInvocation invocation) {
                Workbench.APP.log(new LogMessage(
                        "Rendering ControlPointAdapter", "Service volume set to: " + newVolume
                ));
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                log.warning("Can't set volume: " + defaultMsg);
            }
        });

    }

    protected void callSetMuteAction(final boolean desiredMute) {

        controlPoint.execute(new SetMute(getInstanceId(), getCallback().getService(), desiredMute) {
            @Override
            public void success(ActionInvocation invocation) {
                Workbench.APP.log(new LogMessage(
                        "Rendering ControlPointAdapter", "Service mute set to: " + (desiredMute ? "ON" : "OFF")
                ));
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                log.warning("Can't set mute: " + defaultMsg);
            }
        });

    }

}
