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

package org.teleal.cling.mediarenderer.gstreamer;

import org.teleal.cling.binding.LocalServiceBinder;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.mediarenderer.MediaRenderer;
import org.teleal.cling.mediarenderer.display.DisplayHandler;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.ModelUtil;
import org.teleal.cling.model.ServiceManager;
import org.teleal.cling.model.ValidationException;
import org.teleal.cling.model.meta.DeviceDetails;
import org.teleal.cling.model.meta.DeviceIdentity;
import org.teleal.cling.model.meta.Icon;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.meta.ManufacturerDetails;
import org.teleal.cling.model.meta.ModelDetails;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.renderingcontrol.lastchange.RenderingControlLastChangeParser;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Christian Bauer
 */
public class GstMediaRenderer {

    public static final long LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS = 500;

    final protected LocalServiceBinder binder = new AnnotationLocalServiceBinder();

    // These are shared between all "logical" player instances of a single service
    final protected LastChange avTransportLastChange = new LastChange(new AVTransportLastChangeParser());
    final protected LastChange renderingControlLastChange = new LastChange(new RenderingControlLastChangeParser());

    final protected Map<UnsignedIntegerFourBytes, GstMediaPlayer> mediaPlayers;

    final protected ServiceManager<GstConnectionManagerService> connectionManager;
    final protected ServiceManager<GstAVTransportService> avTransport;
    final protected ServiceManager<GstAudioRenderingControl> renderingControl;

    final protected LocalDevice device;

    protected DisplayHandler displayHandler;

    public GstMediaRenderer(int numberOfPlayers, final DisplayHandler displayHandler) {
        this.displayHandler = displayHandler;

        // This is the backend which manages the actual player instances
        mediaPlayers = new GstMediaPlayers(
                numberOfPlayers,
                avTransportLastChange,
                renderingControlLastChange
        ) {
            // These overrides connect the player instances to the output/display
            @Override
            protected void onPlay(GstMediaPlayer player) {
                getDisplayHandler().onPlay(player);
            }

            @Override
            protected void onStop(GstMediaPlayer player) {
                getDisplayHandler().onStop(player);
            }
        };

        // The connection manager doesn't have to do much, HTTP is stateless
        LocalService connectionManagerService = binder.read(GstConnectionManagerService.class);
        connectionManager =
                new DefaultServiceManager(connectionManagerService) {
                    @Override
                    protected Object createServiceInstance() throws Exception {
                        return new GstConnectionManagerService();
                    }
                };
        connectionManagerService.setManager(connectionManager);

        // The AVTransport just passes the calls on to the backend players
        LocalService<GstAVTransportService> avTransportService = binder.read(GstAVTransportService.class);
        avTransport =
                new DefaultServiceManager<GstAVTransportService>(avTransportService) {
                    @Override
                    protected GstAVTransportService createServiceInstance() throws Exception {
                        return new GstAVTransportService(avTransportLastChange, mediaPlayers);
                    }
                };
        avTransportService.setManager(avTransport);

        // The Rendering Control just passes the calls on to the backend players
        LocalService<GstAudioRenderingControl> renderingControlService = binder.read(GstAudioRenderingControl.class);
        renderingControl =
                new DefaultServiceManager<GstAudioRenderingControl>(renderingControlService) {
                    @Override
                    protected GstAudioRenderingControl createServiceInstance() throws Exception {
                        return new GstAudioRenderingControl(renderingControlLastChange, mediaPlayers);
                    }
                };
        renderingControlService.setManager(renderingControl);

        try {

            device = new LocalDevice(
                    new DeviceIdentity(UDN.uniqueSystemIdentifier("Cling MediaRenderer")),
                    new UDADeviceType("MediaRenderer", 1),
                    new DeviceDetails(
                            "MediaRenderer on " + ModelUtil.getLocalHostName(false),
                            new ManufacturerDetails("Cling", "http://teleal.org/projects/cling/"),
                            new ModelDetails("Cling MediaRenderer", MediaRenderer.APPNAME, "1", "http://teleal.org/projects/cling/mediarenderer/")
                    ),
                    new Icon[]{createDefaultDeviceIcon()},
                    new LocalService[]{
                            avTransportService,
                            renderingControlService,
                            connectionManagerService
                    }
            );

        } catch (ValidationException ex) {
            throw new RuntimeException(ex);
        }

        runLastChangePushThread();
    }

    // The backend player instances will fill the LastChange whenever something happens with
    // whatever event messages are appropriate. This loop will periodically flush these changes
    // to subscribers of the LastChange state variable of each service.
    protected void runLastChangePushThread() {
        // TODO: We should only run this if we actually have event subscribers
        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        // These operations will NOT block and wait for network responses
                        avTransport.getImplementation().fireLastChange();
                        renderingControl.getImplementation().fireLastChange();
                        Thread.sleep(LAST_CHANGE_FIRING_INTERVAL_MILLISECONDS);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }.start();
    }

    public LocalDevice getDevice() {
        return device;
    }

    synchronized public DisplayHandler getDisplayHandler() {
        return displayHandler;
    }

    synchronized public void setDisplayHandler(DisplayHandler displayHandler) {
        this.displayHandler = displayHandler;
    }

    synchronized public Map<UnsignedIntegerFourBytes, GstMediaPlayer> getMediaPlayers() {
        return mediaPlayers;
    }

    synchronized public void stopAllMediaPlayers() {
        for (GstMediaPlayer mediaPlayer : mediaPlayers.values()) {
            TransportState state =
                mediaPlayer.getCurrentTransportInfo().getCurrentTransportState();
            if (!state.equals(TransportState.NO_MEDIA_PRESENT) ||
                    state.equals(TransportState.STOPPED)) {
                MediaRenderer.APP.log(Level.FINE, "Stopping player instance: " + mediaPlayer.getInstanceId());
                mediaPlayer.stop();
            }
        }
    }

    public ServiceManager<GstConnectionManagerService> getConnectionManager() {
        return connectionManager;
    }

    public ServiceManager<GstAVTransportService> getAvTransport() {
        return avTransport;
    }

    public ServiceManager<GstAudioRenderingControl> getRenderingControl() {
        return renderingControl;
    }

    protected Icon createDefaultDeviceIcon() {
        String iconPath = "img/48/mediarenderer.png";
        try {
            return new Icon(
                    "image/png",
                    48, 48, 8,
                    URI.create("icon.png"),
                    MediaRenderer.class.getResourceAsStream(iconPath)
            );
        } catch (IOException ex) {
            throw new RuntimeException("Could not load icon: " + iconPath, ex);
        }
    }

}
