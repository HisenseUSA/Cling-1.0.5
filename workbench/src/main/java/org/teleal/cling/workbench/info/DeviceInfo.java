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
import org.teleal.common.swingfwk.Controller;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;


public class DeviceInfo extends JPanel {

    private final JTabbedPane tabs;
    private final Device device;

    private DeviceInfoController controller;

    public DeviceInfo(JTabbedPane tabs, Device device) {
        this.tabs = tabs;
        this.device = device;
        setLayout(new BorderLayout());
    }

    public void openTab(Controller parent, ImageIcon deviceIcon) {
        controller = new DeviceInfoController(this, parent, device, deviceIcon);
    }

    public void closeTab() {
        tabs.remove(this);
        controller.dispose();
    }

    public Device getDevice() {
        return device;
    }

    public DeviceInfoController getController() {
        return controller;
    }

    public String getTitle() {
        return getDevice().getDetails().getFriendlyName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        if (!device.getIdentity().getUdn().equals(that.device.getIdentity().getUdn())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return device.getIdentity().getUdn().hashCode();
    }
}
