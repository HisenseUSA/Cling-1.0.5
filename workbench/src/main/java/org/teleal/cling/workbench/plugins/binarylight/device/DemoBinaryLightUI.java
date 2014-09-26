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

import org.teleal.cling.UpnpService;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.workbench.Constants;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class DemoBinaryLightUI extends AbstractController<JFrame> {

    // Model
    final private LocalDevice binaryLightDevice;

    // View
    final private JLabel iconLabel = new JLabel();
    final private ImageIcon onIcon = new ImageIcon(getClass().getResource("img/lightbulb.png"));
    final private ImageIcon offIcon = new ImageIcon(getClass().getResource("img/lightbulb_off.png"));

    public DemoBinaryLightUI(final UpnpService upnpService, UDN udn) {
        super(new JFrame(), null);

        LocalService service =
                new AnnotationLocalServiceBinder().read(DemoBinaryLight.class);

        service.setManager(
                new DefaultServiceManager(service) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new DemoBinaryLight() {
                            @Override
                            public void setTarget(boolean newTargetValue) {
                                super.setTarget(newTargetValue);
                                if (newTargetValue) {
                                    switchLightOn();
                                } else {
                                    switchLightOff();
                                }
                            }
                        };
                    }
                }
        );

        try {
            binaryLightDevice = DemoBinaryLight.createDefaultDevice(udn, service);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        upnpService.getRegistry().addDevice(binaryLightDevice);

        //upnpService.getRegistry().printDebugLog();

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        upnpService.getRegistry().removeDevice(binaryLightDevice);
                    }
                }
        );

        // Default state
        switchLightOff();

        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        getView().getContentPane().add(iconLabel);

        Application.center(getView(), Workbench.APP.getView());
        getView().pack();
        getView().setSize(new Dimension(300, 300));
        getView().setMinimumSize(new Dimension(100, 100));
        getView().setResizable(true);
        getView().setVisible(true);
    }

    protected void switchLightOn() {
        iconLabel.setIcon(onIcon);
        getView().getContentPane().setBackground(Constants.GREEN_DARK);
        getView().setTitle(binaryLightDevice.getDetails().getFriendlyName() + ": ON");
    }

    protected void switchLightOff() {
        iconLabel.setIcon(offIcon);
        getView().getContentPane().setBackground(Color.BLACK);
        getView().setTitle(binaryLightDevice.getDetails().getFriendlyName() + ": OFF");
    }


}
