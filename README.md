# GDM - Google Drive Manager
This application provides the ability to navigate Google Drive folders or files for a given Google Account. It has a very basic UI which is capable of showing Google Drive Directory Structure. Unlike Google Drive Web application, this application shows the entire directory structure on a single page.

###### Limitations : The UI is built to handle at most 10 drilled-down directories and the presentation will not be friendly if Google Drive has deeper directory structure.

## Technologies used 
Java, JavaScript, JQuery, JSP, Maven, Spring, Tomcat, Google API SDK

## How to Install
This project builds a standalone Spring Boot Application. Dowload the project and build the code using Maven. For convenience, the repo contains a war file that can be downloaded and installed on tomcat or run individually at command-line like a jar file using 
java -jar GDM-1.0.war 

After installing, the webpage for this application can be accessed with this url - http://localhost:9080/GDM/home

![image](https://user-images.githubusercontent.com/69950262/123526490-c791f700-d6c7-11eb-8f6c-430f11ada613.png)

Screenshot after search
![image](https://user-images.githubusercontent.com/69950262/123526816-e09ba780-d6c9-11eb-8fc6-5613ecccf344.png)

### User Consent
To search a user's Google Drive folder, they need to provide their consent which is achieved using OAuth and this application will automatically open the browser and ask the user to sign-in to provide their permission. The OAuth tokens are stored in /tokens directory


## Pre-requisites 
## Google Accounts
Create a new Google account or use an exisitng one for testing this application.

### Google Cloud Setup
1. Visit https://cloud.google.com/console/start/api?id=drive
2. The above link will ask to create a project, follow prompts to create a new project.
3. OAuth consent screen
    User Type > External
    Create
    App name > GDriveNavigation (or any name you wish)
    Support Email > xxx
    Developer contact information > xxx
    Save and Continue

3. Go to Credentials on left navigation
   Create OAuth client ID
   Application Type : Web Application
   Name : GoogleDriveAPINavigation
   Authorized Redirect URIs : http://localhost:8888/Callback 
   Download json as credentials.json and place under the project /src/main/resources folder
  
4. Go to "OAuth Content Screen" and add Test Users who will have access to this application. This app will allow only these users to view their Google Drive content.

## References / Credits
Google Drive API SDK - https://developers.google.com/drive/api/v3/about-sdk

## Tests Cases
Test cases are not included at this time.

## What is working in this release?
- Search by Google account
- Step-by-step navigation into folders and their content

## What is not available in this release?
- file/folder transfer ownership is not working. It errors out with 403 status code.

## Troubleshooting
During OAuth2 consent from user, if the browser window is closed without getting proper response, the application will stop running with Exception "java.io.IOException: java.net.BindException: Address already in use (Bind failed)" 
##### _Solution : Kill the PID on port 9080 (run lsof -i:9080 to get the PID and use kill -9 command to stop the process)_

Delete OAuth tokens for users
##### _Solution : Navigate to /tokens and delete all files in this directory_

Log/console has exception - Exception in changeOwnerGDriveObject:403 Forbidden
##### _Solution : Ownership transfer functionality is still under development. This exception shows up when 'Change Owner' icon is clicked on the UI_
