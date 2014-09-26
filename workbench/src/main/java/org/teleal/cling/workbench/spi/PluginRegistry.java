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

package org.teleal.cling.workbench.spi;

import org.teleal.cling.model.meta.Service;

import java.util.ServiceLoader;


public class PluginRegistry {

    public static ControlPointAdapter getControlPointAdapter(Service service) {
        // TODO: Just take the first, we really should provide a drop-down menu
        for (ControlPointAdapter controlPointAdapter : ServiceLoader.load(ControlPointAdapter.class)) {
            if (controlPointAdapter.getServiceType().implementsVersion(service.getServiceType())) {
                return controlPointAdapter;
            }
        }
        return null;
    }

}
