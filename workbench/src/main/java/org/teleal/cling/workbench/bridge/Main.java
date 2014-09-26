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

package org.teleal.cling.workbench.bridge;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.binding.annotations.AnnotationLocalServiceBinder;
import org.teleal.cling.model.DefaultServiceManager;
import org.teleal.cling.model.meta.LocalService;
import org.teleal.cling.model.types.UDN;
import org.teleal.cling.workbench.bridge.backend.Bridge;
import org.teleal.cling.workbench.plugins.binarylight.device.DemoBinaryLight;
import org.teleal.common.logging.LoggingUtil;
import org.teleal.common.util.URIUtil;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

/**
 * @author Christian Bauer
 */
public class Main {

    final protected UpnpService upnpService;
    final protected Bridge bridge;

    public static void main(String[] args) throws Exception {

        final Options options = new Options();
        CmdLineParser cmdLineParser = new CmdLineParser(options);
        try {
            cmdLineParser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("USAGE: java -jar <jarfile> [options]");
            cmdLineParser.printUsage(System.err);
            System.exit(1);
        }

        File file = new File("cling-logging.properties");
        if (file.exists())
            LoggingUtil.loadDefaultConfiguration(new FileInputStream(file));

        new Main(options);
    }

    public Main(Options options) throws Exception {
        upnpService = new UpnpServiceImpl() {
            @Override
            public void shutdown() {
                bridge.stop(true);
                super.shutdown();
            }
        };

        bridge = new Bridge(upnpService);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                upnpService.shutdown();
            }
        });

        bridge.start(options.lanHost, URIUtil.toURL(URI.create(options.localURL)));

        if (options.demo) {
            UDN udn = UDN.uniqueSystemIdentifier("Bridge Demo Binary Light");
            LocalService service = new AnnotationLocalServiceBinder().read(DemoBinaryLight.class);

            service.setManager(
                    new DefaultServiceManager(service) {
                        @Override
                        protected Object createServiceInstance() throws Exception {
                            return new DemoBinaryLight() {
                                @Override
                                public void setTarget(boolean newTargetValue) {
                                    super.setTarget(newTargetValue);
                                    System.out.println("### DEMO LIGHT SET TARGET TO: " + newTargetValue);
                                }
                            };
                        }
                    }
            );

            upnpService.getRegistry().addDevice(DemoBinaryLight.createDefaultDevice(udn, service));
        }

        upnpService.getControlPoint().search();
    }

    public static class Options {

        @Option(required = true, name = "-h", metaVar = "<IP>", usage = "The LAN IP of this host")
        public String lanHost;

        @Option(required = true, name = "-url", metaVar = "<URL>", usage = "The local WAN URL (e.g. http://<external IP>:<mapped port>)")
        public String localURL;

        @Option(required = false, name = "-demo", usage = "Start demo binary light device and service")
        public boolean demo;
    }

}
