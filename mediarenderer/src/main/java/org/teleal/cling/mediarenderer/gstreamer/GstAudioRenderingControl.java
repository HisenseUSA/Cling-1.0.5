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

import org.teleal.cling.model.types.ErrorCode;
import org.teleal.cling.model.types.UnsignedIntegerFourBytes;
import org.teleal.cling.model.types.UnsignedIntegerTwoBytes;
import org.teleal.cling.support.lastchange.LastChange;
import org.teleal.cling.support.renderingcontrol.AbstractAudioRenderingControl;
import org.teleal.cling.support.renderingcontrol.RenderingControlErrorCode;
import org.teleal.cling.support.renderingcontrol.RenderingControlException;
import org.teleal.cling.support.model.Channel;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class GstAudioRenderingControl extends AbstractAudioRenderingControl {

    final private static Logger log = Logger.getLogger(GstAudioRenderingControl.class.getName());

    final private Map<UnsignedIntegerFourBytes, GstMediaPlayer> players;

    protected GstAudioRenderingControl(LastChange lastChange, Map<UnsignedIntegerFourBytes, GstMediaPlayer> players) {
        super(lastChange);
        this.players = players;
    }

    protected Map<UnsignedIntegerFourBytes, GstMediaPlayer> getPlayers() {
        return players;
    }

    protected GstMediaPlayer getInstance(UnsignedIntegerFourBytes instanceId) throws RenderingControlException {
        GstMediaPlayer player = getPlayers().get(instanceId);
        if (player == null) {
            throw new RenderingControlException(RenderingControlErrorCode.INVALID_INSTANCE_ID);
        }
        return player;
    }

    protected void checkChannel(String channelName) throws RenderingControlException {
        if (!getChannel(channelName).equals(Channel.Master)) {
            throw new RenderingControlException(ErrorCode.ARGUMENT_VALUE_INVALID, "Unsupported audio channel: " + channelName);
        }
    }

    @Override
    public boolean getMute(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        checkChannel(channelName);
        return getInstance(instanceId).getVolume() == 0;
    }

    @Override
    public void setMute(UnsignedIntegerFourBytes instanceId, String channelName, boolean desiredMute) throws RenderingControlException {
        checkChannel(channelName);
        log.fine("Setting backend mute to: " + desiredMute);
        getInstance(instanceId).setMute(desiredMute);
    }

    @Override
    public UnsignedIntegerTwoBytes getVolume(UnsignedIntegerFourBytes instanceId, String channelName) throws RenderingControlException {
        checkChannel(channelName);
        int vol = (int) (getInstance(instanceId).getVolume() * 100);
        log.fine("Getting backend volume: " + vol);
        return new UnsignedIntegerTwoBytes(vol);
    }

    @Override
    public void setVolume(UnsignedIntegerFourBytes instanceId, String channelName, UnsignedIntegerTwoBytes desiredVolume) throws RenderingControlException {
        checkChannel(channelName);
        double vol = desiredVolume.getValue() / 100d;
        log.fine("Setting backend volume to: " + vol);
        getInstance(instanceId).setVolume(vol);
    }

}