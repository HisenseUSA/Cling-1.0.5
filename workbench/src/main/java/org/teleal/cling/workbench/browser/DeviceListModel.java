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

import org.teleal.cling.bridge.link.proxy.ProxyLocalDevice;
import org.teleal.cling.model.message.StreamRequestMessage;
import org.teleal.cling.model.message.StreamResponseMessage;
import org.teleal.cling.model.message.UpnpRequest;
import org.teleal.cling.model.message.header.ContentTypeHeader;
import org.teleal.cling.model.message.header.UpnpHeader;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.registry.RegistryListener;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.logging.LogMessage;
import org.teleal.common.util.MimeType;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import java.util.logging.Level;


public class DeviceListModel extends DefaultListModel implements RegistryListener {

    public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {

    }

    public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {

    }

    public void remoteDeviceAdded(Registry registry, RemoteDevice device) {

        Workbench.APP.log(new LogMessage(Level.INFO, "Remote device added: " + device));

        final DeviceItem display =
                new DeviceItem(
                        device,
                        device.getDetails().getFriendlyName(),
                        device.getDisplayString(),
                        "(REMOTE) " + device.getType().getDisplayString()
                );

        Icon usableIcon = findUsableIcon(device);

        if (usableIcon != null) {

            // We retrieve it using our own infrastructure, we know how that works and behaves

            final StreamRequestMessage iconRetrievalMsg =
                    new StreamRequestMessage(UpnpRequest.Method.GET, device.normalizeURI(usableIcon.getUri()));

            StreamResponseMessage responseMsg = Workbench.APP.getUpnpService().getRouter().send(iconRetrievalMsg);

            if (responseMsg != null && !responseMsg.getOperation().isFailed()) {

                MimeType contentType =
                        responseMsg.getHeaders().getFirstHeader(UpnpHeader.Type.CONTENT_TYPE, ContentTypeHeader.class).getValue();

                if (isUsableImageType(contentType)) {
                    byte[] imageBody = (byte[]) responseMsg.getBody();
                    if (imageBody != null) {
                        ImageIcon imageIcon = new ImageIcon(imageBody);
                        display.setIcon(imageIcon);
                    } else {
                        Workbench.APP.log(
                                Level.WARNING,
                                "Icon request did not return with response body '" + contentType + "': " + iconRetrievalMsg.getUri()
                        );
                    }
                } else {
                    Workbench.APP.log(
                            Level.WARNING,
                            "Icon was delivered with unsupported content type '" + contentType + "': " + iconRetrievalMsg.getUri()
                    );
                }

            } else {
                if (responseMsg != null) {
                    Workbench.APP.log(
                            Level.WARNING,
                            "Icon retrieval of '" + iconRetrievalMsg.getUri() + "' failed: " +
                                    responseMsg.getOperation().getResponseDetails()
                    );
                } else {
                    Workbench.APP.log(
                            Level.WARNING,
                            "Icon retrieval of '" + iconRetrievalMsg.getUri() + "' failed, no response received."
                    );
                }
            }
        }

        if (display.getIcon() == null) {
            display.setIcon(Application.createImageIcon(Workbench.class, "img/48/unknown_device.png", display.getLabel()[0]));
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addElement(display);
            }
        });
    }

    public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
        final DeviceItem display = new DeviceItem(device, device.getDisplayString());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeElement(display);
            }
        });
    }

    public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
        // Too much information
        // Workbench.APP.log(Level.INFO, "Received update: " + device);
    }

    public void localDeviceAdded(Registry registry, LocalDevice device) {

        String[] labels = device instanceof ProxyLocalDevice
                ?
                new String[]{
                        device.getDetails().getFriendlyName(),
                        device.getDisplayString(),
                        "(PROXY) " + device.getType().getDisplayString(),
                        ((ProxyLocalDevice) device).getIdentity().getEndpoint().getCallback().toString()
                }
                :
                new String[]{
                        device.getDetails().getFriendlyName(),
                        device.getDisplayString(),
                        "(LOCAL) " + device.getType().getDisplayString()
                };

        final DeviceItem display = new DeviceItem(device, labels);

        Icon usableIcon = findUsableIcon(device);
        if (usableIcon != null) {
            ImageIcon imageIcon = new ImageIcon(usableIcon.getData());
            display.setIcon(imageIcon);
        } else {
            display.setIcon(Application.createImageIcon(Workbench.class, "img/48/unknown_device.png", display.getLabel()[0]));
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                addElement(display);
            }
        });

    }

    public void localDeviceRemoved(Registry registry, LocalDevice device) {
        final DeviceItem display = new DeviceItem(device, device.getDisplayString());
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeElement(display);
            }
        });
    }

    protected Icon findUsableIcon(Device device) {
        // Needs to be certain format and size
        for (Object o : device.getIcons()) {
            Icon icon = (Icon) o;
            if (icon.getWidth() <= 64 && icon.getHeight() <= 64 && isUsableImageType(icon.getMimeType()))
                return icon;
        }
        return null;
    }

    protected boolean isUsableImageType(MimeType mt) {
        return mt.getType().equals("image") &&
                (mt.getSubtype().equals("png") || mt.getSubtype().equals("jpg") ||
                        mt.getSubtype().equals("jpeg") || mt.getSubtype().equals("gif"));
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Device) {
            Device device = (Device) o;
            DeviceItem display = new DeviceItem(device, device.getDisplayString());
            return indexOf(display);
        }
        return super.indexOf(o);
    }

    public void beforeShutdown(Registry registry) {
    }

    public void afterShutdown() {
    }
}
