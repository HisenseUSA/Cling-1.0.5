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

package org.teleal.cling.workbench.control;

import org.teleal.cling.controlpoint.ActionCallback;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.meta.Action;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.Controller;
import org.teleal.common.swingfwk.DefaultAction;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class ActionController extends AbstractController<JDialog> {

    // Actions
    public static String[] ACTION_INVOKE = {"Invoke", "invokeAction"};

    // Model
    final private Action action;

    // View
    final private JToolBar invocationToolBar = new JToolBar();
    private final JButton invokeActionButton =
            new JButton(ACTION_INVOKE[0], Application.createImageIcon(Workbench.class, "img/16/execute.png"));

    final private JScrollPane inputArgumentsScrollPane;
    final private ActionArgumentTable inputArgumentsTable;

    final private JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    final private JScrollPane outputArgumentsScrollPane;
    final private ActionArgumentTable outputArgumentsTable;

    public ActionController(Controller parentController, Action action, Object... presetInputValues) {
        super(new JDialog(Workbench.APP.getView(), "Invoking Action: " + action.getName()), parentController);

        this.action = action;

        /* Well, this is only used for QueryStateVariable UI
        // TODO: This is a bit fishy, especially the exception handling
        ActionInvocationValues inputValues = action.getInputInvocationValues();
        for (Object presetInputValue : presetInputValues) {
            try {
                inputValues.addValue(presetInputValue);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    */

        // Register actions
        registerAction(
                invokeActionButton,
                ACTION_INVOKE[1],
                new DefaultAction() {
                    public void actionPerformed(ActionEvent actionEvent) {

                        // Commit the currently typed value
                        if (inputArgumentsTable.getCellEditor() != null)
                            inputArgumentsTable.getCellEditor().stopCellEditing();

                        final ActionInvocation actionInvocation =
                                new ActionInvocation(
                                        ActionController.this.action,
                                        inputArgumentsTable.getArgumentValuesModel().getValues()
                                );

                        // Starts background thread
                        Workbench.APP.log(new LogMessage(
                                "Action Invocation",
                                "Executing action: " + actionInvocation.getAction().getName()
                        ));
                        ActionCallback actionCallback =
                                new ControlActionCallback(actionInvocation, ActionController.this);
                        Workbench.APP.getUpnpService().getControlPoint().execute(actionCallback);
                    }
                }
        );

        // Assemble the view
        invocationToolBar.setMargin(new Insets(5, 0, 5, 0));
        invocationToolBar.setFloatable(false);
        invokeActionButton.setPreferredSize(new Dimension(5000, 25));
        invocationToolBar.add(invokeActionButton);

        inputArgumentsTable = new ActionArgumentTable(this, action, true);
        outputArgumentsTable = new ActionArgumentTable(this, action, false);

        inputArgumentsScrollPane = new JScrollPane(inputArgumentsTable);
        outputArgumentsScrollPane = new JScrollPane(outputArgumentsTable);

        // Calculate an optimal size based in input/output arguments
        getView().setSize(new Dimension(450, (action.getArguments().length *40) + 120));
        getView().setMinimumSize(new Dimension(300,150));
        getView().setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(invocationToolBar, BorderLayout.NORTH);

        if (action.hasInputArguments() && action.hasOutputArguments()) {
            splitPane.setTopComponent(inputArgumentsScrollPane);
            splitPane.setBottomComponent(outputArgumentsScrollPane);
            splitPane.setResizeWeight(0.5);
            mainPanel.add(splitPane, BorderLayout.CENTER);
        } else if (action.hasInputArguments()) {
            mainPanel.add(inputArgumentsScrollPane, BorderLayout.CENTER);
        } else if (action.hasOutputArguments()) {
            mainPanel.add(outputArgumentsScrollPane, BorderLayout.CENTER);
        }

        getView().add(mainPanel);

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        ActionController.this.dispose();
                    }
                }
        );
    }

    public Action getAction() {
        return action;
    }

    public ActionArgumentTable getOutputArgumentsTable() {
        return outputArgumentsTable;
    }
}
