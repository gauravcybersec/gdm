package com.googledriveapi.runner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.googledriveapi.model.GDriveObject;
import com.googledriveapi.utility.DriveQueries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Configuration
public class UIHandler implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(UIHandler.class);

	@Override
	public void run(String... strings) throws Exception {

	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView homePage(HttpServletResponse resp) {

		ModelAndView obj = new ModelAndView();

		obj.setViewName("home");

		return obj;

	}

	/*
	 * @RequestMapping(value="/fetch",method=RequestMethod.POST,consumes=MediaType.
	 * APPLICATION_JSON_VALUE) public@ResponseBody String
	 * userLogin(HttpServletRequest req){
	 * 
	 * return "success";
	 * 
	 * }
	 */

	@RequestMapping(value = "/fetch", method = RequestMethod.POST)
	public void fetchGDriveRootFolder(HttpServletRequest req, HttpServletResponse resp) {

		GDriveObject driveObj = new GDriveObject();

		try {

			JSONObject reqJSON = getJSON(req);
			
			if (reqJSON != null) {
				
				driveObj = populateGDriveObjFromJSON(reqJSON);
				
				logger.info("fetchGDriveRootFolder()" + reqJSON.toString());

				
			}


			// validate and sanitize inputs (no special characters, code etc)
			// create fileModel and add params to it
			// validateInputFields(reqJSON);

			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json");

			DriveQueries obj = new DriveQueries(driveObj.getRequestingOwner());
			PrintWriter out = resp.getWriter();
			resp.setStatus(HttpServletResponse.SC_OK);
			String root = reqJSON.get("isroot").toString();

			// root folder
			if (root == null || root.length() == 0 || root.equalsIgnoreCase("1")) {

				JSONObject result = obj.getRootFolderStructureInJSON();

				if (result.isEmpty()) {
					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

				}
				out.print(result);

			} else {// any other folder but root
				if (driveObj.getObjectId() != null && driveObj.getObjectId().length() > 0) {

					JSONObject result = obj.listChildItemsOfFolderID(driveObj.getObjectId(), driveObj.getMimeType());
					if (result.isEmpty()) {
						resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

					}
					out.print(result);

				} else {
					resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					logger.error("Missing objectid value");

				}
			}

			out.flush();

		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

			logger.error("Exception in fetchGDriveRootFolder()" + e.getMessage());
		}

	}

	@RequestMapping(value = "/changeOwner", method = RequestMethod.POST)
	public void changeOwnerGDriveObject(HttpServletRequest req, HttpServletResponse resp) {
		GDriveObject driveObj = new GDriveObject();

		try {
			JSONObject reqJSON = getJSON(req);

			if (reqJSON != null) {
				
				driveObj = populateGDriveObjFromJSON(reqJSON);
				
				logger.info("changeOwnerGDriveObject()" + reqJSON.toString());

				
			}

			resp.setCharacterEncoding("UTF-8");
			resp.setContentType("application/json");

			DriveQueries obj = new DriveQueries(driveObj.getRequestingOwner());
			PrintWriter out = resp.getWriter();

			if (driveObj.getObjectId() != null && driveObj.getObjectId().length() > 0) {

				GDriveObject driveObjResult = obj.changeOwnerGDriveObject(driveObj);
				if (driveObjResult.isOwnershipTransfered()) {
					// Reply with 204 if changing ownership was successful

					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

				} else {

					// Reply with 503 if changing ownership failed
					resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
					resp.setContentType("text/html");
					out.print(driveObjResult.getOwnershipTransferErrors());

				}

			} else {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				logger.error("Missing objectid value");

			}

			out.flush();

		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

			logger.error("Exception in changeOwnerGDriveObject()" + e.getMessage());
		}
	}

	private GDriveObject populateGDriveObjFromJSON(JSONObject reqJSON) {
		
		GDriveObject driveObj = new GDriveObject();
		
		if(reqJSON.get("requestingowner")!=null)
			driveObj.setRequestingOwner(reqJSON.get("requestingowner").toString());
		
		if(reqJSON.get("objectid")!=null)
			driveObj.setObjectId(reqJSON.get("objectid").toString());
		
		if(reqJSON.get("newowner")!=null)
			driveObj.setNewOwner(reqJSON.get("newowner").toString());
		
		if(reqJSON.get("objmimetype")!=null)
			driveObj.setMimeType(reqJSON.get("objmimetype").toString());

		if(reqJSON.get("objectname")!=null)
			driveObj.setObjectName(reqJSON.get("objectname").toString());

		
		return driveObj;
	}

	private JSONObject getJSON(HttpServletRequest request) {

		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) {
			/* report an error */ }

		try {
			JSONParser parser = new JSONParser();

			return (JSONObject) parser.parse(jb.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
