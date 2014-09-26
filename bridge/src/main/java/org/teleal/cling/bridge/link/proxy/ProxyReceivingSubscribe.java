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

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.message.StreamRequestMessage;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.message.gena.OutgoingSubscribeResponseMessage;
import org.teleal.cling.protocol.sync.ReceivingSubscribe;

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class ProxyReceivingSubscribe extends ReceivingSubscribe {

    private static Logger log = Logger.getLogger(ProxyReceivingSubscribe.class.getName());

    private final ProxyLocalService proxyService;

    public ProxyReceivingSubscribe(UpnpService upnpService, StreamRequestMessage inputMessage, ProxyLocalService proxyService) {
        super(upnpService, inputMessage);

        this.proxyService = proxyService;
    }

    public ProxyLocalService getProxyService() {
        return proxyService;
    }

    @Override
    protected OutgoingSubscribeResponseMessage executeSync() {
        // TODO
        log.warning("Subscription request on proxy service, not implemented!");
        return new OutgoingSubscribeResponseMessage(UpnpResponse.Status.NOT_IMPLEMENTED);
    }
}
