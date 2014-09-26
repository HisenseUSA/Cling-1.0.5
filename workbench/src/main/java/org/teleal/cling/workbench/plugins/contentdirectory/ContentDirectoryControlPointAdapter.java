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

package org.teleal.cling.workbench.plugins.contentdirectory;

import org.teleal.cling.model.meta.Service;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.cling.UpnpService;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.workbench.spi.ControlPointAdapter;

import javax.swing.JFrame;

/**
 *
 */
public class ContentDirectoryControlPointAdapter implements ControlPointAdapter {

    public ServiceType getServiceType() {
        return new UDAServiceType("ContentDirectory", 1);
    }

    public void start(Controller controller, UpnpService upnpService, Service service) {
        JFrame view = new ContentDirectoryController(controller, upnpService, service).getView();
        Application.center(view);
        view.setVisible(true);
    }
}
