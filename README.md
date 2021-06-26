GDM - Google Drive Manager
This tool provides the ability to Navigate into Google Drive folders or files for a given Google Account. It has a very basic UI which is capable of showing Google Drive Directory Structure.

Technologies used : Java, JavaScript, JQuery, JSP, Maven, Spring, Tomcat, Google API SDK

Add a Table of Contents (Optional)
If your README is very long, you might want to add a table of contents to make it easy for users to find what they need. It helps them navigate to different parts of the file.

How to Install
This project builds a standalone Spring Boot Application which can be run 
Copy/Download Git Repo and build the code using Maven. For convenience, the repo contains a war file that can be downloaded and installed on tomcat or run individually using 
java -jar GDM-1.0.war 

The webpage can be accessed with the url - http://localhost:9080/GDM/home

![image](https://user-images.githubusercontent.com/69950262/123526490-c791f700-d6c7-11eb-8f6c-430f11ada613.png)
![image](https://user-images.githubusercontent.com/69950262/123526816-e09ba780-d6c9-11eb-8fc6-5613ecccf344.png)


Pre-requisites - Google Cloud Setup
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

References / Credits
Google Drive API SDK - https://developers.google.com/drive/api/v3/about-sdk

Tests Cases
Test cases are not included at this time.

What is working in this release?
- Search by Google account
- Step-by-step navigation into folders and their content

What is not available in this release?
- file/folder transfer ownership is not working. It errors out with 403 status code.
