# jTomtom
jTomtom is et Java GPS updater. With jTomtom you can update the QuickFix et Radar POI in your Tomtom GPS

Develop few years ago to allow me to update my Tomtom GPS from Linux, I not maintain the source anymore because of
Tomtom crypt all communication between device and TomtomHome.

I just move source from Sourceforge to Github and change build from ant to Maven.

## CHANGELOG

### jTomtom build 97 (12-13-2011)
* Improve: Refactor Robser plugin code for best reading
* Improve: Robser unit test
* Fix: Ant script for exclude junit test class of cleanning step

### jTomtom build 94 (12-12-2011)
* Fix: SF Tracker 3445483 - Problem at start with Caminat Live
* Fix: PdisES connector updated to the new site robser.es

### jTomtom build 90 (06-30-2011)
* Improve: Reduce the network access for send feedback information
* Improve: Update JIIC library (iso9660.jar) to the lastest version (1.1.2)
* Improve: Refactor Ant build script for compile with separate JRE more finest compilation options
* Add: Add test case for sending feedback infos function

### jTomtom build 70
* Fix: Version number display in About tab
* Improve: Move Ant scripts into subfolder
* Improve: Add Ant build script for Eclipse 3.7 (Indigo) and remove for 3.5

### jTomtom build 66
* Improve: Mac OS X Compatibility

### jTomtom build 63
* Improve: Refactor TabRadars, externalize Worker and translate comments
* Improve: Lot of refactor and translation comments
* Improve: Test jTomtom under OpenJDK JRE
* Fix: Chipset informations URL pointing on old web site.
* Add: Automatic chipset detection in the choose chipset dialog
* Add: Reset Quickfix data button on the Quickfix tab
* Add: Check chipset for detect error in chipset configuration 

### jTomtom build 62
* Fix: Bad parsing of new local db file

### jTomtom build 61
* Fix: Tomtomax update error after update of Tomtomax Web site

### jTomtom build 60
* Add: Possibility to set different password for each Radar Site (Connector)
* Improve: Translate forget french translation
* Fix: Error in properties reader class

### jTomtom build 59
* Fix: Crash when map directory does not exist
* Fix: SF Tracker 3173911 - Update iso9660 library to the last version
* Improve: Check for bad encoding files before create backup
* Improve: Refactor & Translate messages and comments of backup action class

### jTomtom build 57
* Fix: Modify Test case for Caminat
* Fix: SF Tracker 3169928 - QuickFix not working (NoSuchElementException)
* Improve: Refactor some code section for clean

### jTomtom build 55
* Add: New command line argument for enter in debug mode
* Add: Real website URL for each connector in Radars tab and remove correspondant translation
* Improve: Use translated and formated date for new version information
* Improve: Give more details when no GPS was found
* Improve: Application class refactor for better code readability
* Improve: Tab build at last time for save memory and reduce startup time
* Improve: Large Connectors refactor for stability and readability
* Improve: Modify some JTomtomException method signature for replace String[] by String... 
* Improve: Lots of other refactor for clean code
* Fix: Plugin PDIS.es crash when radars has never been installed
* Fix: Plugin Tomtomax crash when radars has never been installed
* Fix: NoConnector crash when find local informations
* Fix: Wait dialog message for generic message

### jTomtom build 51
* Add: Carminat Tomtom support
* Improve: Refactor application parameters
* Fix: Remote POIs informations get without first account setting crash with NullPointerException 
* Fix: Some spanish translations forget ... again :p

### jTomtom build 49
* Fix: Some spanish translations forget :s

### jTomtom build 47
* Add: Function for send GPS informations to jTomtom server
* Add: this release note
* Add: Buttons on the About tab for easy contact with developer
* Improve: Move new version warning on the bottom of the main window and remove the popup
* Improve: Check the proxy settings validity before save paraméters
* Improve: Some code rewrote for better readability
* Improve: Exceptions localization mechanism
* Improve: Move up POIs account section in parameters tab
* Fix: Some translations were no longer needed
* Fix: Java Exception when proxy was set to HTTP or SOCKS without server and port
* Fix: Inconsistent text in the tab "Radars" when no POIs are installed
