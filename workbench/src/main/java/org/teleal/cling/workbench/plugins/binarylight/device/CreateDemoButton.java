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

package org.teleal.cling.workbench.plugins.binarylight.device;

import org.teleal.cling.model.types.UDN;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class CreateDemoButton extends JButton {

    public static String[] ACTION_CREATE_DEVICE = {"Create Demo Device", "createDemoDevice"};

    public CreateDemoButton(final Controller controller) {
        super(ACTION_CREATE_DEVICE[0], Application.createImageIcon(Workbench.class, "img/24/lightbulb.png", ACTION_CREATE_DEVICE[0]));

        controller.registerAction(
                this,
                ACTION_CREATE_DEVICE[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        new Thread() {
                            @Override
                            public void run() {
                                UDN udn = UDN.uniqueSystemIdentifier("Demo Binary Light");
                                if (Workbench.APP.getUpnpService().getRegistry().getDevice(udn, true) != null) {
                                    Workbench.APP.log(Level.INFO, "Local demo device already exists!");
                                } else {
                                    new DemoBinaryLightUI(Workbench.APP.getUpnpService(), udn);
                                }
                            }
                        }.start();
                    }
                }
        );
    }
}
