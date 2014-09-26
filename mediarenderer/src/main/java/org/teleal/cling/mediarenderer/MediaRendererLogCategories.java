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

import org.teleal.cling.support.shared.LogCategories;
import org.teleal.common.swingfwk.logging.LogCategory;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaRendererLogCategories extends LogCategories {

    public MediaRendererLogCategories() {
        super();

        add(new LogCategory("Cling MediaRenderer", new LogCategory.Group[]{

                new LogCategory.Group(
                        "MediaRenderer UPnP services",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("org.teleal.cling.support.renderingcontrol", Level.FINER),
                                new LogCategory.LoggerLevel("org.teleal.cling.support.avtransport", Level.FINER),
                                new LogCategory.LoggerLevel("org.teleal.cling.support.connectionmanager", Level.FINER),
                                new LogCategory.LoggerLevel("org.teleal.cling.support.lastchange", Level.FINER),
                                new LogCategory.LoggerLevel("org.teleal.common.statemachine", Level.FINER),
                        }
                ),

                new LogCategory.Group(
                        "GStreamer backend",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("org.teleal.cling.mediarenderer.gstreamer", Level.FINER),
                        }
                ),

                new LogCategory.Group(
                        "Display",
                        new LogCategory.LoggerLevel[]{
                                new LogCategory.LoggerLevel("org.teleal.cling.mediarenderer.display", Level.FINER),
                        }
                )
        }));

    }
}

