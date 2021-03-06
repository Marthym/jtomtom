/**
 * Copyright© 2010, 2011  Frédéric Combes
 * This file is part of jTomtom.
 * <p/>
 * jTomtom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * jTomtom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with jTomtom.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Frédéric Combes can be reached at:
 * <belz12@yahoo.fr>
 */
package org.jtomtom;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jtomtom.tools.NetworkTester;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import static org.junit.Assert.*;

@Ignore
public class TestNetworkTester {
    @BeforeClass
    public static void initLogger() {
        if (!Logger.getRootLogger().getAllAppenders().hasMoreElements())
            BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.DEBUG);
    }

    @Test
    public void testIsNetworkAvailable() {
        Proxy proxy = Application.getInstance().getProxyServer();
        boolean withProxy = NetworkTester.getInstance().isNetworkAvailable(proxy);

        assertTrue(withProxy);
    }

    @Test
    public void testCalculateResponseTime() {
        Proxy proxy = Application.getInstance().getProxyServer();
        long accessTime = NetworkTester.getInstance().calculateResponseTime(proxy);

        assertFalse(accessTime <= 0);
        assertFalse(accessTime > 10000);
    }

    @Test
    public void testValidNetworkAvailability() {
        boolean jttExceptionThrowed = false;
        NetworkTester.getInstance().resetNetworkTesterInstance();
        Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("1.1.1.1", 1111));
        try {
            NetworkTester.getInstance().validNetworkAvailability(proxy);
        } catch (JTomtomException e) {
            jttExceptionThrowed = true;
        }
        assertTrue(jttExceptionThrowed);

        NetworkTester.getInstance().resetNetworkTesterInstance();
        proxy = Application.getInstance().getProxyServer();
        try {
            NetworkTester.getInstance().validNetworkAvailability(proxy);
        } catch (JTomtomException e) {
            fail(e.getLocalizedMessage());
        }
    }
}
