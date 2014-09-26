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

package org.teleal.cling.bridge.link.proxy;

import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.UDAVersion;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.UDN;

import java.util.List;

/**
 * @author Christian Bauer
 */
public class ProxyLocalDevice extends LocalDevice {

    public ProxyLocalDevice(ProxyDeviceIdentity identity) throws ValidationException {
        super(identity);
    }

    public ProxyLocalDevice(ProxyDeviceIdentity identity, UDAVersion version, DeviceType type, DeviceDetails details,
                            Icon[] icons,
                            LocalService[] services,
                            LocalDevice[] embeddedDevices) throws ValidationException {
        super(identity, version, type, details, icons, services, embeddedDevices);
    }

    @Override
    public ProxyDeviceIdentity getIdentity() {
        return (ProxyDeviceIdentity)super.getIdentity();
    }

    @Override
    public ProxyLocalDevice newInstance(UDN udn, UDAVersion version, DeviceType type, DeviceDetails details,
                                   Icon[] icons, LocalService[] services, List<LocalDevice> embeddedDevices) throws ValidationException {
        return new ProxyLocalDevice(
                new ProxyDeviceIdentity(udn, getIdentity().getMaxAgeSeconds(), getIdentity().getEndpoint()),
                version, type, details, icons,
                services,
                embeddedDevices.size() > 0 ? embeddedDevices.toArray(new LocalDevice[embeddedDevices.size()]) : null
        );
    }

}
