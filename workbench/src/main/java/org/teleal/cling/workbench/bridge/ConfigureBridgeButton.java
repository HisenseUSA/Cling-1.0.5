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

import org.teleal.cling.model.meta.Service;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.DefaultEvent;
import org.teleal.common.swingfwk.DefaultEventListener;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class ConfigureBridgeButton extends JButton {

    public static String[] ACTION_BRIDGE = {"Configure WAN Bridge", "configureWANBridge"};

    public ConfigureBridgeButton(final Controller controller) {
        super(ACTION_BRIDGE[0], Application.createImageIcon(Workbench.class, "img/24/device.png", ACTION_BRIDGE[0]));

        controller.registerAction(
                this,
                ACTION_BRIDGE[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        new ConfigureBridgeController(
                                controller,
                                Workbench.APP.getUpnpService(),
                                Workbench.APP.getBridge()
                        ).getView().setVisible(true);
                    }
                }
        );
    }
}
