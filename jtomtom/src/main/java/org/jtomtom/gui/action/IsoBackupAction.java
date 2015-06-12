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

import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import org.apache.log4j.Logger;
import org.jtomtom.Application;
import org.jtomtom.JTomtomException;
import org.jtomtom.device.TomtomDevice;
import org.jtomtom.device.TomtomMap;
import org.jtomtom.gui.TabBackupDevice;
import org.jtomtom.gui.WaitingDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

/**
 * @author Frédéric Combes
 */
public class IsoBackupAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(IsoBackupAction.class);

    private WaitingDialog waitingDialog = null;
    private String targetIsoFile;
    private boolean makeTestISO = false;

    public IsoBackupAction(String p_label) {
        super(p_label);
    }

    public void actionPerformed(ActionEvent event) {
        if (JButton.class.isAssignableFrom(event.getSource().getClass())) {
            JButton bp = (JButton) event.getSource();
            if (TabBackupDevice.class.isAssignableFrom(bp.getParent().getClass())) {
                TabBackupDevice tabSauvegarde = (TabBackupDevice) bp.getParent();
                targetIsoFile = tabSauvegarde.getTargetFile();
                makeTestISO = tabSauvegarde.getMakeTestISO();
            }
        }

        SwingWorker<ActionResult, Void> worker = new SwingWorker<ActionResult, Void>() {

            @Override
            protected ActionResult doInBackground() throws Exception {
                final TomtomDevice theDevice = Application.getInstance().getTheDevice();

                ActionResult result = new ActionResult();
                try {
                    String mountPoint = theDevice.getMountPoint();
                    if (mountPoint.isEmpty()) {
                        throw new JTomtomException("org.jtomtom.errors.gps.nomountpoint");
                    }
                    result.status = createGpsBackup(theDevice);

                } catch (JTomtomException e) {
                    LOGGER.error(e.getLocalizedMessage());
                    if (LOGGER.isDebugEnabled()) e.printStackTrace();
                    result.exception = e;
                }

                return result;
            }

            @Override
            public void done() {
                // First, Close wait window
                if (waitingDialog != null) {
                    waitingDialog.dispose();
                }

                // Second, check for possible errors
                ActionResult result;
                try {
                    result = get();
                    if (!result.status) {
                        // If error show message
                        JOptionPane.showMessageDialog(null, result.exception.getLocalizedMessage(),
                                Application.getInstance().getMainTranslator().getString("org.jtomtom.main.dialog.default.error.title"),
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (ExecutionException e) {
                    LOGGER.warn(e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    LOGGER.warn(e.getLocalizedMessage());
                }
            }
        };
        waitingDialog = new WaitingDialog(worker);
        waitingDialog.setVisible(true);

    }

    /**
     * Init the path of the result ISO file. Just use in test case.
     *
     * @param p_fichier Absolute path of the created file
     */
    public void setTargetFile(String p_fichier) {
        targetIsoFile = p_fichier;
    }

    /**
     * Create ISO backup from the GPS file system
     *
     * @param p_GPS     Mount point of the GPS
     * @param p_forTest True if we want create ISO file for test. The file will be smaller
     * @return true if OK
     */
    public boolean createGpsBackup(TomtomDevice p_GPS, boolean p_forTest) {
        File gpsDir = new File(p_GPS.getMountPoint());
        if (!gpsDir.exists() || !gpsDir.canRead()) {
            throw new JTomtomException("org.jtomtom.errors.gps.directorycannotberead");
        }
        ISO9660RootDirectory isoRoot = new ISO9660RootDirectory();
        try {
            if (!p_forTest) {
                isoRoot.addContentsRecursively(gpsDir);

            } else {
                LOGGER.debug("Restricting the contents of the ISO test file");
                // - For test ISO file we just put necessary file.
                java.util.Map<String, TomtomMap> mapsList = p_GPS.getAvailableMaps();

                for (String currFileName : gpsDir.list()) {
                    File current = new File(gpsDir, currFileName);

                    if (mapsList.containsKey(current.getName())) {
                        LOGGER.debug("Remove DAT files from map " + current.getName());
                        // We clean map directories and remove big files
                        ISO9660Directory isoCurrDir = isoRoot.addDirectory(current);
                        String[] mapFileList = current.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return !name.endsWith(".dat");
                            }
                        });

                        for (String currMapFileName : mapFileList) {
                            isoCurrDir.addRecursively(new File(current, currMapFileName));
                        }

                    } else if ("voices".equalsIgnoreCase(current.getName())) {
                        LOGGER.debug("Remove CHK files from voice directory");
                        ISO9660Directory isoCurrDir = isoRoot.addDirectory(current);
                        String[] notchkFileList = current.list(new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                return !name.endsWith(".chk");
                            }
                        });

                        for (String currMapFileName : notchkFileList) {
                            isoCurrDir.addRecursively(new File(current, currMapFileName));
                        }

                    } else if ("helpme".equalsIgnoreCase(current.getName())) {
                        LOGGER.debug("Empty help directory");
                        isoRoot.addDirectory(current);

                    } else {
                        isoRoot.addRecursively(current);
                    }
                } // for (String currFileName: gpsDir.list())

            } // end if (!p_forTest)

        } catch (HandlerException e) {
            throw new JTomtomException(e);
        }

        if (targetIsoFile == null || targetIsoFile.isEmpty()) {
            throw new JTomtomException("org.jtomtom.errors.backup.destmustexist");
        }

        // Check files list for bad encoding
        checkForBadEncoding(isoRoot);

        // ISO9660 support
        ISO9660Config iso9660Config = new ISO9660Config();
        try {
            iso9660Config.allowASCII(false);
            iso9660Config.setDataPreparer("jTomtom");
            iso9660Config.forceDotDelimiter(true);

        } catch (ConfigException e) {
            throw new JTomtomException(e);
        }

        // Joliet support
        JolietConfig jolietConfig = new JolietConfig();
        try {
            jolietConfig.setDataPreparer("jTomtom");
            jolietConfig.forceDotDelimiter(true);

        } catch (ConfigException e) {
            throw new JTomtomException(e);

        }

        // Create ISO
        File outputIsoFile = new File(targetIsoFile);
        StreamHandler streamHandler;
        try {
            streamHandler = new ISOImageFileHandler(outputIsoFile);
            CreateISO iso = new CreateISO(streamHandler, isoRoot);
            LOGGER.debug("Start creating ISO backup file ...");
            iso.process(iso9660Config, null, jolietConfig, null);

        } catch (FileNotFoundException e) {
            throw new JTomtomException(e);

        } catch (HandlerException e) {
            throw new JTomtomException(e);

        }

        LOGGER.debug("End creating ISO file.");
        return true;
    }

    private void checkForBadEncoding(ISO9660Directory directory) {
        Iterator<?> it = directory.getFiles().iterator();
        while (it.hasNext()) {
            Object anObject = it.next();
            if (File.class.isAssignableFrom(anObject.getClass())) {
                File aFile = (File) anObject;
                if (!aFile.exists()) {
                    it.remove();
                    LOGGER.warn("Exclude file " + aFile.getAbsolutePath() + " from backup, it does not exist !");
                }
            }
        }

        if (directory.hasSubDirs()) {
            it = directory.getDirectories().iterator();
            while (it.hasNext()) {
                checkForBadEncoding((ISO9660Directory) it.next());
            }
        }
    }

    public boolean createGpsBackup(TomtomDevice p_GPS) {
        return createGpsBackup(p_GPS, makeTestISO);
    }

}
