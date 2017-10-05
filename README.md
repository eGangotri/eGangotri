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
 
 