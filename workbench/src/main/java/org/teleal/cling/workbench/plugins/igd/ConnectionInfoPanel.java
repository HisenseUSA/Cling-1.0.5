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

import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.support.model.Connection;
import org.teleal.common.swingfwk.Form;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagLayout;

/**
 * @author Christian Bauer
 */
public class ConnectionInfoPanel extends JPanel {

    final protected JTextField statusField = new JTextField();
    final protected JTextField uptimeField = new JTextField();
    final protected JTextField lastErrorField = new JTextField();
    final protected JTextField ipField = new JTextField();

    public ConnectionInfoPanel() {
        super(new GridBagLayout());

        statusField.setEditable(false);
        uptimeField.setEditable(false);
        lastErrorField.setEditable(false);
        ipField.setEditable(false);

        Form form = new Form(3);
        setLabelAndTextField(form, "Connection Status:", statusField);
        setLabelAndTextField(form, "Connection Uptime:", uptimeField);
        setLabelAndTextField(form, "Last Error:", lastErrorField);
        setLabelAndTextField(form, "External IP Address:", ipField);
    }

    public void updateIP(String ip) {
        ipField.setText(ip);
    }

    public void updateStatus(Connection.StatusInfo statusInfo) {
        statusField.setText(statusInfo.getStatus().name());
        uptimeField.setText(ModelUtil.toTimeString(statusInfo.getUptimeSeconds()));
        lastErrorField.setText(statusInfo.getLastError().name());
    }

    protected void setLabelAndTextField(Form form, String l, Component field) {
        JLabel label = new JLabel(l);
        label.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        form.addLabel(label, this);
        form.addLastField(field, this);
    }

}
