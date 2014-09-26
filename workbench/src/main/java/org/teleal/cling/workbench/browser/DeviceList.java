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

package org.teleal.cling.workbench.browser;

import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.cling.workbench.Constants;
import org.teleal.cling.workbench.shared.RootDeviceSelectedEvent;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Color;
import java.awt.Component;

public class DeviceList extends JList {

    public DeviceList(final Controller controller, DeviceListModel listModel) {
        super(listModel);

        setLayoutOrientation(JList.VERTICAL);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFocusable(false);
        
        addListSelectionListener(
                new ListSelectionListener() {
                    public void valueChanged(ListSelectionEvent e) {
                        if (!e.getValueIsAdjusting()) {

                            if (getSelectedIndex() != -1) {
                                DeviceItem selected = (DeviceItem) getModel().getElementAt(getSelectedIndex());
                                controller.fireEventGlobal(
                                        new RootDeviceSelectedEvent(selected.getIcon(), selected.getDevice())
                                );
                            }
                        }
                    }
                }
        );

        setCellRenderer(
                new ListCellRenderer() {
                    public Component getListCellRendererComponent(JList list, Object value, int index,
                                                                  boolean isSelected, boolean cellHasFocus) {
                        assert value instanceof DeviceItem;
                        DeviceItem display = (DeviceItem) value;

                        JPanel panel = new JPanel();
                        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                        panel.setBorder(new EmptyBorder(10,10,10,10));

                        JLabel iconLabel = new JLabel(display.getIcon());
                        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                        panel.add(iconLabel);

                        for (String label : display.getLabel()) {
                            JLabel l = new JLabel(label);

                            // First label is larger font
                            if (display.getLabel()[0].equals(label)) {
                                Application.increaseFontSize(l);
                            } else {
                                Application.decreaseFontSize(l);
                            }
                            l.setAlignmentX(Component.CENTER_ALIGNMENT);
                            panel.add(l);
                        }

                        panel.setBackground(Color.WHITE);
                        if (isSelected) {
                            iconLabel.setBorder(new LineBorder(Constants.GREEN_DARK, 4));
                        } else {
                            iconLabel.setBorder(new LineBorder(Color.WHITE, 4));
                        }

                        return panel;
                    }
                }
        );

    }

    @Override
    public DeviceListModel getModel() {
        return (DeviceListModel)super.getModel();
    }
}
