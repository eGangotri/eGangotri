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
   java -Dwebdriver.chrome.driver=C:\Users\user\chromedriver\chromedriver.exe -jar =C:\Users\user\eGangotri\eGangotri.jar PRFL1 PRFL2
  
  ** PRFL1 PRFL2 -> Read about how to setup Profile in the Main README
  https://github.com/eGangotri/eGangotri/blob/master/README.md
 
 In case Chromedriver gets outdated, get latest from 
 https://sites.google.com/a/chromium.org/chromedriver/downloads
 
 and dump it in Home Directory\chromedriver\chromedriver.exe and use this as the path in 
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
    you have to find ways to disable Locking