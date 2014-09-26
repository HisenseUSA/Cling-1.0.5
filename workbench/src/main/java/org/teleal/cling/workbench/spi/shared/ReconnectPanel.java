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

package org.teleal.cling.workbench.spi.shared;

import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.cling.UpnpService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionEvent;


public abstract class ReconnectPanel extends DitheredBackgroundPanel {

    public static String[] ACTION_CONNECT = {"Connect...", "connectToService"};
    public static String[] ACTION_WAKEUP = {"Wake Up...", "wakeUpDevice"};

    final protected JButton connectButton = new JButton(ACTION_CONNECT[0]);
    final protected JButton wakupButton = new JButton(ACTION_WAKEUP[0]);

    public ReconnectPanel(Controller controller, final UpnpService upnpService, final byte[] wakeOnLANBytes) {

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        add(Box.createHorizontalGlue());

        controller.registerAction(
                connectButton,
                ACTION_CONNECT[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        connect();
                    }
                });
        add(connectButton);

        if (wakeOnLANBytes != null) {
            controller.registerAction(
                    wakupButton,
                    ACTION_WAKEUP[1],
                    new DefaultAction() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            upnpService.getRouter().broadcast(wakeOnLANBytes);
                        }
                    });
            add(wakupButton);
        }
        add(Box.createHorizontalGlue());
    }

    protected abstract void connect();
}
