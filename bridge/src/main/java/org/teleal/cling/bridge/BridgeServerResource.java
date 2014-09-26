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

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.registry.Registry;
import org.teleal.common.xhtml.Body;
import org.teleal.common.xhtml.XHTML;
import org.teleal.common.xhtml.XHTMLElement;
import org.teleal.common.xhtml.XHTMLParser;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.xpath.XPath;

/**
 * @author Christian Bauer
 */
public class BridgeServerResource {

    final private XHTMLParser parserXHTML = new XHTMLParser();

    protected ServletContext servletContext;

    protected UriInfo uriInfo;

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Context
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    @Context
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    protected BridgeUpnpService getUpnpService() {
        return (BridgeUpnpService)getServletContext().getAttribute(Constants.ATTR_UPNP_SERVICE);
    }

    protected Registry getRegistry() {
        return getUpnpService().getRegistry();
    }

    protected ControlPoint getControlPoint() {
        return getUpnpService().getControlPoint();
    }

    protected BridgeUpnpServiceConfiguration getConfiguration() {
        return getUpnpService().getConfiguration();
    }

    protected BridgeNamespace getNamespace() {
        return getUpnpService().getConfiguration().getNamespace();
    }

    protected String getFirstPathParamValue(String paramName) {
        MultivaluedMap<String, String> map = getUriInfo().getPathParameters();
        String value = map.getFirst(paramName);
        if (value == null) {
            throw new BridgeWebApplicationException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Desired path parameter value not found in request: " + paramName
            );
        }
        return value;
    }

    protected UDN getRequestedUDN() {
        return UDN.valueOf(getFirstPathParamValue(Constants.PARAM_UDN));
    }

    public XHTMLParser getParserXHTML() {
        return parserXHTML;
    }

    public void createHead(XHTMLElement root, String title) {
        root.createChild(XHTML.ELEMENT.head)
                .createChild(XHTML.ELEMENT.title)
                .setContent(title);
    }

    public Body createBodyTemplate(XHTML xhtml, XPath xpath, String title) {
        XHTMLElement root = xhtml.createRoot(xpath, XHTML.ELEMENT.html);
        createHead(root, title);
        root.createChild(XHTML.ELEMENT.body);
        return xhtml.getRoot(xpath).getBody();
    }

    protected String appendLocalCredentials(String uri) {
        return UriBuilder.fromUri(uri)
                .queryParam("auth", getConfiguration().getAuthManager().getLocalCredentials())
                .build().toString();
    }

}
