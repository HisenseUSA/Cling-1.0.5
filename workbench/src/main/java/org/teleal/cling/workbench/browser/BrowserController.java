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

package org.teleal.cling.workbench.browser;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.workbench.Workbench;
import org.teleal.cling.workbench.WorkbenchController;
import org.teleal.cling.workbench.shared.DeviceInfoSelectionChangedEvent;
import org.teleal.cling.workbench.shared.RefreshDevicesEvent;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.DefaultEvent;
import org.teleal.common.swingfwk.DefaultEventListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

public class BrowserController extends AbstractController<JPanel> {

    // Actions
    public static String[] ACTION_REFRESH = {"Refresh", "refreshDevices"};

    // Model
    DeviceListModel listModel = new DeviceListModel();

    // View
    final JToolBar browserToolBar = new JToolBar();
    private final JButton refreshDevicesButton =
            new JButton(ACTION_REFRESH[0], Application.createImageIcon(Workbench.class, "img/24/search.png", ACTION_REFRESH[0]));

    final DeviceList deviceList = new DeviceList(this, listModel);
    final JScrollPane listPane = new JScrollPane(deviceList);

    public BrowserController(JPanel view, WorkbenchController parentController) {
        super(view, parentController);

        getView().setLayout(new BorderLayout());

        refreshDevicesButton.setPreferredSize(new Dimension(2500, 32));
        registerAction(
                refreshDevicesButton,
                ACTION_REFRESH[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Workbench.APP.getUpnpService().getRegistry().removeAllRemoteDevices();
                        Workbench.APP.getUpnpService().getControlPoint().search(1);
                        fireEventGlobal(new RefreshDevicesEvent(Workbench.APP.getUpnpService()));
                    }
                }
        );

        browserToolBar.setFloatable(false);
        browserToolBar.add(refreshDevicesButton);

        listPane.setPreferredSize(new Dimension(100, 100)); // Disables auto-resizing, will fit Container view

        getView().add(browserToolBar, BorderLayout.SOUTH);
        getView().add(listPane, BorderLayout.CENTER);

        // Sync the selection of this list with the selection on the device info (tabs)
        registerEventListener(
                DeviceInfoSelectionChangedEvent.class,
                new DefaultEventListener<Device>() {
                    public void handleEvent(DefaultEvent<Device> e) {
                        int indexOfDevice = deviceList.getModel().indexOf(e.getPayload());
                        if (indexOfDevice != -1) {
                            deviceList.setSelectedIndex(indexOfDevice);
                            deviceList.ensureIndexIsVisible(indexOfDevice);
                        } else {
                            deviceList.clearSelection();
                        }
                    }
                }
        );

        // Start listening to UPnP registry events
        parentController.getUpnpService().getRegistry().addListener(listModel);
    }

}
