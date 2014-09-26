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

import org.teleal.cling.model.Namespace;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.model.meta.ActionArgument;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.RemoteService;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.QueryStateVariableAction;
import org.teleal.cling.model.meta.RemoteDevice;
import org.teleal.cling.model.meta.StateVariable;
import org.teleal.cling.model.meta.StateVariableAllowedValueRange;
import org.teleal.cling.model.types.DLNADoc;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.util.HexBin;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.Component;
import java.net.URI;


public class DeviceTree extends JTree {

    protected Namespace namespace;

    public DeviceTree(Namespace namespace, Device device, ImageIcon deviceIcon) {
        this.namespace = namespace;

        TreeModel model = new DefaultTreeModel(
                createNodes(new DefaultMutableTreeNode("ROOT"), device)
        );
        setModel(model);
        setRootVisible(false);

        setCellRenderer(new DeviceTreeRenderer(deviceIcon));
        setShowsRootHandles(true);
        putClientProperty("JTree.lineStyle", "None");
        setRowHeight(26);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    public DefaultMutableTreeNode createNodes(DefaultMutableTreeNode currentNode, Device device) {

        DefaultMutableTreeNode deviceNode = new DefaultMutableTreeNode(device);
        currentNode.add(deviceNode);

        addIfNotNull(deviceNode, "UDN: ", device.getIdentity().getUdn());
        addIfNotNull(deviceNode, "Device Type: ", device.getType().toString());
        if (device.getDetails().getDlnaDocs() != null) {
            for (DLNADoc dlnaDoc : device.getDetails().getDlnaDocs()) {
                addIfNotNull(deviceNode, "DLNA Doc: ", dlnaDoc);
            }
        }
        addIfNotNull(deviceNode, "DLNA Caps: ", device.getDetails().getDlnaCaps());

        if (device instanceof RemoteDevice) {
            addIfNotNull(deviceNode, "Descriptor URL: ", ((RemoteDevice) device).getIdentity().getDescriptorURL(), true);
        } else if (device instanceof LocalDevice) {
            addIfNotNull(deviceNode, "Descriptor URI: ", namespace.getDescriptorPath(device));
        }

        addIfNotNull(deviceNode, "Manufacturer: ", device.getDetails().getManufacturerDetails().getManufacturer());
        addIfNotNull(deviceNode, "Manufacturer URL/URI: ", device.getDetails().getManufacturerDetails().getManufacturerURI(), device);
        addIfNotNull(deviceNode, "Model Name: ", device.getDetails().getModelDetails().getModelName());
        addIfNotNull(deviceNode, "Model #: ", device.getDetails().getModelDetails().getModelNumber());
        addIfNotNull(deviceNode, "Model Description: ", device.getDetails().getModelDetails().getModelDescription());
        addIfNotNull(deviceNode, "Model URL/URI: ", device.getDetails().getModelDetails().getModelURI(), device);
        addIfNotNull(deviceNode, "Serial #: ", device.getDetails().getSerialNumber());
        addIfNotNull(deviceNode, "Universal Product Code: ", device.getDetails().getUpc());
        addIfNotNull(deviceNode, "Presentation URI: ", device.getDetails().getPresentationURI(), device);

        if (device instanceof RemoteDevice && ((RemoteDevice) device).getIdentity().getInterfaceMacAddress() != null)
            addIfNotNull(deviceNode, "MAC Ethernet Address: ", HexBin.bytesToString(((RemoteDevice) device).getIdentity().getInterfaceMacAddress(), ":"));

        if (device.hasIcons()) {
            for (Icon icon : device.getIcons()) {
                deviceNode.add(new DefaultMutableTreeNode(icon));
            }
        }

        if (device.hasServices()) {

            for (Service service : device.getServices()) {
                DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(service);
                deviceNode.add(serviceNode);

                addIfNotNull(serviceNode, "Service Type: ", service.getServiceType().toString());
                addIfNotNull(serviceNode, "Service ID: ", service.getServiceId().toString());

                if (service instanceof LocalService) {
                    LocalService ls = (LocalService) service;
                    addIfNotNull(serviceNode, "Descriptor URI: ", namespace.getDescriptorPath(ls));
                    addIfNotNull(serviceNode, "Control URI: ", namespace.getControlPath(ls));
                    addIfNotNull(serviceNode, "Event Subscription URI: ", namespace.getEventSubscriptionPath(ls));
                    addIfNotNull(serviceNode, "Local Event Callback URI: ", namespace.getEventCallbackPath(ls));
                } else if (service instanceof RemoteService) {
                    RemoteService rs = (RemoteService) service;
                    addIfNotNull(serviceNode, "Descriptor URL: ", rs.getDevice().normalizeURI(rs.getDescriptorURI()), true);
                    addIfNotNull(serviceNode, "Control URL: ", rs.getDevice().normalizeURI(rs.getControlURI()), true);
                    addIfNotNull(serviceNode, "Event Subscription URL: ", rs.getDevice().normalizeURI(rs.getEventSubscriptionURI()), true);
                }

                for (Action action : service.getActions()) {

                    if (action instanceof QueryStateVariableAction) continue; // Skip that

                    DefaultMutableTreeNode actionNode = new DefaultMutableTreeNode(action);
                    serviceNode.add(actionNode);

                    int i = 0;
                    for (ActionArgument actionArgument : action.getArguments()) {
                        DefaultMutableTreeNode actionArgumentNode = new DefaultMutableTreeNode(actionArgument);
                        actionNode.add(actionArgumentNode);

                        addIfNotNull(actionArgumentNode, i++ + " Direction: ", actionArgument.getDirection());
                        addIfNotNull(actionArgumentNode, "Related State Variable: ", actionArgument.getRelatedStateVariableName());
                        addIfNotNull(actionArgumentNode, "Datatype: ", actionArgument.getDatatype().getDisplayString());
                    }
                }

                for (StateVariable stateVariable : service.getStateVariables()) {
                    DefaultMutableTreeNode stateVariableNode = new DefaultMutableTreeNode(stateVariable);
                    serviceNode.add(stateVariableNode);

                    addIfNotNull(stateVariableNode, "Datatype: ", stateVariable.getTypeDetails().getDatatype().getDisplayString());
                    addIfNotNull(stateVariableNode, "Default Value: ", stateVariable.getTypeDetails().getDefaultValue());

                    if (stateVariable.getTypeDetails().getAllowedValues() != null) {
                        for (String allowedValue : stateVariable.getTypeDetails().getAllowedValues()) {
                            addIfNotNull(stateVariableNode, "Allowed Value: " , allowedValue);
                        }
                    }

                    if (stateVariable.getTypeDetails().getAllowedValueRange() != null) {
                        StateVariableAllowedValueRange range = stateVariable.getTypeDetails().getAllowedValueRange();
                        addIfNotNull(stateVariableNode, "Allowed Value Range Minimum: ", range.getMinimum());
                        addIfNotNull(stateVariableNode, "Allowed Value Range Maximum: ", range.getMaximum());
                        addIfNotNull(stateVariableNode, "Allowed Value Range Step: ", range.getStep());
                    }

                }
            }
        }

        if (device.hasEmbeddedDevices()) {
            for (Device embedded : device.getEmbeddedDevices()) {
                createNodes(deviceNode, embedded);
            }
        }

        return currentNode;
    }

    protected void addIfNotNull(DefaultMutableTreeNode parent, String info, URI uri, Device device) {
        if (device instanceof RemoteDevice) {
            addIfNotNull(parent, info, uri != null ? ((RemoteDevice)device).normalizeURI(uri) : null, true);
        } else if (device instanceof LocalDevice) {
            addIfNotNull(parent, info, uri, false);
        }
    }

    protected void addIfNotNull(DefaultMutableTreeNode parent, String info, Object data) {
        addIfNotNull(parent, info, data, false);
    }

    protected void addIfNotNull(DefaultMutableTreeNode parent, String info, Object data, boolean isUrl) {
        if (data != null) {
            parent.add(new DefaultMutableTreeNode(
               new InfoItem(info, data, isUrl)
            ));
        }
    }

    private class DeviceTreeRenderer extends DefaultTreeCellRenderer {

        private ImageIcon rootDeviceIcon;

        private DeviceTreeRenderer(ImageIcon rootDeviceIcon) {
            this.rootDeviceIcon = rootDeviceIcon;
        }

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean sel,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            if (node.getUserObject() instanceof InfoItem) {
                setToolTipText(null);
                setIcon(Application.createImageIcon(Workbench.class, "img/24/info.png"));
            }

            if (node.getUserObject() instanceof Device) {
                Device nodeDevice = (Device) node.getUserObject();

                if (nodeDevice.isRoot()) {
                    if (rootDeviceIcon != null) {
                        setIcon(new ImageIcon(rootDeviceIcon.getImage().getScaledInstance(24, 24, java.awt.Image.SCALE_SMOOTH)));
                    } else {
                        setIcon(Application.createImageIcon(Workbench.class, "img/24/device.png"));
                    }
                } else {
                    setIcon(Application.createImageIcon(Workbench.class, "img/24/device_embedded.png"));
                }

                setToolTipText(
                        nodeDevice.getDisplayString()
                                + " (UPnP Version: " + nodeDevice.getVersion().getMajor() + "." + nodeDevice.getVersion().getMinor() + ")"
                );
                setText(nodeDevice.getDetails().getFriendlyName());
            }

            if (node.getUserObject() instanceof Icon) {
                // TODO: Can't copy the URL to clipboard...
                Icon nodeIcon = (Icon) node.getUserObject();

                String uri = nodeIcon.getDevice() instanceof RemoteDevice
                        ? ((RemoteDevice)nodeIcon.getDevice()).normalizeURI(nodeIcon.getUri()).toString()
                        : nodeIcon.getUri().toString();

                setIcon(Application.createImageIcon(Workbench.class, "img/24/device_icon.png"));
                setToolTipText(uri);
                setText(uri
                        + " (" + nodeIcon.getMimeType()
                        + " " + nodeIcon.getWidth()
                        + "x" + nodeIcon.getHeight() + ")");
            }

            if (node.getUserObject() instanceof Service) {
                Service serviceNode = (Service) node.getUserObject();

                setIcon(Application.createImageIcon(Workbench.class, "img/24/service.png"));
                setToolTipText(serviceNode.getServiceId().toString());
                setText(serviceNode.getServiceType().getType());
            }

            if (node.getUserObject() instanceof Action) {
                Action nodeAction = (Action) node.getUserObject();

                setIcon(Application.createImageIcon(Workbench.class, "img/24/action.png"));
                int numOfArguments = nodeAction.getArguments().length;
                setToolTipText(numOfArguments + " argument" + (numOfArguments > 1 ? "s" : ""));
                setText(nodeAction.getName());
            }

            if (node.getUserObject() instanceof ActionArgument) {
                ActionArgument nodeActionArgument = (ActionArgument) node.getUserObject();

                if (nodeActionArgument.getDirection().equals(ActionArgument.Direction.IN)) {
                    setIcon(Application.createImageIcon(Workbench.class, "img/24/argument_in.png"));
                } else {
                    setIcon(Application.createImageIcon(Workbench.class, "img/24/argument_out.png"));
                }
                setToolTipText(nodeActionArgument.getRelatedStateVariableName() + ", " + nodeActionArgument.getDatatype().getDisplayString());
                setText(nodeActionArgument.getName());
            }

            if (node.getUserObject() instanceof StateVariable) {
                StateVariable nodeStateVariable = (StateVariable) node.getUserObject();

                setIcon(Application.createImageIcon(Workbench.class, "img/24/statevariable.png"));
                setToolTipText(nodeStateVariable.getTypeDetails().getDatatype().getDisplayString());
                setText(nodeStateVariable.getName() + (nodeStateVariable.getEventDetails().isSendEvents() ? " (Sends Events)" : ""));
            }

            return this;
        }
    }
}
