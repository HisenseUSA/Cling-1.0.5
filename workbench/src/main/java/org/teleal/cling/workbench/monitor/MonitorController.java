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

package org.teleal.cling.workbench.monitor;

import org.teleal.cling.model.meta.Service;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;


public class MonitorController extends AbstractController<JDialog> {

    // Actions
    public static String[] ACTION_START_MONITORING = {"Start Monitoring", "startMonitoring"};
    public static String[] ACTION_STOP_MONITORING = {"Stop Monitoring", "stopMonitoring"};

    // Model
    final private MonitorSubscriptionCallback callback;

    // View
    final private JToolBar monitoringToolBar = new JToolBar();
    private final JButton startButton =
            new JButton(ACTION_START_MONITORING[0], Application.createImageIcon(Workbench.class, "img/16/run.png"));
    private final JButton stopButton =
            new JButton(ACTION_STOP_MONITORING[0], Application.createImageIcon(Workbench.class, "img/16/stop.png"));

    final private JScrollPane stateVariablesScrollPane;
    final private StateVariableTable stateVariablesTable;

    public MonitorController(Controller parentController, Service service) {
        super(new JDialog(
                Workbench.APP.getView(),
                "Monitoring Service: " + service.getServiceType().toFriendlyString()),
                parentController
        );

        callback = new MonitorSubscriptionCallback(service, this);

        // Register actions
        registerAction(
                startButton,
                ACTION_START_MONITORING[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Workbench.APP.log(new LogMessage("Monitor Controller", "Subscribing monitor to: " + callback.getService()));
                        Workbench.APP.getUpnpService().getControlPoint().execute(callback);
                    }
                }
        );

        registerAction(
                stopButton,
                ACTION_STOP_MONITORING[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        Workbench.APP.log(new LogMessage("Monitor Controller", "Unsubscribing from: " + callback.getService()));
                        callback.end();
                    }
                }
        );

        // Assemble the view
        monitoringToolBar.setMargin(new Insets(5, 0, 5, 0));
        monitoringToolBar.setFloatable(false);
        stopButton.setEnabled(false);
        startButton.setPreferredSize(new Dimension(5000, 25));
        stopButton.setPreferredSize(new Dimension(5000, 25));
        monitoringToolBar.add(startButton);
        monitoringToolBar.add(stopButton);

        stateVariablesTable = new StateVariableTable(null);
        stateVariablesScrollPane = new JScrollPane(stateVariablesTable);


        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(monitoringToolBar, BorderLayout.NORTH);
        mainPanel.add(stateVariablesScrollPane, BorderLayout.CENTER);

        getView().setSize(new Dimension(450, 300));
        getView().setResizable(true);
        getView().setMinimumSize(new Dimension(300, 150));
        getView().add(mainPanel);

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        callback.end(); // Don't forget that!
                        MonitorController.this.dispose();
                    }
                }
        );
    }

    public StateVariableTable getStateVariablesTable() {
        return stateVariablesTable;
    }

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getStopButton() {
        return stopButton;
    }
}