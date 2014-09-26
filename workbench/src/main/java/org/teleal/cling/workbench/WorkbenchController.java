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

import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.shared.MainController;
import org.teleal.cling.support.shared.TextExpandDialog;
import org.teleal.cling.support.shared.TextExpandEvent;
import org.teleal.cling.workbench.bridge.backend.Bridge;
import org.teleal.cling.workbench.browser.BrowserController;
import org.teleal.cling.workbench.control.ActionController;
import org.teleal.cling.workbench.info.DeviceInfosController;
import org.teleal.cling.workbench.monitor.MonitorController;
import org.teleal.cling.workbench.shared.ActionInvocationRequestEvent;
import org.teleal.cling.workbench.shared.ServiceMonitoringRequestEvent;
import org.teleal.cling.workbench.shared.ServiceUseRequestEvent;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.DefaultEvent;
import org.teleal.common.swingfwk.DefaultEventListener;
import org.teleal.common.swingfwk.EventListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.Dimension;


public class WorkbenchController extends MainController {

    // Dependencies
    final private UpnpService upnpService;
    final private Bridge bridge;

    // View
    final private JSplitPane northSouthSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    final private JSplitPane eastWestSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    final private WorkbenchToolbar toolbar;
    final private JPanel browserPanel = new JPanel();
    final private JPanel infoPanel = new JPanel();

    protected WorkbenchController() {
        super(new JFrame(Workbench.APPNAME), new WorkbenchLogCategories());

        upnpService = new UpnpServiceImpl() {
            @Override
            public void shutdown() {
                bridge.stop(true);
                super.shutdown();
            }
        };

        bridge = new Bridge(upnpService);

        registerEventListener(
                TextExpandEvent.class,
                new DefaultEventListener<String>() {
                    public void handleEvent(DefaultEvent<String> e) {
                        new TextExpandDialog(WorkbenchController.this.getView(), e.getPayload());
                    }
                }
        );

        registerEventListener(
                ActionInvocationRequestEvent.class,
                new EventListener<ActionInvocationRequestEvent>() {
                    public void handleEvent(ActionInvocationRequestEvent e) {
                        Application.center(
                                new ActionController(WorkbenchController.this, e.getPayload(), e.getPresetInputValues()).getView(),
                                WorkbenchController.this.getView()
                        ).setVisible(true);
                    }
                }
        );

        registerEventListener(
                ServiceMonitoringRequestEvent.class,
                new DefaultEventListener<Service>() {
                    public void handleEvent(DefaultEvent<Service> e) {
                        Application.center(
                                new MonitorController(WorkbenchController.this, e.getPayload()).getView(),
                                WorkbenchController.this.getView()
                        ).setVisible(true);
                    }
                }
        );

        registerEventListener(
                ServiceUseRequestEvent.class,
                new EventListener<ServiceUseRequestEvent>() {
                    public void handleEvent(ServiceUseRequestEvent e) {
                        e.getControlPointAdapter().start(WorkbenchController.this, WorkbenchController.this.getUpnpService(), e.getPayload());
                    }
                }
        );

        // Toolbar
        toolbar = new WorkbenchToolbar(this);
        
        // Initially visible subcontrollers
        new BrowserController(browserPanel, this);
        new DeviceInfosController(infoPanel, this);

        // Assemble the view
        toolbar.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        browserPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 5));
        browserPanel.setPreferredSize(new Dimension(250, 250));
        browserPanel.setMinimumSize(new Dimension(250, 250));

        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        infoPanel.setPreferredSize(new Dimension(675, 200));
        infoPanel.setMinimumSize(new Dimension(650, 200));

        eastWestSplitPane.setBorder(BorderFactory.createEmptyBorder());
        eastWestSplitPane.setResizeWeight(0);
        eastWestSplitPane.setLeftComponent(browserPanel);
        eastWestSplitPane.setRightComponent(infoPanel);

        northSouthSplitPane.setBorder(BorderFactory.createEmptyBorder());
        northSouthSplitPane.setResizeWeight(0.8);
        northSouthSplitPane.setTopComponent(eastWestSplitPane);
        northSouthSplitPane.setBottomComponent(getLogPanel());

        getView().add(toolbar, BorderLayout.NORTH);
        getView().add(northSouthSplitPane, BorderLayout.CENTER);

        getView().addWindowListener(this);
        getView().setSize(new Dimension(975, 700));
        getView().setMinimumSize(new Dimension(975, 450));
        getView().setResizable(true);
        Application.center(getView());
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public Bridge getBridge() {
        return bridge;
    }

    public void onViewReady() {
        // Immediately search for all devices on network (with MX 1 second)
        upnpService.getControlPoint().search(1);
    }
}
