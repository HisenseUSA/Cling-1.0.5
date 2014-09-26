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
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.model.PortMapping;
import org.teleal.cling.support.igd.callback.PortMappingAdd;
import org.teleal.cling.support.igd.callback.PortMappingDelete;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.DefaultEvent;
import org.teleal.common.swingfwk.DefaultEventListener;
import org.teleal.common.swingfwk.Form;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class PortMappingEditController extends AbstractController<JPanel> {

    // Actions
    public static String[] ACTION_ADD = {"Add Port Mapping", "addMapping"};
    public static String[] ACTION_DELETE = {"Delete Port Mapping", "deleteMapping"};
    public static String[] ACTION_RELOAD = {"Reload", "reload"};

    final protected JPanel formPanel = new JPanel(new GridBagLayout());

    final protected JCheckBox enabledField = new JCheckBox();
    final protected JTextField leaseDurationField = new JTextField();
    final protected JTextField remoteHostField = new JTextField();
    final protected JTextField externalPortField = new JTextField();
    final protected JComboBox protocolField = new JComboBox(PortMapping.Protocol.values());
    final protected JTextField internalClientField = new JTextField();
    final protected JTextField internalPortField = new JTextField();
    final protected JTextField descriptionField = new JTextField();

    final protected JToolBar portMappingToolBar = new JToolBar();
    final protected JButton addButton =
            new JButton(ACTION_ADD[0], Application.createImageIcon(Workbench.class, "img/24/add.png", ACTION_ADD[0]));
    final protected JButton deleteButton =
            new JButton(ACTION_DELETE[0], Application.createImageIcon(Workbench.class, "img/24/delete.png", ACTION_DELETE[0]));
    final protected JButton reloadButton =
            new JButton(ACTION_RELOAD[0], Application.createImageIcon(Workbench.class, "img/24/reload.png", ACTION_RELOAD[0]));

    // Dependencies
    final protected UpnpService upnpService;
    final protected Service service;

    public PortMappingEditController(Controller parentController, UpnpService upnpService, Service service) {
        super(new JPanel(new BorderLayout()), parentController);

        this.upnpService = upnpService;
        this.service = service;

        registerEventListener(
                PortMappingSelectedEvent.class,
                new DefaultEventListener<PortMapping>() {
                    public void handleEvent(DefaultEvent<PortMapping> e) {
                        enabledField.setSelected(e.getPayload().isEnabled());
                        leaseDurationField.setText(e.getPayload().getLeaseDurationSeconds().getValue().toString());
                        remoteHostField.setText(e.getPayload().getRemoteHost());
                        externalPortField.setText(e.getPayload().getExternalPort().getValue().toString());
                        protocolField.setSelectedItem(e.getPayload().getProtocol());
                        internalClientField.setText(e.getPayload().getInternalClient());
                        internalPortField.setText(e.getPayload().getInternalPort().getValue().toString());
                        descriptionField.setText(e.getPayload().getDescription());
                    }
                }
        );

        registerAction(
                addButton,
                ACTION_ADD[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        final PortMapping mapping = getPortMapping();
                        if (mapping != null)
                            addPortMapping(mapping);
                    }
                }
        );

        registerAction(
                deleteButton,
                ACTION_DELETE[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        final PortMapping mapping = getPortMapping();
                        if (mapping != null)
                            deletePortMapping(mapping);
                    }
                }
        );

        registerAction(
                reloadButton,
                ACTION_RELOAD[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        getParentController().fireEvent(new ConnectionDetailsReloadEvent());
                    }
                }
        );

        Form form = new Form(3);
        setLabelAndField(form, "Enabled:", enabledField);
        enabledField.setSelected(true);
        setLabelAndField(form, "Lease Duration (Seconds):", leaseDurationField);
        leaseDurationField.setText("0");
        setLabelAndField(form, "WAN Host:", remoteHostField);
        remoteHostField.setText("-");
        setLabelAndField(form, "External Port:", externalPortField);
        setLabelAndField(form, "Protocol:", protocolField);
        setLabelAndField(form, "LAN Host:", internalClientField);
        setLabelAndField(form, "Internal Port:", internalPortField);
        setLabelAndField(form, "Description:", descriptionField);

        portMappingToolBar.setFloatable(false);
        portMappingToolBar.add(reloadButton);
        portMappingToolBar.addSeparator();
        portMappingToolBar.add(addButton);
        portMappingToolBar.add(deleteButton);

        getView().add(formPanel, BorderLayout.CENTER);
        getView().add(portMappingToolBar, BorderLayout.SOUTH);

    }

    protected void setLabelAndField(Form form, String l, Component field) {
        JLabel label = new JLabel(l);
        label.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        form.addLabel(label, formPanel);
        form.addLastField(field, formPanel);
    }

    protected PortMapping getPortMapping() {

        try {
            PortMapping pm = new PortMapping();
            pm.setEnabled(enabledField.isSelected());
            pm.setLeaseDurationSeconds(new UnsignedIntegerFourBytes(leaseDurationField.getText()));
            pm.setRemoteHost(remoteHostField.getText());
            pm.setExternalPort(new UnsignedIntegerTwoBytes(externalPortField.getText()));
            pm.setProtocol((PortMapping.Protocol) protocolField.getSelectedItem());
            pm.setInternalClient(internalClientField.getText());
            pm.setInternalPort(new UnsignedIntegerTwoBytes(internalPortField.getText()));
            pm.setDescription(descriptionField.getText());

            return pm;
        } catch (Exception ex) {
            Workbench.APP.log(new LogMessage(
                    Level.INFO,
                    "WANIPConnection ControlPoint",
                    "Error in port mapping form data: " + ex
            ));
        }

        return null;
    }

    protected void deletePortMapping(final PortMapping mapping) {
        upnpService.getControlPoint().execute(
                new PortMappingDelete(service, mapping) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        Workbench.APP.log(new LogMessage(
                                Level.INFO,
                                "WANIPConnection ControlPoint",
                                "Removed port mapping " + mapping.getProtocol() + "/" + mapping.getExternalPort()
                        ));
                        getParentController().fireEvent(new ConnectionDetailsReloadEvent());
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        Workbench.APP.log(new LogMessage(
                                Level.WARNING,
                                "WANIPConnection ControlPoint",
                                "Port mapping removal failed: " + defaultMsg
                        ));
                    }
                }
        );
    }

    protected void addPortMapping(final PortMapping mapping) {
        upnpService.getControlPoint().execute(
                new PortMappingAdd(service, mapping) {
                    @Override
                    public void success(ActionInvocation invocation) {
                        Workbench.APP.log(new LogMessage(
                                Level.INFO,
                                "WANIPConnection ControlPoint",
                                "Added port mapping " + mapping.getProtocol() + "/" + mapping.getExternalPort()
                        ));
                        getParentController().fireEvent(new ConnectionDetailsReloadEvent());
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        Workbench.APP.log(new LogMessage(
                                Level.WARNING,
                                "WANIPConnection ControlPoint",
                                "Port mapping addition failed: " + defaultMsg
                        ));
                    }
                }
        );
    }

}
