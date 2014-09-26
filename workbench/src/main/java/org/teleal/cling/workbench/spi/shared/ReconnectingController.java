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

package org.teleal.cling.workbench.spi.shared;

import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;
import org.teleal.cling.UpnpService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.ServiceReference;

import javax.swing.JFrame;
import javax.swing.JPanel;


public abstract class ReconnectingController extends AbstractController<JFrame> {


    final protected UpnpService upnpService;
    final protected ServiceReference serviceReference;
    final protected JPanel reconnectPanel;

    public ReconnectingController(Controller parentController, UpnpService upnpService, Service service) {
        super(new JFrame(), parentController);
        this.upnpService = upnpService;
        this.serviceReference = service.getReference();

        byte[] wakeOnLANBytes = service.getDevice() instanceof RemoteDevice
                ? ((RemoteDevice) service.getDevice()).getIdentity().getWakeOnLANBytes()
                : null;

        this.reconnectPanel = new ReconnectPanel(this, upnpService, wakeOnLANBytes) {
            protected void connect() {
                ReconnectingController.this.connect(resolveService());
            }
        };

        getView().setGlassPane(reconnectPanel);
    }

    public Service resolveService() {
        Service service = upnpService.getRegistry().getService(serviceReference);
        if (service == null) {
            onConnectFailure("Device service not registered/available");
            return null;
        }
        return service;
    }

    public abstract void connect(Service service);

    public void onConnectFailure(String msg) {
        getView().setTitle("Connection failed: " + msg);
        getView().getGlassPane().setVisible(true);
    }

    public void onConnect() {
        getView().setTitle("Connected to service...");
        getView().getGlassPane().setVisible(false);
    }

    public void onDisconnect() {
        getView().setTitle("Disconnected from service!");
        getView().getGlassPane().setVisible(true);
    }

}
