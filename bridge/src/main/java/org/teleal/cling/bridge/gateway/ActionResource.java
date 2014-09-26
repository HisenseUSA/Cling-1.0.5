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

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.teleal.cling.bridge.BridgeWebApplicationException;
import org.teleal.cling.bridge.auth.SecureHashAuthManager;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.InvalidValueException;
import org.teleal.common.util.Exceptions;
import org.teleal.common.xhtml.Body;
import org.teleal.common.xhtml.XHTML;
import org.teleal.common.xhtml.XHTMLElement;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

import static org.teleal.common.xhtml.XHTML.ATTR;
import static org.teleal.common.xhtml.XHTML.ELEMENT;

/**
 * @author Christian Bauer
 */
@Path("/dev/{UDN}/svc/{ServiceIdNamespace}/{ServiceId}/action")
public class ActionResource extends GatewayServerResource {

    final private static Logger log = Logger.getLogger(ActionResource.class.getName());

    @GET
    public XHTML browseAll() {
        Service service = getRequestedService();

        XHTML result = getParserXHTML().createDocument();
        Body body = createBodyTemplate(result, getParserXHTML().createXPath(), "Actions");

        body.createChild(ELEMENT.h1).setContent("Actions");
        representActions(body, service);

        return result;
    }

    @GET
    @Path("/{ActionName}")
    public XHTML browse() {
        Action action = getRequestedAction();

        XHTML result = getParserXHTML().createDocument();
        Body body = createBodyTemplate(result, getParserXHTML().createXPath(), action.getName());

        createForm(body, action);

        return result;
    }

    @POST
    @Path("/{ActionName}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response executeAction(MultivaluedMap<String, String> form) {

        ActionInvocation invocation = executeInvocation(form, getRequestedAction());

        MultivaluedMap<String, String> result = new MultivaluedMapImpl();

        if (invocation.getFailure() != null) {
            log.fine("Invocation was unsuccessful, returning server error for: " + invocation.getFailure());
            getConfiguration().getActionProcessor().appendFailure(invocation, result);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        }

        log.fine("Invocation was successful, returning OK response: " + invocation);
        getConfiguration().getActionProcessor().appendOutput(invocation, result);
        return Response.status(Response.Status.OK).entity(result).build();
    }

    @POST
    @Path("/{ActionName}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_XHTML_XML)
    public XHTML executeActionXHTML(MultivaluedMap<String, String> form) {

        ActionInvocation invocation = executeInvocation(form, getRequestedAction());

        Action action = invocation.getAction();
        XHTML result = getParserXHTML().createDocument();
        Body body = createBodyTemplate(result, getParserXHTML().createXPath(), action.getName());

        createForm(body, action);

        XHTMLElement output = body.createChild(ELEMENT.div).setAttribute(ATTR.id, "invocation-output");

        if (invocation.getFailure() != null) {
            log.fine("Invocation was unsuccessful, generating FAILURE message: " + invocation.getFailure());
            getConfiguration().getActionProcessor().appendFailure(invocation, output);
        } else {
            log.fine("Invocation was successful, generating SUCCESS message: " + invocation);
            getConfiguration().getActionProcessor().appendOutput(invocation, output);
        }

        return result;
    }

    protected ActionInvocation executeInvocation(MultivaluedMap<String, String> form, Action action) {
        ActionInvocation invocation;
        try {
            invocation = getConfiguration().getActionProcessor().createInvocation(form, action);
        } catch (InvalidValueException ex) {
            throw new BridgeWebApplicationException(
                    Response.Status.BAD_REQUEST,
                    "Error processing action input form data: " + Exceptions.unwrap(ex)
            );
        }

        ActionCallback actionCallback = new ActionCallback.Default(invocation, getControlPoint());
        log.fine("Executing action after transformation from HTML form: " + invocation);
        actionCallback.run();

        return invocation;
    }

    protected void createForm(XHTMLElement container, Action action) {
        container.createChild(ELEMENT.h1).setContent(action.getName());

        XHTMLElement form = container.createChild(ELEMENT.form)
                .setAttribute(
                        ATTR.action,
                        getUriInfo().getAbsolutePath().getPath()
                                + "?" + SecureHashAuthManager.QUERY_PARAM_AUTH
                                + "=" + getConfiguration().getAuthManager().getLocalCredentials()
                )
                .setAttribute(ATTR.method, "POST")
                .setAttribute(ATTR.id, "invocation-input");

        if (action.hasInputArguments()) {
            for (ActionArgument argument : action.getInputArguments()) {
                XHTMLElement arg = form.createChild(ELEMENT.div);
                arg.createChild(ELEMENT.span).setContent(argument.getName());
                arg.createChild(ELEMENT.input).setAttribute(ATTR.name, argument.getName());
            }
        }

        form.createChild(ELEMENT.input).setAttribute(ATTR.type, "submit");
        container.createChild("hr");
    }


}
