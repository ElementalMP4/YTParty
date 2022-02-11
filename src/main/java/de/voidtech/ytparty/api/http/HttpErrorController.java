package main.java.de.voidtech.ytparty.api.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpErrorController implements ErrorController { //This class is an implementation of the existing error controller

	private static final Logger LOGGER = Logger.getLogger(HttpErrorController.class.getName()); //We need the logger to record errors
    private static final String PATH = "/error"; //This tells tomcat where to find the error page

    @RequestMapping(value = PATH) //Set the request mapping to the previously defined route
    public String error(HttpServletRequest request) {
    	Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE); //Get the error code
    	String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE); //Get the error message
    	String url = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI); //Get the URL that caused the error
    	
    	if (status != null) { //If we know the error, we can give a specific response...
            int statusCode = Integer.valueOf(status.toString());
            LOGGER.log(Level.WARNING, "Error " + statusCode + " occurred on route '" + url + "' " + message); //Log the error
            return formatErrorPage(statusCode); //Send a specific error page
    	} //...Otherwise, we will have to hand out a generic response.
    	LOGGER.log(Level.WARNING, "Unknown error occurred on route '" + url + "' " + message); //Log the error
    	return formatErrorPage(0); //Send a generic error page
    }

    public String getErrorPath() { //Tell tomcat where to find this error route
        return PATH;
    }
    
    private String formatErrorPage(int statusCode) { //Create the error page
    	return String.format("<html><title>YTParty Error</title><center><h1>There seems to be a problem...</h1><hr />Error: %s"
    			+ "<br></br><img src='/deadcat.gif'><br></br><a href='https://http.cat/%s'>Click here to find out what this error means</a></center></html>",
    			statusCode == 0 ? "Unknown Error" : statusCode, statusCode == 0 ? 404 : statusCode); //If we do not know the code, send 404
    }
}