package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Permissions.Update;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriveBasicReadQueries {
	protected Drive service;

	public DriveBasicReadQueries(Drive service) {
		this.service = service;
	}

	public List<File> listFoldersInRoot() throws IOException {
		FileList result = service.files().list()
				.setQ("'root' in parents and trashed = false")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents, mimeType)").execute();
		List<File> folders = result.getFiles();

		return folders;
	}

	public Map<File, List<File>> listChildItemsOfFolder(String searchParentFolderName) throws IOException {
		Map<File, List<File>> results = new HashMap<File, List<File>>();
		
		FileList result = service.files().list()
				.setQ(String.format(
						"name = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
						searchParentFolderName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		
		List<File> foldersMatchingSearchName = result.getFiles();
		
		if (foldersMatchingSearchName != null && !foldersMatchingSearchName.isEmpty()) {
			for (File folder : foldersMatchingSearchName) {
				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and trashed = false", folder.getId())).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents)").execute();
				
				List<File> childItems = childResult.getFiles();
				
				if (childItems != null && !childItems.isEmpty()) {
					results.put(folder, childItems);
				}
			}
		}
		
		return results;
	}
	
	public  List<File> listChildFolderItems(String parentFolderID) throws IOException {
		
	
		List<File> childItems = null;

		
		if (parentFolderID != null && !parentFolderID.isEmpty()) {
		
				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false", parentFolderID)).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents, mimeType)").execute();
				
				childItems = childResult.getFiles();
				
				
			
		}

		return childItems;
	}
	
	public  List<File> listChildFileItems(String parentFolderID) throws IOException {
		
		
		List<File> childItems = null;

		
		if (parentFolderID != null && !parentFolderID.isEmpty()) {
		
				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false", parentFolderID)).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents, mimeType)").execute();
				
				childItems = childResult.getFiles();
				
				
			
		}

		return childItems;
	}

	public Boolean changeOwnerGDriveObject(String objectid, String fromUser, String toUser) {
		
		Boolean result = false;
		
		if (objectid != null && !objectid.isEmpty()) {
			
			try {
				
				  //first give write permission for toUser to get permissionid
				   Permission newPerm = new Permission();
				   newPerm.setType("user");
				   newPerm.setRole("writer");
				   newPerm.setEmailAddress(toUser);
				   newPerm = service.permissions().create(objectid, newPerm).execute();
				   				   
				   //update permissions on object and transfer ownership
				   newPerm.setRole("owner");
				   Update update = service.permissions().update(objectid, newPerm.getId(), newPerm);
				   update.setTransferOwnership(true);
				   update.execute(); 
				   
				   result = true;
				   
				} catch (IOException e) {
					System.out.println("Exception in changeOwnerGDriveObject:"+e.getMessage());
				}
			
			
		
	}

		return result;
	}
}
