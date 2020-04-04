#Instructions to use

- Must have Java 8 installed and in PATH also

- Dump this entire Folder into your Home Directory ( typically in Windows C:\User\<windows_login_id>)

- change the userids and passwords in archiveLogins.properties.

- Dump the Chromedriver folder as is into your Home Directory/eGangotri also
//the chromedriver folder here has the Windows Version. Get appropriate version for your OS from
//http://chromedriver.chromium.org/downloads

- Run project using the following command from command prompt:

java -jar eGangotri.jar 

 - java -Dwebdriver.chrome.driver=<PATH_TO_CHROMEDRIVER>chromedriver\chromedriver.exe -jar <PATH_TO_EGANGOTRI_JAR>/eGangotri.jar PRFL1 PRFL2
  
  Example:
   java -Dwebdriver.chrome.driver=C:\Users\user\eGangotri\chromedriver\chromedriver.exe -jar C:\ws\egangotri\latestJarForUse\eGangotri.jar PRFL1 PRFL2
  
where PRFL1 etc are the profiles you have configured

