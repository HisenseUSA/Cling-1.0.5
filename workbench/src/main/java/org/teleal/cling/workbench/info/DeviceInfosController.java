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

package org.teleal.cling.workbench.info;

import org.teleal.cling.workbench.shared.DeviceInfoSelectionChangedEvent;
import org.teleal.cling.workbench.shared.RootDeviceSelectedEvent;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.EventListener;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.GridLayout;


public class DeviceInfosController extends AbstractController<JPanel> {

    // View
    private final JTabbedPane tabs = new JTabbedPane();

    public DeviceInfosController(final JPanel view, Controller parentController) {
        super(view, parentController);

        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabs.addChangeListener(new ChangeListener() {

            // This method is called whenever the selected tab changes
            public void stateChanged(ChangeEvent evt) {
                DeviceInfo selectedTab = (DeviceInfo) tabs.getSelectedComponent();
                fireEventGlobal(
                        new DeviceInfoSelectionChangedEvent(selectedTab != null ? selectedTab.getDevice() : null)
                );
            }
        });

        registerEventListener(
                RootDeviceSelectedEvent.class,
                new EventListener<RootDeviceSelectedEvent>() {
                    public void handleEvent(RootDeviceSelectedEvent e) {

                        DeviceInfo tab = new DeviceInfo(tabs, e.getPayload());

                        if (tabs.indexOfComponent(tab) != -1) {
                            tabs.setSelectedIndex(tabs.indexOfComponent(tab));
                        } else {
                            tab.openTab(DeviceInfosController.this, e.getIcon());
                            tabs.addTab(tab.getTitle(), tab);
                            tabs.setSelectedComponent(tab);
                        }
                    }
                }
        );

        view.setLayout(new GridLayout(1, 1)); // Makes the tabs magically auto-fit in the parent container
        view.add(tabs);
    }


}
