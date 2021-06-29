package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Permissions.Create;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.PermissionList;
import com.googledriveapi.model.GDriveObject;
import com.googledriveapi.model.RecursiveTransferOwnershipObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriveBasicReadQueries {
	protected Drive service;
	private static Logger logger = LoggerFactory.getLogger(DriveBasicReadQueries.class);

	public DriveBasicReadQueries(Drive service) {
		this.service = service;
	}

	public List<File> listFoldersInRoot() throws IOException {
		FileList result = service.files().list().setQ("'root' in parents and trashed = false").setSpaces("drive")
				.setFields("nextPageToken, files(id, name, parents, mimeType)").execute();
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

	public List<File> listChildFolderItems(String parentFolderID) throws IOException {

		List<File> childItems = null;

		if (parentFolderID != null && !parentFolderID.isEmpty()) {

			FileList childResult = service.files().list()
					.setQ(String.format(
							"'%s' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
							parentFolderID))
					.setSpaces("drive").setFields("nextPageToken, files(id, name, parents, mimeType)").execute();

			childItems = childResult.getFiles();

		}

		return childItems;
	}

	public List<File> listChildFileItems(String parentFolderID) throws IOException {

		List<File> childItems = null;

		if (parentFolderID != null && !parentFolderID.isEmpty()) {

			FileList childResult = service.files().list()
					.setQ(String.format(
							"'%s' in parents and mimeType != 'application/vnd.google-apps.folder' and trashed = false",
							parentFolderID))
					.setSpaces("drive").setFields("nextPageToken, files(id, name, parents, mimeType)").execute();

			childItems = childResult.getFiles();

		}

		return childItems;
	}

	public GDriveObject changeOwnerGDriveObject(GDriveObject driveObj) {

		GDriveObject resultObj = driveObj;

		try {

			// first give write permission for toUser to get permissionid
			Permission newPerm = new Permission();
			newPerm.setType("user");
			newPerm.setRole("owner");
			newPerm.setEmailAddress(driveObj.getNewOwner());

			Create createPerm = service.permissions().create(driveObj.getObjectId(), newPerm);
			createPerm.setTransferOwnership(true);
			createPerm.execute();

			BatchRequest batch = service.batch();

			RecursiveTransferOwnershipObject recObj = new RecursiveTransferOwnershipObject();
			recObj.setBatch(batch);
			recObj.setNewOwnerEmail(driveObj.getNewOwner());
			recObj.setParentFolderID(driveObj.getObjectId());

			driveObj.setOwnershipTransfered(true);

			recursiveBatchTransferOwner(recObj);
			
			

		} catch (IOException e) {
			resultObj.setOwnershipTransfered(false);
			resultObj.setOwnershipTransferErrors(e.getMessage());
			logger.error("Exception in changeOwnerGDriveObject():" + e.getMessage());
			e.printStackTrace();
		}

		logger.info("changeOwnerGDriveObject(): Status of ChangeOwnership of " + resultObj.getObjectId() + " is "
				+ resultObj.isOwnershipTransfered());
		logger.info("changeOwnerGDriveObject(): Owners of " + resultObj.getObjectId() + " are "
				+ getObjectOwners(driveObj));

		return resultObj;
	}

	private RecursiveTransferOwnershipObject recursiveBatchTransferOwner(RecursiveTransferOwnershipObject recObj)
			throws IOException {

		List<File> listFileResults = listChildFileItems(recObj.getParentFolderID());
		RecursiveTransferOwnershipObject recObjNew = recObj;
		Permission transferOwnerPermission = new Permission().setType("user").setRole("owner")
				.setEmailAddress(recObjNew.getNewOwnerEmail());
		
		Create createPerm = null;

		// First : change permission of all files under parentfolderid

		if (listFileResults != null && !listFileResults.isEmpty()) {
			for (File file : listFileResults) {

				// transfer of ownership is limited to Google Files and folders -
				// https://support.google.com/drive/answer/2494892
				if (file.getMimeType().toLowerCase().contains("application/vnd.google-apps")) {

					createPerm = service.permissions().create(file.getId(), transferOwnerPermission).setFields("id");
					createPerm.setTransferOwnership(true);
					createPerm.queue(recObjNew.getBatch(), recObjNew.getCallback());

					recObjNew.setFileCounter(recObjNew.getFileCounter() + 1);

					// execute batch if counter reaches 50
					if (recObjNew.getFileCounter() == 50) {
						recObjNew.getBatch().execute();
						recObjNew.setTotalFileCounter(recObjNew.getFileCounter() + recObjNew.getTotalFileCounter());
						recObjNew.setFileCounter(0);
					}
				}

			}
		}

		// Second : change permission of all folders under parentfolderid

		List<File> listFolderResults = listChildFolderItems(recObjNew.getParentFolderID());

		if (listFolderResults == null || listFolderResults.isEmpty()) {

			// recursion exit
			if (recObjNew.getBatch().size() > 0) {
				recObjNew.getBatch().execute();// recursion exit
				recObjNew.setTotalFileCounter(recObjNew.getTotalFileCounter() + recObjNew.getFileCounter());
				recObjNew.setTotalFolderCounter(recObjNew.getTotalFolderCounter() + recObjNew.getFolderCounter());
			}

			logger.info("RecursiveTransferOwnershipObject(): Total Files Modified :" + recObjNew.getTotalFileCounter());
			logger.info(
					"RecursiveTransferOwnershipObject(): Total Folders Modified :" + recObjNew.getTotalFolderCounter());

		} else {

			for (File folder : listFolderResults) {
			
				createPerm = service.permissions().create(folder.getId(), transferOwnerPermission).setFields("id");
				createPerm.setTransferOwnership(true);
				createPerm.queue(recObjNew.getBatch(), recObjNew.getCallback());

				recObjNew.setFolderCounter(recObjNew.getFolderCounter() + 1);

				// execute batch if counter reaches 50
				if (recObjNew.getFolderCounter() == 50) {
					recObjNew.getBatch().execute();
					recObjNew.setTotalFolderCounter(recObjNew.getFolderCounter() + recObjNew.getTotalFolderCounter());
					recObjNew.setFolderCounter(0);
				}

				recObjNew.setParentFolderID(folder.getId());

				return recursiveBatchTransferOwner(recObjNew);

			}

		}
		return recObjNew;

	}

	public ArrayList<String> getObjectOwners(GDriveObject driveObj) {

		PermissionList permissions;
		ArrayList<String> owners = null;
		try {
			permissions = service.permissions().list(driveObj.getObjectId())
					.setFields("permissions(emailAddress,id,kind,role,type)").execute();

			List<Permission> permissionList = permissions.getPermissions();

			owners = new ArrayList<String>();

			if (permissionList.isEmpty()) {
				logger.info("getObjectOwners(): No Files or Permissions found for id :" + driveObj.getObjectId());

			} else {
				for (Permission permission : permissionList) {

					if (permission.getRole().toLowerCase().equals("owner")) {

						owners.add(permission.getEmailAddress());
					}

				}
			}

		} catch (IOException e) {
			logger.error("Exception in getObjectOwners() :" + driveObj.getObjectId());
			e.printStackTrace();

		} finally {
			return owners;

		}

	}
}
