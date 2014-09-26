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

package org.teleal.cling.workbench.plugins.avtransport.state;

import org.teleal.cling.support.model.TransportState;
import org.teleal.cling.workbench.plugins.avtransport.ui.InstanceController;

import javax.swing.SwingUtilities;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class AVTransportClientState {

    static final public Map<TransportState, Class<? extends AVTransportClientState>> STATE_MAP =
            new HashMap<TransportState, Class<? extends AVTransportClientState>>() {{
                put(TransportState.NO_MEDIA_PRESENT, NoMediaPresent.class);
                put(TransportState.STOPPED, Stopped.class);
                put(TransportState.PLAYING, Playing.class);
                put(TransportState.PAUSED_PLAYBACK, PausedPlay.class);
                put(TransportState.TRANSITIONING, Transitioning.class);
            }};

    private InstanceController instanceController;

    public AVTransportClientState(InstanceController instanceController) {
        this.instanceController = instanceController;
    }

    public InstanceController getInstanceController() {
        return instanceController;
    }

    public abstract void onEntry();

    public abstract void onExit();

    public abstract class UserInterfaceUpdate implements Runnable {
        protected UserInterfaceUpdate() {
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            run(getInstanceController());
        }

        protected abstract void run(InstanceController controller);
    }

}
