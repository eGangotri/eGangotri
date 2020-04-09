# eGangotri Upload Tools and Utilities
Short Programs for handling ebooks uploading and ebooks management.

# To use. download the latest Jar from 
- https://github.com/eGangotri/eGangotri/blob/master/latestJarForUse/eGangotri.jar

- Make Sure you have Java 8 or above installed and Java is in the Path


- For using ArchiveUploader:
In Home Directory ( for Windows this is typically
 C:\Users\<username>) 
 you should have the following Directory Structure
 
 C:\Users\HOME_DIRECTORY\eGangotri
 archiveLogins.properties
 archiveMetadata.properties
 localFolders.properties
 settings.properties
 
 If you are using Google Uploader, then you need following files:
 googleDriveLogins.properties
 
 Samples of all with a README.txt is in the latestJarForUse Folder
 
 #Instructions in Order:
 - Create an archive.org account.
 - place details in archiveLogins.properties
 - identify the folder where the books will be picked from
 - put the Link to this Folder in localFolders.properties
 - create the mandatory generic metadata for your Files in archiveMetadata.properties.
 - run java -jar <path_To_jar>.jar PROFILE_ID_1 PROFILE_ID_2
 - java -Dwebdriver.chrome.driver=<PATH_TO_CHROMEDRIVER>chromedriver\chromedriver.exe -jar <PATH_TO_EGANGOTRI_JAR>/eGangotri.jar PRFL1 PRFL2
  
  Example:
   java -Dwebdriver.chrome.driver=C:\Users\user\eGangotri\chromedriver\chromedriver.exe -jar =C:\Users\user\eGangotri\eGangotri.jar PRFL1 PRFL2
  
  ** PRFL1 PRFL2 -> Read about how to setup Profile in the Main README
  https://github.com/eGangotri/eGangotri/blob/master/README.md
 
 In case Chromedriver gets outdated, get latest from 
 https://sites.google.com/a/chromium.org/chromedriver/downloads
 
 and dump it in Home Directory\eGangotri\chromedriver\chromedriver.exe and use this as the path in 
 java -Dwebdriver.chrome.driver....
 
 
 #Note:
 Any Folder or File which contains 'upload' or 'pre57' in there names will be automatically filtered out.
 
 This can be ingeniously used to continue uplaoding from a folder which may fail after a while. 
 By not having to change the path, only move uploaded items to a filder with name such as 'uploaded'
 containing the 'upload' text. This way they wont be picked up again
  and you dont have to make changes in your localFolders.properties file
  
  'pre57' is also ignored but that is a different usage.
  It is meant to be ignored by all accounts. 
  Except ones which will specifically pick only pre57 folder. These accounts can be configured.
  
  #Lock Screen Issue in Remote Computers
  In Lock Screen Mode the Robot APIs stop working.
  So No tabbing, pasting etc.
  If you are working remotely and your remote computer goes in Lock Screen Mode
    you have to find ways to disable Locking.
    
# Remove Lock Screen/Login Screen
https://www.isunshare.com/windows-10/2-ways-to-automatically-skip-windows-10-login-screen.html
https://smarttechnicalworld.com/gpedit-msc-windows-10-group-policy-editor/
https://www.windowscentral.com/how-stop-windows-10-showing-welcome-screen-after-feature-updates
https://www.windowscentral.com/how-disable-microsoft-edges-first-run-page-windows-10
Win-R gpedit.msc -> https://www.howtogeek.com/howto/windows-vista/disableenable-lock-workstation-functionality-windows-l/
https://www.windowscentral.com/how-disable-windows-10-lock-screen
Win-R gpedit.msc -> User Config-> Ad Tm -> Ctrl P -> Persn -> Enable Screen Saver -> Disabled

Seems gpedit.msc is ignored for Windows Home so can only use regedit
Read Note in:
Note: If you are using Windows Home, Starter, or Premium edition you will not have access to the group policy editor. Instead you should follow the instructions here.
https://www.bleepingcomputer.com/tutorials/remove-lock-option-from-ctrl-alt-delete/


https://answers.microsoft.com/en-us/windows/forum/all/is-there-a-way-we-can-turn-off-the-whole-lock/81c5c778-e247-45c3-97f1-5b65bd47ddeb
https://www.windows-commandline.com/disable-screensaver-registry-settings/

#Additional Tools to Help
1. https://www.bulkrenameutility.co.uk/Download.php
2. pdftk https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/
If you prefer the command line you can use
https://www.pdflabs.com/tools/pdftk-server/

3. Adobe Acrobat
4. https://bulkresizephotos.com/en
5. snap2html