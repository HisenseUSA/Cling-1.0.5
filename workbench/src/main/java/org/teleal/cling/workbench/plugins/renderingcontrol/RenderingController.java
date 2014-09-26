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

package org.teleal.cling.workbench.plugins.renderingcontrol;

import org.teleal.cling.controlpoint.ControlPoint;
import org.teleal.cling.model.gena.CancelReason;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.common.swingfwk.AbstractController;
import org.teleal.common.swingfwk.Controller;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class RenderingController extends AbstractController<JFrame> {

    final private static Logger log = Logger.getLogger(RenderingController.class.getName());

    public static final int SUPPORTED_INSTANCES = 8;

    // Dependencies
    final protected ControlPoint controlPoint;
    final protected Service service;
    final protected RenderingControlCallback callback;

    // Model
    final Map<UnsignedIntegerFourBytes, InstanceController> instances = new LinkedHashMap();

    // View
    private final JTabbedPane tabs = new JTabbedPane();

    public RenderingController(Controller parentController, ControlPoint controlPoint, Service service) {
        super(new JFrame(service.getDevice().getDetails().getFriendlyName()), parentController);
        this.controlPoint = controlPoint;
        this.service = service;
        this.callback = new RenderingControlCallback(service, this);

        tabs.setFont(new Font(Font.DIALOG, Font.PLAIN, 20));

        for (int i = 0; i < SUPPORTED_INSTANCES; i++) {
            UnsignedIntegerFourBytes instanceId = new UnsignedIntegerFourBytes(i);
            InstanceController instance = new InstanceController(this, controlPoint, callback, instanceId);
            instances.put(instanceId, instance);
            tabs.addTab(Integer.toString(i), instance.getView());
        }

        getView().addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent windowEvent) {
                        RenderingController.this.dispose();
                    }
                }
        );

        getView().getContentPane().add(tabs, BorderLayout.CENTER);
        getView().setResizable(false);
        getView().pack();

        connect();
    }

    public InstanceController getInstanceController(UnsignedIntegerFourBytes instanceId) {
        return instances.get(instanceId);
    }

    @Override
    public void dispose() {
        callback.end(); // End subscription when controller ends
        super.dispose();
    }

    protected void connect() {

        // Register with the service for future LAST CHANGE events
        controlPoint.execute(callback);

        // The initial event is useless with the LAST CHANGE mechanism (the initial state is "nothing changed", great...),
        // so we need to query the initial state

        for (InstanceController instance : instances.values()) {
            instance.updateVolume();
        }
    }

    public void disconnect(final CancelReason reason) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String title = "DISCONNECTED: " + (reason != null ? reason.toString() : "");
                getView().setTitle(title);
                for (InstanceController instance : instances.values()) {
                    instance.getMuteButton().setEnabled(false);
                    instance.getVolumeSlider().setEnabled(false);
                }
            }
        });
    }

}
