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

package org.teleal.cling.bridge.gateway;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.teleal.cling.bridge.BridgeWebApplicationException;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.common.util.Exceptions;
import org.teleal.common.xhtml.Body;
import org.teleal.common.xhtml.XHTML;
import org.teleal.common.xhtml.XHTMLElement;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.teleal.common.xhtml.XHTML.ELEMENT;

/**
 * @author Christian Bauer
 */
@Path("/dev")
public class DeviceResource extends GatewayServerResource {

    final private static Logger log = Logger.getLogger(DeviceResource.class.getName());

    @GET
    public XHTML browseAll() {

        XHTML result = getParserXHTML().createDocument();
        Body body = createBodyTemplate(result, getParserXHTML().createXPath(), "Devices");

        body.createChild(ELEMENT.h1).setContent("Devices");

        XHTMLElement registryContainer = body.createChild(ELEMENT.div).setId("registry");

        List<Device> devices = new ArrayList(getRegistry().getDevices());

        representDeviceList(registryContainer, devices);

        return result;
    }

    @GET
    @Path("/{UDN}")
    public XHTML browse() {
        Device device = getRequestedDevice();

        XHTML result = getParserXHTML().createDocument();
        Body body = createBodyTemplate(result, getParserXHTML().createXPath(), device.getClass().getSimpleName());

        body.createChild(ELEMENT.h1).setContent(device.getClass().getSimpleName());

        representDevice(body, device);

        return result;
    }

    @GET
    @Path("/{UDN}/desc.xml")
    public Response retrieveMappedDescriptor() {
        RemoteDevice rd = getRequestedRemoteDevice();
        String url = rd.getIdentity().getDescriptorURL().toString();
        log.fine("Retrieving device descriptor from remote URL: " + url);
        try {
            ClientRequest request = new ClientRequest(url);
            request.accept(MediaType.TEXT_XML);
            return request.get(String.class);
        } catch (Exception ex) {
            throw new BridgeWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Retrieving mapped descriptor failed: " + Exceptions.unwrap(ex)
            );
        }
    }

    @GET
    @Path("/{UDN}/MappedIcon/{Index}")
    public Response retrieveMappedIcon(@PathParam("Index") int index) {
        RemoteDevice rd = getRequestedRemoteDevice();

        if (!rd.hasIcons() || rd.getIcons().length - 1 < index) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        URL iconURL = rd.normalizeURI(rd.getIcons()[index].getUri());
        log.fine("Retrieving icon from remote URL: " + iconURL);
        try {
            ClientRequest request = new ClientRequest(iconURL.toString());
            request.accept("image/*");
            // If I return the Response directly, Resteasy puts some broken additional headers on it - but of
            // course only if I serve the icon from my own Cling library code. I have no idea why, all seems valid.
            // return request.get(byte[].class);
            ClientResponse res = request.get(byte[].class);
            return Response.ok(res.getEntity(), res.getHeaders().getFirst("Content-Type").toString()).build();
        } catch (Exception ex) {
            throw new BridgeWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Retrieving mapped icon failed: " + Exceptions.unwrap(ex)
            );
        }
    }

}
