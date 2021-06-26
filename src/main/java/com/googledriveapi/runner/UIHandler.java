package com.googledriveapi.runner;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.googledriveapi.utility.DriveQueries;

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

		logger.debug("homePage() START");

		ModelAndView obj = new ModelAndView();

		obj.setViewName("home");

		logger.debug("homePage() END");

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

		logger.debug("fetchGDriveRootFolder() START");
		logger.debug("fetchGDriveRootFolder(); User is " + req.getParameter("user"));
		logger.debug("fetchGDriveRootFolder(); mimeType is " + req.getParameter("mimeType"));

		String user = req.getParameter("user");

		String objectid = req.getParameter("objectid");

		String root = req.getParameter("root");

		String mimeType = req.getParameter("mimeType");

		// validate and sanitize inputs (no special characters, code etc)
		// create fileModel and add params to it
		// validateInputFields(fileModel);

		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		try {
			DriveQueries obj = new DriveQueries(req.getParameter("user"));
			PrintWriter out = resp.getWriter();
			resp.setStatus(HttpServletResponse.SC_OK);

			// root folder
			if (root == null || root.length() == 0 || root.equalsIgnoreCase("1")) {

				JSONObject result = obj.getRootFolderStructureInJSON();

				if (result.isEmpty()) {
					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

				}
				out.print(result);

			} else {
				if (objectid != null && objectid.length() > 0) {

					JSONObject result = obj.listChildItemsOfFolderID(objectid, mimeType);
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

			logger.error(e.getMessage());
		}

	}

	@RequestMapping(value = "/changeOwner", method = RequestMethod.POST)
	public void changeOwnerGDriveObject(HttpServletRequest req, HttpServletResponse resp) {

		String fromUser = req.getParameter("fromUser");

		String objectid = req.getParameter("objectid");

		String toUser = req.getParameter("toUser");

		// validate and sanitize inputs (no special characters, code etc)
		// create fileModel and add params to it
		// validateInputFields(fileModel);

		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");

		try {
			DriveQueries obj = new DriveQueries(fromUser);
			PrintWriter out = resp.getWriter();

			if (objectid != null && objectid.length() > 0) {

				Boolean result = obj.changeOwnerGDriveObject(objectid, fromUser, toUser);
				if (result) {
					// send 204 if changing ownership was successful

					resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

				} else {

					// send 503 if changing ownership failed
					resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

				}

			} else {
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				logger.error("Missing objectid value");

			}

			out.flush();

		} catch (IOException e) {
			resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);

			logger.error(e.getMessage());
		}

	}

}
