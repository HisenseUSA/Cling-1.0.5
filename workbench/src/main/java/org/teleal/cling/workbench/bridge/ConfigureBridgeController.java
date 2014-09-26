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

package org.teleal.cling.workbench.bridge;

import org.teleal.cling.UpnpService;
import org.teleal.cling.bridge.BridgeUpnpService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.workbench.bridge.backend.Bridge;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Christian Bauer
 */
public class ConfigureBridgeController extends AbstractController<JFrame> {

    // Dependencies
    final protected UpnpService upnpService;
    final protected Bridge bridge;
    final protected SettingsController settingsController;
    final protected EndpointController endpointController;

    // View

    public ConfigureBridgeController(Controller parentController, UpnpService upnpService, Bridge bridge) {
        super(new JFrame("WAN Bridge"), parentController);

        this.upnpService = upnpService;
        this.bridge = bridge;

        settingsController = new SettingsController(this, upnpService, bridge);
        endpointController = new EndpointController(this, bridge);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(settingsController.getView(), BorderLayout.NORTH);
        mainPanel.add(endpointController.getView(), BorderLayout.CENTER);

        getView().add(mainPanel);
        getView().setMinimumSize(new Dimension(600, 300));
        getView().pack();

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        ConfigureBridgeController.this.dispose();
                    }
                }
        );
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public Bridge getBridge() {
        return bridge;
    }

}
