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

package org.teleal.cling.workbench;

import org.teleal.cling.workbench.bridge.ConfigureBridgeButton;
import org.teleal.cling.workbench.plugins.binarylight.device.CreateDemoButton;
import org.teleal.common.swingfwk.Controller;

import javax.swing.JToolBar;

/**
 * @author Christian Bauer
 */
public class WorkbenchToolbar extends JToolBar {

    final protected CreateDemoButton demoButton;
    final protected ConfigureBridgeButton bridgeButton;

    public WorkbenchToolbar(Controller controller) {
        setFloatable(false);

        demoButton = new CreateDemoButton(controller);
        add(demoButton);

        bridgeButton = new ConfigureBridgeButton(controller);
        add(bridgeButton);

    }
}
