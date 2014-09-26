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

package org.teleal.cling.workbench.info;

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;

import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import java.awt.BorderLayout;


public class DeviceInfoController extends AbstractController<DeviceInfo> {

    // Model
    private final Device device;

    // View
    private final DeviceInfoToolBar deviceToolBar;
    private final JScrollPane deviceTreeScrollPane;
    private final DeviceTree deviceTree;

    //private final JTextArea textArea = new JTextArea();

    public DeviceInfoController(DeviceInfo view, Controller parentController, Device device, ImageIcon deviceIcon) {
        super(view, parentController);

        this.device = device;

        /*
        textArea.setEditable(false);
        StringBuilder desc = new StringBuilder();
        desc.append("ROOT DEVICE:\n\n");
        desc.append(Workbench.getRouter().getConfig().getDeviceDescriptorBinderUDA10().generate(device));
        desc.append("\n\n\n");
        appendServices(desc, device);
        textArea.setText(desc.toString());
        getView().add(textArea);
        */

        deviceToolBar = new DeviceInfoToolBar(this);

        // Tree
        deviceTree = new DeviceTree(
                Workbench.APP.getUpnpService().getConfiguration().getNamespace(),
                device,
                deviceIcon
        );
        deviceTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        deviceTree.addTreeSelectionListener(
                new TreeSelectionListener() {
                    public void valueChanged(TreeSelectionEvent event) {
                        if (deviceTree.getLastSelectedPathComponent() != null) {
                            deviceToolBar.deviceInfoSelected();
                        }
                    }
                }
        );

        deviceTreeScrollPane = new JScrollPane(deviceTree);
        getView().add(deviceToolBar, BorderLayout.NORTH);
        getView().add(deviceTreeScrollPane, BorderLayout.CENTER);

        // Expand first node (root device)
        deviceTree.expandRow(0);
    }

    public Device getDevice() {
        return device;
    }

    public DeviceTree getDeviceTree() {
        return deviceTree;
    }

    public void close() {
        getView().closeTab();
    }


}
