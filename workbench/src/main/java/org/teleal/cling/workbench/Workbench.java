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

import org.teleal.cling.support.shared.AWTExceptionHandler;
import org.teleal.cling.support.shared.PlatformApple;
import org.teleal.common.util.OS;

import javax.swing.SwingUtilities;

public class Workbench {

    public static final String APPNAME = "Cling Workbench";
    public static final WorkbenchController APP;

    static {
        WorkbenchController app = null;
        try {
            app = new WorkbenchController();
        } catch (Throwable t) {
            new AWTExceptionHandler().handle(t);
        }
        APP = app;
    }

    public static void main(String[] args) throws Exception {

        if (OS.checkForMac())
            PlatformApple.setup(APP, APPNAME);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                APP.getView().setVisible(true);
                APP.onViewReady();
            }
        });
    }

}
