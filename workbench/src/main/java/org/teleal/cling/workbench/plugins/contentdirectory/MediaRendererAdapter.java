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

import org.teleal.cling.UpnpService;
import org.teleal.cling.model.action.ActionInvocation;
import org.teleal.cling.model.message.UpnpResponse;
import org.teleal.cling.model.meta.Device;
import org.teleal.cling.model.meta.Service;
import org.teleal.cling.model.types.DeviceType;
import org.teleal.cling.model.types.ServiceType;
import org.teleal.cling.model.types.UDADeviceType;
import org.teleal.cling.model.types.UDAServiceType;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.support.avtransport.callback.SetAVTransportURI;
import org.teleal.cling.support.connectionmanager.callback.GetProtocolInfo;
import org.teleal.cling.support.model.Protocol;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.ProtocolInfos;
import org.teleal.cling.support.model.Res;
import org.teleal.common.util.MimeType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public abstract class MediaRendererAdapter {

    final private static Logger log = Logger.getLogger(MediaRendererAdapter.class.getName());

    public static final DeviceType SUPPORTED_MEDIA_RENDERER_TYPE = new UDADeviceType("MediaRenderer", 1);
    public static final ServiceType SUPPORTED_CONNECTION_MGR_TYPE = new UDAServiceType("ConnectionManager", 1);
    public static final ServiceType SUPPORTED_AV_TRANSPORT_TYPE = new UDAServiceType("AVTransport", 1);

    final protected Map<Device, List<ProtocolInfo>> availableRenderers = new HashMap();
    final protected UpnpService upnpService;

    protected MediaRendererAdapter(UpnpService upnpService) {
        this.upnpService = upnpService;
    }

    synchronized public Map<Device, List<ProtocolInfo>> getAvailableRenderers() {
        return availableRenderers;
    }

    synchronized protected void updateMediaRenderers() {
        log.fine("Updating media renderers");

        Collection<Device> foundMediaRenderers = upnpService.getRegistry().getDevices(SUPPORTED_MEDIA_RENDERER_TYPE);

        log.fine("Mediarenderers found in local registry: " + foundMediaRenderers.size());

        for (final Device foundMediaRenderer : foundMediaRenderers) {

            // Queue a GetProtocolInfo action that will add the renderer + protocol info to the available renderer map
            if (!availableRenderers.containsKey(foundMediaRenderer)) {

                log.fine("New media renderer, preparing to get protocol information: " + foundMediaRenderer);

                Service connectionManager =
                        foundMediaRenderer.findService(SUPPORTED_CONNECTION_MGR_TYPE);

                if (connectionManager == null) {
                    log.warning("MediaRenderer device has no ConnectionManager service: " + foundMediaRenderer);
                    break;
                }

                GetProtocolInfo getProtocolInfoActionCallback =
                        new GetProtocolInfo(connectionManager) {

                            @Override
                            public void received(ActionInvocation actionInvocation, ProtocolInfos sinkProtocolInfos, ProtocolInfos sourceProtocolInfos) {
                                addMediaRendererInformation(foundMediaRenderer, sinkProtocolInfos);
                            }

                            @Override
                            public void failure(ActionInvocation invocation,
                                                UpnpResponse operation,
                                                String defaultMsg) {
                                addMediaRendererInformation(foundMediaRenderer, Collections.EMPTY_LIST);
                                updateStatusFailure(
                                        "Error retrieving protocol info from " +
                                                foundMediaRenderer.getDetails().getFriendlyName() + ". " + defaultMsg
                                );
                            }

                        };

                upnpService.getControlPoint().execute(getProtocolInfoActionCallback);
            }
        }

        // Remove renderers from the available renderers map if they are not in the registry anymore
        Iterator<Map.Entry<Device, List<ProtocolInfo>>> it = availableRenderers.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Device, List<ProtocolInfo>> currentDeviceEntry = it.next();
            if (!foundMediaRenderers.contains(currentDeviceEntry.getKey())) {
                it.remove();
            }
        }

    }

    synchronized void addMediaRendererInformation(Device mediaRenderer, List<ProtocolInfo> protocolInfos) {
        availableRenderers.put(mediaRenderer, protocolInfos);
    }

    protected void sendToMediaRenderer(final int instanceId, final Service avTransportService, String uri) {

        SetAVTransportURI setAVTransportURIActionCallback =
                new SetAVTransportURI(new UnsignedIntegerFourBytes(instanceId), avTransportService, uri) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        updateStatus(
                                "Successfuly sent URI to: (Instance: " + instanceId + ") " +
                                        avTransportService.getDevice().getDetails().getFriendlyName()
                        );
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        updateStatusFailure(
                                "Failed to send URI: " + defaultMsg
                        );
                    }
                };

        upnpService.getControlPoint().execute(setAVTransportURIActionCallback);
    }

    protected boolean isProtocolInfoMatch(List<ProtocolInfo> supportedProtocols, Res resource) {
        ProtocolInfo resourceProtocolInfo = resource.getProtocolInfo();
        if (!resourceProtocolInfo.getProtocol().equals(Protocol.HTTP_GET)) return false;

        MimeType resourceMimeType;
        try {
            resourceMimeType = resourceProtocolInfo.getContentFormatMimeType();
        } catch (IllegalArgumentException ex) {
            log.warning("Illegal resource mime type: " + resourceProtocolInfo.getContentFormat());
            return false;
        }

        for (ProtocolInfo supportedProtocol : supportedProtocols) {
            // We currently only support HTTP-GET
            if (!Protocol.HTTP_GET.equals(supportedProtocol.getProtocol())) continue;
            try {
                if (supportedProtocol.getContentFormatMimeType().equals(resourceMimeType)) {
                    return true;
                } else if (supportedProtocol.getContentFormatMimeType().isCompatible(resourceMimeType)) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                log.warning("Illegal MediaRenderer supported mime type: " + supportedProtocol.getContentFormat());
            }
        }
        return false;
    }

    protected abstract void updateStatus(String statusMessage);
    protected abstract void updateStatusFailure(String statusMessage);
}
