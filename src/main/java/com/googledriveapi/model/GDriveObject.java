package com.googledriveapi.model;

import java.util.ArrayList;

public class GDriveObject {
	
	private String objectId;
	private String objectName;
	private boolean ownershipTransfered = false;
	private String newOwner;
	private String requestingOwner;
	private ArrayList<String> owners;
	private String mimeType;
	private String ownershipTransferErrors;
	
	
	
	public String getOwnershipTransferErrors() {
		return ownershipTransferErrors;
	}
	public void setOwnershipTransferErrors(String ownershipTransferErrors) {
		this.ownershipTransferErrors = ownershipTransferErrors;
	}
	public String getObjectId() {
		return objectId;
	}
	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public boolean isOwnershipTransfered() {
		return ownershipTransfered;
	}
	public void setOwnershipTransfered(boolean ownershipTransfered) {
		this.ownershipTransfered = ownershipTransfered;
	}
	public String getNewOwner() {
		return newOwner;
	}
	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}
	
	
	public String getRequestingOwner() {
		return requestingOwner;
	}
	public void setRequestingOwner(String requestingOwner) {
		this.requestingOwner = requestingOwner;
	}
	public ArrayList<String> getOwners() {
		return owners;
	}
	public void setOwners(ArrayList<String> owners) {
		this.owners = owners;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	

}
