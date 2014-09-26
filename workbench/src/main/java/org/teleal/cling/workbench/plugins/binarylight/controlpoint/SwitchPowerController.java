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

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.SubscriptionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.workbench.Workbench;
import org.teleal.cling.workbench.spi.shared.ReconnectingController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;


public class SwitchPowerController extends ReconnectingController {

    private static Logger log = Logger.getLogger(SwitchPowerController.class.getName());

    public static final ImageIcon ICON_ON = Application.createImageIcon(SwitchPowerControlPointAdapter.class, "img/switch_down.png");
    public static final ImageIcon ICON_OFF = Application.createImageIcon(SwitchPowerControlPointAdapter.class, "img/switch_up.png");

    // Dependencies
    protected SubscriptionCallback callback;

    // View
    final private JToggleButton toggleButton;

    public SwitchPowerController(Controller parentController, UpnpService upnpService, Service service) {
        super(parentController, upnpService, service);

        toggleButton = createToggleButton();
        toggleButton.setBorderPainted(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(toggleButton, BorderLayout.CENTER);
        mainPanel.setBackground(Color.WHITE);

        getView().add(mainPanel);

        getView().setPreferredSize(new Dimension(250, 250));
        getView().setMinimumSize(new Dimension(250, 250));
        getView().pack();

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        SwitchPowerController.this.dispose();
                    }
                }
        );

        connect(resolveService());
    }

    public void connect(Service service) {
        if (service == null) return;
        callback = new SwitchPowerSubscriptionCallback(service, this);
        upnpService.getControlPoint().execute(callback);
    }

    @Override
    public void dispose() {
        if (callback != null)
            callback.end(); // End subscription when controller ends
        super.dispose();
    }

    @Override
    public void onConnectFailure(String msg) {
        super.onConnectFailure(msg);
        getToggleButton().setEnabled(false);
        log.warning("Connection failed: " + msg);
    }

    @Override
    public void onConnect() {
        super.onConnect();
        getToggleButton().setEnabled(true);
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        getToggleButton().setEnabled(false);
    }

    public JToggleButton getToggleButton() {
        return toggleButton;
    }

    protected JToggleButton createToggleButton() {
        final JToggleButton button = new JToggleButton(ICON_OFF);

        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(128, 128));

        button.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent itemEvent) {
                int state = itemEvent.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    button.setIcon(ICON_ON);
                } else {
                    button.setIcon(ICON_OFF);
                }
            }
        });

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                callSetTargetAction(resolveService(), button.isSelected());
            }
        });

        // Initial state is disabled, until we receive the initial event
        button.setEnabled(false);
        return button;
    }

    protected void callSetTargetAction(Service service, final boolean desiredTarget) {
        if (service == null) return;
        upnpService.getControlPoint().execute(new SetTarget(service, desiredTarget) {
            @Override
            public void success(ActionInvocation invocation) {
                Workbench.APP.log(new LogMessage(
                        "SwitchPower ControlPointAdapter", "Target set to: " + (desiredTarget ? "ON" : "OFF")
                ));
            }

            @Override
            public void failure(ActionInvocation invocation,
                                UpnpResponse operation,
                                String defaultMsg) {
                log.warning("Can't set target: " + defaultMsg);
            }
        });
    }

}