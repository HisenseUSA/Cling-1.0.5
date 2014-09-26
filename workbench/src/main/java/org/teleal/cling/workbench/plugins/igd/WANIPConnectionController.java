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

package org.teleal.cling.workbench.plugins.igd;

import org.teleal.cling.UpnpService;
import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.model.Connection;
import org.teleal.cling.support.igd.callback.GetExternalIP;
import org.teleal.cling.support.igd.callback.GetStatusInfo;
import org.teleal.cling.support.model.PortMapping;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.Event;
import org.teleal.common.swingfwk.EventListener;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class WANIPConnectionController extends AbstractController<JFrame> {

    // Dependencies
    final protected UpnpService upnpService;
    final protected Service service;
    final protected PortMappingEditController editController;

    // View
    final protected ConnectionInfoPanel connectionInfoPanel = new ConnectionInfoPanel();

    final protected JScrollPane portMappingScrollPane;
    final protected PortMappingTable portMappingTable = new PortMappingTable();

    public WANIPConnectionController(Controller parentController, UpnpService upnpService, Service service) {
        super(new JFrame("WAN IP Connection on " + service.getDevice().getRoot().getDetails().getFriendlyName()), parentController);
        this.upnpService = upnpService;
        this.service = service;

        editController = new PortMappingEditController(this, upnpService, service);

        portMappingTable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) return;
                        int index = ((ListSelectionModel) e.getSource()).getMinSelectionIndex();
                        if (index > -1) {
                            fireEvent(
                                    new PortMappingSelectedEvent(portMappingTable.getPortMapping(index))
                            );
                        }
                    }
                }
        );

        portMappingScrollPane = new JScrollPane(portMappingTable);

        connectionInfoPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(connectionInfoPanel, BorderLayout.NORTH);
        mainPanel.add(portMappingScrollPane, BorderLayout.CENTER);
        mainPanel.add(editController.getView(), BorderLayout.SOUTH);

        getView().add(mainPanel);
        getView().setMinimumSize(new Dimension(600, 550));
        getView().setPreferredSize(new Dimension(600, 550));
        getView().pack();

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        WANIPConnectionController.this.dispose();
                    }
                }
        );

        registerEventListener(
                ConnectionDetailsReloadEvent.class,
                new EventListener() {
                    public void handleEvent(Event event) {
                        updateConnectionInfo();
                        updatePortMappings();
                    }
                }
        );

        // Eventing is broken on 4 routers I've tested, so let's just skip that and use action calls
        updateConnectionInfo();
        updatePortMappings();
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public Service getService() {
        return service;
    }

    protected void updateConnectionInfo() {
        getUpnpService().getControlPoint().execute(
                new GetExternalIP(getService()) {
                    @Override
                    protected void success(final String externalIPAddress) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                connectionInfoPanel.updateIP(externalIPAddress);
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        Workbench.APP.log(new LogMessage(
                                Level.INFO,
                                "WANIPConnection ControlPoint",
                                "Can't retrieve external IP: " + defaultMsg
                        ));
                    }
                }
        );

        getUpnpService().getControlPoint().execute(
                new GetStatusInfo(getService()) {
                    @Override
                    protected void success(final Connection.StatusInfo statusInfo) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                connectionInfoPanel.updateStatus(statusInfo);
                            }
                        });
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        Workbench.APP.log(new LogMessage(
                                Level.INFO,
                                "WANIPConnection ControlPoint",
                                "Can't retrieve connection status: " + defaultMsg
                        ));
                    }
                }
        );
    }

    protected void updatePortMappings() {
        // Don't block the EDT
        getUpnpService().getConfiguration().getAsyncProtocolExecutor().execute(new Runnable() {
            public void run() {
                Workbench.APP.log(new LogMessage(
                        Level.INFO,
                        "WANIPConnection ControlPoint",
                        "Updating list of port mappings"
                ));
                final List<PortMapping> mappings = new ArrayList();
                for (int i = 0; i < 65535; i++) { // You can't have more than 65535 port mappings
                    // Synchronous execution! And we stop when we hit a 713 response code because there
                    // is no other way to retrieve all mappings.
                    GetGenericPortMappingCallback invocation = new GetGenericPortMappingCallback(i);
                    invocation.run();

                    if (invocation.isStopRetrieval()) break;

                    if (invocation.getMapping() != null) {
                        mappings.add(invocation.getMapping());
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        portMappingTable.updatePortMappings(mappings.toArray(new PortMapping[mappings.size()]));
                    }
                });
            }
        });
    }

    class GetGenericPortMappingCallback extends ActionCallback {

        int index;
        PortMapping mapping;
        boolean stopRetrieval = false;

        GetGenericPortMappingCallback(int index) {
            super(new ActionInvocation(getService().getAction("GetGenericPortMappingEntry")), getUpnpService().getControlPoint());
            this.index = index;
            getActionInvocation().setInput("NewPortMappingIndex", new UnsignedIntegerTwoBytes(index));
        }

        public PortMapping getMapping() {
            return mapping;
        }

        @Override
        public void success(ActionInvocation invocation) {
            mapping = new PortMapping(invocation.getOutputMap());
        }

        @Override
        public void failure(ActionInvocation invocation,
                            UpnpResponse operation,
                            String defaultMsg) {

            stopRetrieval = true;

            if (invocation.getFailure().getErrorCode() == 713) {
                // This is the _only_ way how we can know that we have retrieved an almost-up-to-date
                // list of all port mappings! Yes, the designer of this API was and probably still is
                // a moron.
                Workbench.APP.log(new LogMessage(
                        Level.INFO,
                        "WANIPConnection ControlPoint",
                        "Retrieved all port mappings: " + index
                ));
            } else {
                Workbench.APP.log(new LogMessage(
                        Level.WARNING,
                        "WANIPConnection ControlPoint",
                        "Error retrieving port mapping index '" + index + "': " + defaultMsg
                ));
            }
        }

        public boolean isStopRetrieval() {
            return stopRetrieval;
        }
    }

}
