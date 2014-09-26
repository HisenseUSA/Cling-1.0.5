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

import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.workbench.Workbench;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.EventListener;
import org.teleal.common.swingfwk.logging.LogMessage;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 */
public class DetailController extends AbstractController<JPanel> {

    public static final int SUPPORTED_INSTANCES = 8;

    // Dependencies
    final protected MediaRendererAdapter mediaRendererAdapter;

    public DetailController(final ContentDirectoryController parentController) {
        super(new JPanel(), parentController);

        mediaRendererAdapter = new MediaRendererAdapter(parentController.getUpnpService()) {
            protected void updateStatus(final String statusMessage) {
                Workbench.APP.log(new LogMessage("ContentDirectoryService MediaRenderer Adapter", statusMessage));
            }

            protected void updateStatusFailure(final String statusMessage) {
                Workbench.APP.log(new LogMessage(Level.SEVERE, "ContentDirectoryService MediaRenderer Adapter", statusMessage));
            }
        };

        registerEventListener(
                ContainerSelectedEvent.class,
                new EventListener<ContainerSelectedEvent>() {
                    public void handleEvent(ContainerSelectedEvent e) {
                        showContainer(e.getPayload());
                    }
                }
        );

        registerEventListener(
                ItemSelectedEvent.class,
                new EventListener<ItemSelectedEvent>() {
                    public void handleEvent(ItemSelectedEvent e) {
                        showItem(e.getPayload());
                    }
                }
        );

        getView().setLayout(new BorderLayout());

        JLabel welcomeLabel = new JLabel("Please select an item.");
        welcomeLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        getView().add(welcomeLabel, BorderLayout.CENTER);

        mediaRendererAdapter.updateMediaRenderers();
    }

    @Override
    public ContentDirectoryController getParentController() {
        return (ContentDirectoryController)super.getParentController();
    }

    protected void showContainer(Container container) {
        getView().removeAll();

        JScrollPane scrollPane = new JScrollPane(new ContainerFormPanel(container));
        getView().add(scrollPane, BorderLayout.CENTER);

        getView().revalidate();
        getView().repaint();
    }

    protected void showItem(Item item) {
        getView().removeAll();

        // Whenever an item is selected, update the media renders, maybe we have new ones or old ones dropped out
        mediaRendererAdapter.updateMediaRenderers();

        ItemFormPanel itemFormPanel = new ItemFormPanel(item) {

            public List<JMenuItem> createSendToMenuItems(final Res resource) {
                List<JMenuItem> menuItems = new ArrayList();

                for (Map.Entry<Device, List<ProtocolInfo>> entry : mediaRendererAdapter.getAvailableRenderers().entrySet()) {

                    final Service avTransportService =
                            entry.getKey().findService(MediaRendererAdapter.SUPPORTED_AV_TRANSPORT_TYPE);

                    boolean protocolMatch =
                            mediaRendererAdapter.isProtocolInfoMatch(entry.getValue(), resource);

                    JMenuItem menuItem;

                    if (avTransportService != null && protocolMatch) {

                        menuItem = new JMenu(entry.getKey().getDetails().getFriendlyName());
                        for (int i = 0; i < SUPPORTED_INSTANCES; i++) {
                            final int instanceId = i;
                            JMenuItem instanceItem = new JMenuItem("Instance: " + i);
                            instanceItem.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent e) {
                                    mediaRendererAdapter.sendToMediaRenderer(instanceId, avTransportService, resource.getValue());
                                }
                            });
                            menuItem.add(instanceItem);
                        }

                    } else if (avTransportService == null) {

                        menuItem = new JMenuItem(
                                entry.getKey().getDetails().getFriendlyName() + " (Missing AV Transport Service)"
                        );
                        menuItem.setEnabled(false);

                    } else {

                        menuItem = new JMenuItem(
                                entry.getKey().getDetails().getFriendlyName() + " (Unsupported Protocol)"
                        );
                        menuItem.setEnabled(false);

                    }

                    menuItems.add(menuItem);
                }


                if (menuItems.size() == 0) {
                    JMenuItem noRenderersItem = new JMenuItem("No MediaRenderers found...");
                    noRenderersItem.setEnabled(false);
                    menuItems.add(noRenderersItem);
                }

                return menuItems;
            }
        };
        JScrollPane scrollPane = new JScrollPane(itemFormPanel);
        getView().add(scrollPane, BorderLayout.CENTER);

        getView().revalidate();
        getView().repaint();
    }



}
