package com.googledriveapi.utility;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.samples.drive.cmdline.queries.DriveBasicReadQueries;

import org.apache.commons.collections4.CollectionUtils;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.naming.ldap.LdapReferralException;

public class DriveQueries {

	private static Logger logger = LoggerFactory.getLogger(DriveQueries.class);

	private final String APPLICATION_NAME = "GDM";
	private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private final String TOKENS_DIRECTORY_PATH = "tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	private final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
	private final String CREDENTIALS_FILE_PATH = "/credentials.json";
	private Drive service;
	private String user;

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = DriveQueries.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();

		// setting this port static may not work for multiple users trying at the same
		// time; no scalable
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

		AuthorizationCodeInstalledApp objAuthorizationCodeInstalledApp = new AuthorizationCodeInstalledApp(flow,
				receiver);

		Credential objCreds = objAuthorizationCodeInstalledApp.authorize(user);

		return objCreds;
	}

	public DriveQueries(String user) {
		this.user = user;

		try {
			// Build a new authorized API client service.
			final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME).build();

		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private JSONObject getChildItemsInJSON() {

		JSONObject folderStructure = null;
		DriveBasicReadQueries query = new DriveBasicReadQueries(service);

		try {
			List<File> listFoldersInRootResults = query.listFoldersInRoot();
			JSONObject jsonFolderList = outputListOfFileResults(listFoldersInRootResults);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return folderStructure;

	}

	public JSONObject getRootFolderStructureInJSON() {

		JSONObject folderStructure = null;
		DriveBasicReadQueries query = new DriveBasicReadQueries(service);

		try {
			List<File> listFoldersInRootResults = query.listFoldersInRoot();
			folderStructure = outputListOfFileResults(listFoldersInRootResults);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return folderStructure;

	}

	public JSONObject listChildItemsOfFolderID(String parentFolderID, String mimeType) {

		JSONObject jsonFolderList = null;
		DriveBasicReadQueries query = new DriveBasicReadQueries(service);

		try {

			List<File> listFolderResults = null;

			if (mimeType != null && mimeType.equalsIgnoreCase("folder")) {
				listFolderResults = query.listChildFolderItems(parentFolderID);
			} else {
				listFolderResults = query.listChildFileItems(parentFolderID);

			}
			jsonFolderList = outputListOfFileResults(listFolderResults);
			System.out.println(jsonFolderList.toJSONString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonFolderList;

	}

	private JSONObject outputListOfFileResults(List<File> files) {
		JSONObject jsonList = new JSONObject();
		JSONObject jsonFileProps = null;

		Map<String, JSONObject> hmAllObjects = new HashMap<String, JSONObject>();

		if (files == null || files.isEmpty()) {
			logger.info("No results.");
		} else {

			for (File file : files) {
				logger.info(String.format("file.getName: %s getId: %s", file.getName(), file.getId()));

				jsonFileProps = new JSONObject();

				jsonFileProps.put("id", file.getId());
				jsonFileProps.put("createtime", file.getCreatedTime());
				jsonFileProps.put("lastmodifyuser", file.getLastModifyingUser());
				jsonFileProps.put("name", file.getName());
				if (file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")) {

					jsonFileProps.put("type", "folder");
				} else {

					jsonFileProps.put("type", "file");

				}

				jsonList.put(file.getId(), jsonFileProps);
			}

		}

		return jsonList;
	}

	private HashMap<String, JSONObject> sortByValue(HashMap<String, JSONObject> hm) {
		// Create a list from elements of HashMap
		List<Map.Entry<String, JSONObject>> list = new LinkedList<Map.Entry<String, JSONObject>>(hm.entrySet());

		// Sort the list
		Collections.sort(list, new Comparator<Map.Entry<String, JSONObject>>() {
			public int compare(Map.Entry<String, JSONObject> o1, Map.Entry<String, JSONObject> o2) {
				JSONObject obj1 = o1.getValue();
				JSONObject obj2 = o2.getValue();

				return (obj1.get("type").toString()).compareTo(obj2.get("type").toString());
			}
		});

		// put data from sorted list to hashmap
		HashMap<String, JSONObject> temp = new LinkedHashMap<String, JSONObject>();
		for (Map.Entry<String, JSONObject> aa : list) {
			temp.put(aa.getKey(), aa.getValue());
		}
		return temp;
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		DriveQueries obj = new DriveQueries("interviewtest1062521@gmail.com");
		// obj.getRootFolderStructureInJSON();
		obj.changeOwnerGDriveObject("1-M0wQEDW8Ux6WPQ2Xi1g3rBbJrEWVZ0D", "", "interviewtest2062521@gmail.com");
	}

	public Boolean changeOwnerGDriveObject(String objectid, String fromUser, String toUser) {
		Boolean result = false;
		DriveBasicReadQueries query = new DriveBasicReadQueries(service);

		result = query.changeOwnerGDriveObject(objectid, fromUser, toUser);
		System.out.println("changeOwnerGDriveObject():" + result);

		return result;
	}
}
