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
package org.jtomtom.gui.action;

import net.sf.jcablib.CabEntry;
import net.sf.jcablib.CabFile;
import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.Constant;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.Chipset;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.gui.TabQuickFix;
import org.jtomtom.gui.WaitingDialog;
import org.jtomtom.tools.HttpUtils;
import org.jtomtom.tools.NetworkTester;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * @author Frédéric Combes
 *
 */
public class UpdateQuickFixAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UpdateQuickFixAction.class);

    private WaitingDialog waitingDialog = null;
    private TabQuickFix tabQuickFix = null;

    public UpdateQuickFixAction(String p_label) {
        super(p_label);
    }

    public void actionPerformed(ActionEvent arg0) {
        // Get panel which call action for refresh later
        if (JButton.class.isAssignableFrom(arg0.getSource().getClass())) {
            JButton btQF = (JButton) arg0.getSource();
            if (TabQuickFix.class.isAssignableFrom(btQF.getParent().getClass())) {
                tabQuickFix = (TabQuickFix) btQF.getParent();
            }
        }

        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {
            @Override
            public ActionResult doInBackground() {
                ActionResult result = new ActionResult();
                try {
                    Application theApp = Application.getInstance();

                    NetworkTester.getInstance().validNetworkAvailability(theApp.getProxyServer());
                    updateQuickFix(theApp.getTheDevice());
                    result.status = true;

                } catch (JTomtomException e) {
                    result.status = false;
                    result.exception = e;
                }

                return result;
            }

            @Override
            public void done() {
                // First of all we close waiting dialog
                if (waitingDialog != null) {
                    waitingDialog.dispose();
                }

                // Second we check there are no error
                ActionResult result = null;
                try {
                    result = get();
                    if (!result.status) {
                        // If error occured we show message
                        JOptionPane.showMessageDialog(null, result.exception.getLocalizedMessage(),
                                Application.getInstance().getMainTranslator().getString("org.jtomtom.main.dialog.default.error.title"), JOptionPane.ERROR_MESSAGE);
                    }
                } catch (ExecutionException e) {
                    LOGGER.warn(e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getLocalizedMessage());
                }

                if (tabQuickFix != null && (result == null || result.status)) {
                    tabQuickFix.loadQuickFixInfos();
                }
            }
        };
        waitingDialog = new WaitingDialog(worker);
        waitingDialog.setVisible(true);
    }

    /**
     * Update the GPS by downloading the QuickGPSFix file and unzip it in GPS
     * @param theGPS    The device to be updated
     */
    public void updateQuickFix(TomtomDevice theGPS) {

        List<URL> filesToDownload = getEphemeridFilesURL(theGPS.getChipset());
        List<File> filesToUncab = downloadEphemeridFiles(filesToDownload);

        updateProgressBar(0, 0);

        Set<File> filesToInstall = uncabEphemeridFiles(filesToUncab);
        theGPS.updateQuickFix(filesToInstall);
    }

    private static List<URL> getEphemeridFilesURL(Chipset gpsChipset) {
        List<URL> filesUrl = new LinkedList<URL>();
        try {
            if (gpsChipset == Chipset.UNKNOWN) {
                for (Chipset cs : Chipset.available()) {
                    filesUrl.add(new URL(Constant.URL_EPHEMERIDE + cs));
                }
            } else {
                filesUrl.add(new URL(Constant.URL_EPHEMERIDE + gpsChipset));
            }

        } catch (MalformedURLException e) {
            LOGGER.warn(e);
        }
        return filesUrl;
    }

    private List<File> downloadEphemeridFiles(List<URL> filesLocations) {
        List<File> downloadedFiles = new LinkedList<File>();
        try {
            for (URL oneFileLocation : filesLocations) {
                File ephemFile = File.createTempFile("ephem", ".cab");
                ephemFile.deleteOnExit();

                HttpURLConnection conn =
                        (HttpURLConnection) oneFileLocation.openConnection(
                                Application.getInstance().getProxyServer());
                conn.setRequestProperty("User-agent", Constant.TOMTOM_USER_AGENT);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setReadTimeout(HttpUtils.TIMEOUT); // TimeOut for prevent conexion lost
                conn.connect();

                int currentSize = 0;
                if (LOGGER.isDebugEnabled()) LOGGER.debug("conn.getResponseCode() = " + conn.getResponseCode());

                int connResponseCode = conn.getResponseCode();
                if (connResponseCode != HttpURLConnection.HTTP_OK) {
                    throw new JTomtomException("org.jtomtom.errors.connexion.fail",
                            Integer.toString(connResponseCode), conn.getResponseMessage());
                }

                int fileSize = conn.getContentLength();
                InputStream is = null;
                FileOutputStream fout = null;
                try {

                    is = conn.getInputStream();
                    fout = new FileOutputStream(ephemFile);
                    byte buffer1[] = new byte[1024 * 128];
                    int k = 0;

                    while ((k = is.read(buffer1)) != -1) {
                        fout.write(buffer1, 0, k);
                        currentSize += k;

                        updateProgressBar(currentSize, fileSize);
                    }

                    downloadedFiles.add(ephemFile);

                } finally {
                    try {
                        fout.close();
                    } catch (Exception ignored) {
                    }
                    ;
                    try {
                        is.close();
                    } catch (Exception ignored) {
                    }
                    ;
                    conn.disconnect();
                }

            } // end for (URL oneFileLocation : filesLocations)

        } catch (IOException e) {
            throw new JTomtomException("Unable to download ephemerid files !", e);

        }
        return downloadedFiles;
    }

    private static Set<File> uncabEphemeridFiles(List<File> cabFiles) {
        Set<File> uncompressedFiles = new LinkedHashSet<File>();
        final int BUFFER = 2048;

        for (File oneCabinetFile : cabFiles) {
            InputStream cabis = null;
            BufferedOutputStream dest = null;
            FileOutputStream fos = null;

            CabFile theCabinet;
            try {
                theCabinet = new CabFile(oneCabinetFile);
                CabEntry[] entries = theCabinet.getEntries();

                for (CabEntry entry : entries) {
                    int count;

                    cabis = theCabinet.getInputStream(entry);
                    byte data[] = new byte[BUFFER];

                    File current = new File(oneCabinetFile.getParent(), entry.getName());

                    fos = new FileOutputStream(current);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = cabis.read(data)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();

                    LOGGER.debug("Extract " + current.getName() + " in " + current.getParent());
                    uncompressedFiles.add(current);
                }

            } catch (IOException e) {
                throw new JTomtomException(e);

            } finally {
                LOGGER.debug("Close uncab stream ...");
                try {
                    cabis.close();
                } catch (Exception ignored) {
                }
                try {
                    dest.close();
                } catch (Exception ignored) {
                }
                try {
                    fos.close();
                } catch (Exception ignored) {
                }
            }

        } // end for (File oneCabinetFile : cabFiles)

        return uncompressedFiles;
    }

    private void updateProgressBar(int current, int max) {
        if (waitingDialog != null) { // Need for test case
            waitingDialog.refreshProgressBar(current, max);
        }
    }
}
