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
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.logging.LogMessage;
import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.support.contentdirectory.ui.ContentTree;
import org.teleal.cling.support.contentdirectory.ui.ContentTreeCellRenderer;
import org.teleal.cling.workbench.Workbench;

import javax.swing.Icon;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.util.logging.Level;

/**
 * Customizes the tree icons and how status updates are displayed.
 * <p>
 * Also fires extra events when a tree node is selected and when the
 * items children of a container are loaded.
 * </p>
 *
 * @author Christian Bauer
 */
public class CustomContentTree extends ContentTree {

    final protected ContentDirectoryController controller;

    public CustomContentTree(ControlPoint controlPoint, Service service,
                             ContentDirectoryController controller, int toggleClickCount) {

        super(controlPoint, service);

        this.controller = controller;

        // Well, we can disable that
        setRootVisible(false);

        // Large icons make it easier on touchscreens (Asus EEE Top is what I use)
        setRowHeight(36);
        Application.increaseFontSize(this);

        setToggleClickCount(toggleClickCount);

        addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
                if (node == null) return;

                if (node.getUserObject() instanceof Container) {

                    CustomContentTree.this.controller.fireEvent(
                            new ContainerSelectedEvent((Container) node.getUserObject())
                    );

                } else if (node.getUserObject() instanceof Item) {

                    CustomContentTree.this.controller.fireEvent(
                            new ItemSelectedEvent((Item) node.getUserObject())
                    );
                }
            }
        });
    }

    @Override
    protected DefaultTreeCellRenderer createContainerTreeCellRenderer() {
        return new ContentTreeCellRenderer() {

            @Override
            protected Icon getContainerOpenIcon() {
                return Application.createImageIcon(Workbench.class, "img/32/folder_grey_open.png");
            }

            @Override
            protected Icon getContainerClosedIcon() {
                return Application.createImageIcon(Workbench.class, "img/32/folder_grey.png");
            }

            @Override
            protected Icon getItemIcon(Item item, String upnpClass) {
                if (upnpClass != null) {
                    if (upnpClass.startsWith("object.item.audioItem")) {
                        return Application.createImageIcon(Workbench.class, "img/32/audio.png");
                    }
                    if (upnpClass.startsWith("object.item.videoItem")) {
                        return Application.createImageIcon(Workbench.class, "img/32/video.png");
                    }
                }
                return Application.createImageIcon(Workbench.class, "img/32/misc.png");
            }

            @Override
            protected Icon getInfoIcon() {
                return Application.createImageIcon(Workbench.class, "img/32/info.png");
            }
        };
    }

/*
    // You don't have to do this if you are happy with the message _inside_ the tree...
    @Override
    public void updateStatus(ContentBrowseActionCallback.Status status,
                             DefaultMutableTreeNode treeNode,
                             DefaultTreeModel treeModel) {
        super.updateStatus(status, treeNode, treeModel);
        //controller.getStatusPanel().setStatusMessage(status.getDefaultMessage());
    }

*/
    public void failure(String message) {
        Workbench.APP.log(new LogMessage(Level.SEVERE, "ContentDirectoryService Browser", message));
    }

}
