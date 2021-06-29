package com.googledriveapi.model;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.drive.model.Permission;

public class RecursiveTransferOwnershipObject {
	
	private static Logger logger = LoggerFactory.getLogger(RecursiveTransferOwnershipObject.class);

	
	private String parentFolderID;
	private String newOwnerEmail;
	private BatchRequest batch;
	private int folderCounter = 0;
	private int fileCounter = 0;
	private int totalFolderCounter = 0;
	private int totalFileCounter = 0;

	JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
		@Override
		public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) throws IOException {
			// Handle error
			logger.error("changeOwnerGDriveObject(): Failed batch with error :" + e.getMessage());
		}

		@Override
		public void onSuccess(Permission permission, HttpHeaders responseHeaders) throws IOException {
			logger.info("changeOwnerGDriveObject(): Successfully Transfered Ownership in Batch");
		}
	};
	
	public int getFolderCounter() {
		return folderCounter;
	}
	public void setFolderCounter(int folderCounter) {
		this.folderCounter = folderCounter;
	}
	public int getFileCounter() {
		return fileCounter;
	}
	public void setFileCounter(int fileCounter) {
		this.fileCounter = fileCounter;
	}
	public String getParentFolderID() {
		return parentFolderID;
	}
	public void setParentFolderID(String parentFolderID) {
		this.parentFolderID = parentFolderID;
	}
	public String getNewOwnerEmail() {
		return newOwnerEmail;
	}
	public void setNewOwnerEmail(String newOwnerEmail) {
		this.newOwnerEmail = newOwnerEmail;
	}
	public BatchRequest getBatch() {
		return batch;
	}
	public void setBatch(BatchRequest batch) {
		this.batch = batch;
	}
	
	public int getTotalFolderCounter() {
		return totalFolderCounter;
	}
	public void setTotalFolderCounter(int totalFolderCounter) {
		this.totalFolderCounter = totalFolderCounter;
	}
	public int getTotalFileCounter() {
		return totalFileCounter;
	}
	public void setTotalFileCounter(int totalFileCounter) {
		this.totalFileCounter = totalFileCounter;
	}
	public JsonBatchCallback<Permission> getCallback() {
		return callback;
	}
	public void setCallback(JsonBatchCallback<Permission> callback) {
		this.callback = callback;
	}
	
	
}
