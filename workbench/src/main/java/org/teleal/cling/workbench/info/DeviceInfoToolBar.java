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

import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.workbench.Workbench;
import org.teleal.cling.workbench.spi.ControlPointAdapter;
import org.teleal.cling.workbench.spi.PluginRegistry;
import org.teleal.cling.workbench.shared.ActionInvocationRequestEvent;
import org.teleal.cling.workbench.shared.ServiceMonitoringRequestEvent;
import org.teleal.cling.workbench.shared.ServiceUseRequestEvent;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.Application;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.event.ActionEvent;


public class DeviceInfoToolBar extends JToolBar {

    final protected DeviceInfoController controller;

    // Actions
    public static String[] ACTION_COPY = {"Copy to clipboard", "copyClipboard"};
    public static String[] ACTION_MONITOR = {"Monitor Service", "monitorService"};
    public static String[] ACTION_QUERY = {"Query Variable", "queryVariable"};
    public static String[] ACTION_INVOKE = {"Invoke Action", "requestActionInvocation"};
    public static String[] ACTION_USE = {"Use Service", "useService"};
    public static String[] ACTION_CLOSE = {"Close", "deviceEditorClose"};

    // View
    private final JButton copyButton = new JButton(ACTION_COPY[0], Application.createImageIcon(Workbench.class, "img/16/copyclipboard.png"));
    private final JButton queryButton = new JButton(ACTION_QUERY[0], Application.createImageIcon(Workbench.class, "img/16/querystatevar.png"));
    private final JButton invokeButton = new JButton(ACTION_INVOKE[0], Application.createImageIcon(Workbench.class, "img/16/execute.png"));
    private final JButton useButton = new JButton(ACTION_USE[0], Application.createImageIcon(Workbench.class, "img/16/service.png"));
    private final JButton monitorButton = new JButton(ACTION_MONITOR[0], Application.createImageIcon(Workbench.class, "img/16/monitor.png"));
    private final JButton closeButton = new JButton(ACTION_CLOSE[0], Application.createImageIcon(Workbench.class, "img/16/close.png"));


    public DeviceInfoToolBar(final DeviceInfoController controller) {

        this.controller = controller;

        setFloatable(false);

        useButton.setPreferredSize(new Dimension(500, 25));
        useButton.setFocusable(false);
        add(useButton);
        monitorButton.setPreferredSize(new Dimension(500, 25));
        monitorButton.setFocusable(false);
        add(monitorButton);
        invokeButton.setPreferredSize(new Dimension(500, 25));
        invokeButton.setFocusable(false);
        add(invokeButton);
        queryButton.setPreferredSize(new Dimension(500, 25));
        queryButton.setFocusable(false);
        add(queryButton);
        copyButton.setPreferredSize(new Dimension(500, 25));
        copyButton.setFocusable(false);
        add(copyButton);
        closeButton.setPreferredSize(new Dimension(500, 25));
        closeButton.setFocusable(false);
        add(closeButton);

        controller.registerAction(
                closeButton,
                ACTION_CLOSE[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        controller.close();
                    }
                }
        );

        resetState();

    }

    public void resetState() {
        copyButton.setEnabled(false);
        invokeButton.setEnabled(false);
        queryButton.setEnabled(false);
        useButton.setEnabled(false);
        monitorButton.setEnabled(false);
    }

    public void deviceInfoSelected() {
        resetState();

        final DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) controller.getDeviceTree().getLastSelectedPathComponent();

        if (node == null) return;

        if (node.getUserObject() instanceof InfoItem) {

            controller.registerAction(
                    copyButton,
                    ACTION_COPY[1],
                    new DefaultAction() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            InfoItem infoItem = (InfoItem) node.getUserObject();
                            Application.copyToClipboard(
                                    infoItem.getData() != null ? infoItem.getData().toString() : infoItem.getInfo()
                            );
                        }
                    }
            );
            copyButton.setEnabled(true);

        } else if (node.getUserObject() instanceof Action) {

            controller.registerAction(
                    invokeButton,
                    ACTION_INVOKE[1],
                    new DefaultAction() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            controller.fireEventGlobal(
                                    new ActionInvocationRequestEvent((Action) node.getUserObject())
                            );
                        }
                    }
            );
            invokeButton.setEnabled(true);

        } else if (node.getUserObject() instanceof Service) {

            serviceSelected((Service)node.getUserObject());

        } else if (node.getUserObject() instanceof StateVariable) {

            controller.registerAction(
                    queryButton,
                    ACTION_QUERY[1],
                    new DefaultAction() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            StateVariable stateVar = (StateVariable)node.getUserObject();
                            Action action = stateVar.getService().getQueryStateVariableAction();
                            Object[] inputValues = new Object[1];
                            inputValues[0] = stateVar.getName();
                            controller.fireEventGlobal(
                                    new ActionInvocationRequestEvent(action, inputValues)
                            );
                        }
                    }
            );
            queryButton.setEnabled(true);

        }

        if (node.getParent() != null) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)node.getParent();

            if (parentNode.getUserObject() instanceof Service) {
                serviceSelected((Service)parentNode.getUserObject());
            }

        }
    }

    protected void serviceSelected(final Service service) {

        // We can always monitor (or try to...) a sevice
        controller.registerAction(
                monitorButton,
                ACTION_MONITOR[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        controller.fireEventGlobal(
                                new ServiceMonitoringRequestEvent(service)
                        );
                    }
                }
        );
        monitorButton.setEnabled(true);

        // We might even have a custom control point
        final ControlPointAdapter controlPointAdapter = PluginRegistry.getControlPointAdapter(service);
        if (controlPointAdapter != null) {
            controller.registerAction(
                    useButton,
                    ACTION_USE[1],
                    new DefaultAction() {
                        public void actionPerformed(ActionEvent actionEvent) {
                            controller.fireEventGlobal(
                                    new ServiceUseRequestEvent(service, controlPointAdapter)
                            );
                        }
                    }
            );
            useButton.setEnabled(true);
        }

    }


}

