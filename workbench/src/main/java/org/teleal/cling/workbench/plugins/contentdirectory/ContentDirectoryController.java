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

package org.teleal.cling.workbench.plugins.contentdirectory;

import org.teleal.cling.model.meta.Service;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;
import org.teleal.cling.UpnpService;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 */
public class ContentDirectoryController extends AbstractController<JFrame> {

    // Dependencies
    final protected UpnpService upnpService;
    final protected Service service;

    // View
    final protected JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    final protected CustomContentTree containerTree;
    final protected JScrollPane containerTreePane;
    final protected JPanel mainPanel;

    public ContentDirectoryController(Controller parentController, UpnpService upnpService, Service service) {
        super(new JFrame("Content Directory on " + service.getDevice().getDetails().getFriendlyName()), parentController);
        this.upnpService = upnpService;
        this.service = service;

        containerTree = new CustomContentTree(getUpnpService().getControlPoint(), service, this, 1);
        containerTree.setBorder(new EmptyBorder(5, 5,5 ,5));
        containerTree.setFocusable(false);

        containerTreePane = new JScrollPane(containerTree);
        containerTreePane.setMinimumSize(new Dimension(180, 200));

        mainPanel = new DetailController(this).getView();
        mainPanel.setPreferredSize(new Dimension(300, 100));

        splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        splitPane.setLeftComponent(containerTreePane);
        splitPane.setRightComponent(mainPanel);
        splitPane.setResizeWeight(0.65);

        getView().setLayout(new BorderLayout());
        getView().setPreferredSize(new Dimension(700, 500));
        getView().setMinimumSize(new Dimension(500,250));

        getView().add(splitPane, BorderLayout.CENTER);
        getView().pack();

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        ContentDirectoryController.this.dispose();
                    }
                }
        );
    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public Service getService() {
        return service;
    }

}
