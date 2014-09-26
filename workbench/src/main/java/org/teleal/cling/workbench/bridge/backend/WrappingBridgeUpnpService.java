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

package org.teleal.cling.workbench.bridge.backend;

import org.teleal.cling.UpnpService;
import org.teleal.cling.bridge.BridgeProtocolFactory;
import org.teleal.cling.bridge.BridgeUpnpService;
import org.teleal.cling.bridge.BridgeUpnpServiceConfiguration;
import org.teleal.cling.bridge.link.LinkManager;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.protocol.ProtocolFactory;
import org.teleal.cling.registry.Registry;
import org.teleal.cling.transport.Router;

/**
 * @author Christian Bauer
 */
public class WrappingBridgeUpnpService implements BridgeUpnpService {

    final private UpnpService wrapped;
    final private BridgeUpnpServiceConfiguration configuration;
    final private LinkManager linkManager;

    public WrappingBridgeUpnpService(UpnpService wrapped, BridgeUpnpServiceConfiguration configuration) {
        this.wrapped = wrapped;
        this.configuration = configuration;
        this.linkManager = createLinkManager();
        getRegistry().addListener(linkManager.getDeviceDiscovery());
    }

    protected LinkManager createLinkManager() {
        return new LinkManager(this);
    }

    public BridgeUpnpServiceConfiguration getConfiguration() {
        return configuration;
    }

    public LinkManager getLinkManager() {
        return linkManager;
    }

    public ControlPoint getControlPoint() {
        return wrapped.getControlPoint();
    }

    public ProtocolFactory getProtocolFactory() {
        return new BridgeProtocolFactory(this);
    }

    public Registry getRegistry() {
        return wrapped.getRegistry();
    }

    public Router getRouter() {
        return wrapped.getRouter();
    }

    public void shutdown() {
        getLinkManager().shutdown();
    }
}
