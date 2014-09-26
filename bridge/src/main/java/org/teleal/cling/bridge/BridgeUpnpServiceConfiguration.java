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

package org.teleal.cling.bridge;

import org.teleal.cling.DefaultUpnpServiceConfiguration;
import org.teleal.cling.bridge.auth.AuthManager;
import org.teleal.cling.bridge.auth.SecureHashAuthManager;
import org.teleal.cling.bridge.gateway.FormActionProcessor;
import org.teleal.cling.bridge.link.proxy.CombinedDescriptorBinder;
import org.teleal.cling.transport.Router;
import org.teleal.cling.transport.spi.InitializationException;
import org.teleal.cling.transport.spi.NetworkAddressFactory;
import org.teleal.cling.transport.spi.StreamServer;
import org.teleal.cling.transport.spi.StreamServerConfiguration;
import org.teleal.common.util.URIUtil;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class BridgeUpnpServiceConfiguration extends DefaultUpnpServiceConfiguration {

    final private static Logger log = Logger.getLogger(BridgeUpnpServiceConfiguration.class.getName());

    final private URL localBaseURL;
    final private String contextPath;
    final private CombinedDescriptorBinder combinedDescriptorBinder;
    final private FormActionProcessor actionProcessor;
    final private AuthManager authManager;

    public BridgeUpnpServiceConfiguration(URL localBaseURL) {
        this(localBaseURL, "");
    }

    public BridgeUpnpServiceConfiguration(URL localBaseURL, String contextPath) {
        super(localBaseURL.getPort(), false);
        this.localBaseURL = localBaseURL;
        this.contextPath = contextPath;
        this.actionProcessor = createFormActionProcessor();
        this.combinedDescriptorBinder = createCombinedDescriptorBinder();
        this.authManager = createAuthManager();

        log.info("Bridge configured with local URL: " + getLocalEndpointURLWithCredentials());
    }

    public URL getLocalBaseURL() {
        return localBaseURL;
    }

    public String getContextPath() {
        return contextPath;
    }

    public CombinedDescriptorBinder getCombinedDescriptorBinder() {
        return combinedDescriptorBinder;
    }

    public FormActionProcessor getActionProcessor() {
        return actionProcessor;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    protected CombinedDescriptorBinder createCombinedDescriptorBinder() {
        return new CombinedDescriptorBinder(this);
    }

    protected FormActionProcessor createFormActionProcessor() {
        return new FormActionProcessor();
    }

    protected AuthManager createAuthManager() {
        return new SecureHashAuthManager();
    }

    public URL getLocalEndpointURL() {
        try {
            return new URL(
                    getLocalBaseURL().getProtocol(),
                    getLocalBaseURL().getHost(),
                    getLocalBaseURL().getPort(),
                    getNamespace().getBasePath().toString()
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public URL getLocalEndpointURLWithCredentials() {
        StringBuilder url = new StringBuilder();
        url.append(getLocalEndpointURL().toString()).append("/");
        url.append("?").append(SecureHashAuthManager.QUERY_PARAM_AUTH);
        url.append("=").append(getAuthManager().getLocalCredentials());
        return URIUtil.toURL(URI.create(url.toString()));
    }

    // TODO: Make the network interfaces/IPs for binding configurable with servlet context params

    @Override
    public BridgeNamespace getNamespace() {
        return new BridgeNamespace(getContextPath());
    }

    // The job of the StreamServer is now taken care of by the GatewayFilter

    @Override
    public StreamServer createStreamServer(NetworkAddressFactory networkAddressFactory) {
        return new StreamServer() {
            public void init(InetAddress bindAddress, Router router) throws InitializationException {
            }

            public int getPort() {
                return getLocalBaseURL().getPort();
            }

            public void stop() {
            }

            public StreamServerConfiguration getConfiguration() {
                return null;
            }

            public void run() {
            }
        };
    }
}
