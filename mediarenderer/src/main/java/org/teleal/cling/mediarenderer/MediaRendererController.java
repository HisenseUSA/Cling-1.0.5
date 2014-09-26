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

package org.teleal.cling.mediarenderer;

import org.gstreamer.Gst;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.mediarenderer.display.DisplayHandler;
import org.teleal.cling.mediarenderer.display.FullscreenDisplayHandler;
import org.teleal.cling.mediarenderer.display.WindowedDisplayHandler;
import org.teleal.cling.mediarenderer.gstreamer.GstMediaRenderer;
import org.teleal.cling.support.shared.MainController;
import org.teleal.cling.support.shared.TextExpandDialog;
import org.teleal.cling.support.shared.TextExpandEvent;
import org.teleal.common.swingfwk.Application;
import org.teleal.common.swingfwk.DefaultEvent;
import org.teleal.common.swingfwk.DefaultEventListener;
import org.teleal.common.util.Exceptions;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

public class MediaRendererController extends MainController {

    public static final int SUPPORTED_INSTANCES = 8;
    public static final String ARG_USE_WINDOWS = "-w";

    final private UpnpService upnpService;

    // View
    final protected JPanel statusIconPanel = new JPanel();
    final protected JButton statusIconButton = new JButton(Application.createImageIcon(getClass(), "img/128/mediarenderer.png"));
    final protected JCheckBox fullscreenCheckbox = new JCheckBox("Fullscreen Video");

    // Model
    protected GstMediaRenderer mediaRenderer;

    boolean logPanelVisible = true;

    protected MediaRendererController() {
        super(new JFrame(MediaRenderer.APPNAME), new MediaRendererLogCategories());

        upnpService = new UpnpServiceImpl();

        registerEventListener(
                TextExpandEvent.class,
                new DefaultEventListener<String>() {
                    public void handleEvent(DefaultEvent<String> e) {
                        new TextExpandDialog(MediaRendererController.this.getView(), e.getPayload());
                    }
                }
        );

        statusIconButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                toggleLogPanel();
            }
        });
        statusIconButton.setFocusable(false);

        statusIconPanel.setPreferredSize(new Dimension(150, 200));
        statusIconPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        statusIconPanel.add(statusIconButton);

        fullscreenCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                toggleFullscreen();
            }
        });
        fullscreenCheckbox.setFocusable(false);
        statusIconPanel.add(fullscreenCheckbox);

        getLogPanel().setFocusable(false);
        getLogPanel().setPreferredSize(new Dimension(800, 175));

        getView().setMinimumSize(new Dimension(150, 200));
        getView().add(statusIconPanel, BorderLayout.WEST);
        getView().add(getLogPanel(), BorderLayout.CENTER);

        getView().addWindowListener(this);
        getView().pack();
        getView().setResizable(true);

    }

    public UpnpService getUpnpService() {
        return upnpService;
    }

    public GstMediaRenderer getMediaRenderer() {
        return mediaRenderer;
    }

    public void onViewReady(final String[] args) {
        new Thread() {
            @Override
            public void run() {
                MediaRenderer.APP.log(Level.INFO, "Initializing GStreamer backend and registering MediaRenderer device...");

                try {
                    fullscreenCheckbox.setSelected(
                            args.length != 1 || !args[0].equals(ARG_USE_WINDOWS)
                    );

                    // Pick a display method
                    DisplayHandler displayHandler = args.length == 1 && args[0].equals(ARG_USE_WINDOWS)
                            ? new WindowedDisplayHandler()
                            : new FullscreenDisplayHandler();

                    // Initialize the GStreamer backend (this also sets log level to WARNING for org.gstreamer)
                    Gst.init(MediaRenderer.APPNAME, args);

                    mediaRenderer =
                            new GstMediaRenderer(SUPPORTED_INSTANCES, displayHandler);

                    getUpnpService().getRegistry().addDevice(
                            mediaRenderer.getDevice()
                    );

                    MediaRenderer.APP.log(Level.INFO, "Initialization complete!");

                } catch (Throwable t) {
                    MediaRenderer.APP.log(Level.SEVERE, "Initialization failed: " + t);
                    MediaRenderer.APP.log(Level.SEVERE, "Cause: " + Exceptions.unwrap(t));
                }
            }
        }.start();
    }

    public void toggleLogPanel() {

        if (!logPanelVisible) {
            getView().add(getLogPanel(), BorderLayout.CENTER);
            getLogController().getLogTableModel().setPaused(false);
            logPanelVisible = true;
        } else {
            getView().remove(getLogPanel());
            getLogController().getLogTableModel().setPaused(true);
            logPanelVisible = false;
        }

        getView().pack();
    }

    public void toggleFullscreen() {
        if (fullscreenCheckbox.isSelected()
                && !(mediaRenderer.getDisplayHandler() instanceof FullscreenDisplayHandler)) {
            MediaRenderer.APP.log(Level.INFO, "Switching to fullscreen video rendering");
            getMediaRenderer().stopAllMediaPlayers();
            getMediaRenderer().setDisplayHandler(new FullscreenDisplayHandler());
        } else if (!fullscreenCheckbox.isSelected()
                && !(mediaRenderer.getDisplayHandler() instanceof WindowedDisplayHandler)) {
            MediaRenderer.APP.log(Level.INFO, "Switching to windowed video rendering");
            getMediaRenderer().stopAllMediaPlayers();
            getMediaRenderer().setDisplayHandler(new WindowedDisplayHandler());
        }
    }

}
