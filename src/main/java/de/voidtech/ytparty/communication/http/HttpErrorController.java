package main.java.de.voidtech.ytparty.communication.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HttpErrorController implements ErrorController{

	private static final Logger LOGGER = Logger.getLogger(HttpErrorController.class.getName());
    private static final String PATH = "/error";

    @RequestMapping(value = PATH)
    public String error(HttpServletRequest request) {
    	Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    	String url = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    	
    	if (status != null) {
            int statusCode = Integer.valueOf(status.toString());
            LOGGER.log(Level.WARNING, "Error " + statusCode + " occurred on route '" + url + "'");
            return formatErrorPage(statusCode);
    	}
    	LOGGER.log(Level.WARNING, "Unknown error occurred on route '" + url + "'");
    	return formatErrorPage(0);
    }

    @Override
    public String getErrorPath() {
        return PATH;
    }
    
    private String formatErrorPage(int statusCode) {
    	return String.format("<html><title>YouTube Party Error</title><center><h1>YouTube Party</h1><hr />Server Error: %s"
    			+ "<br></br><img src='/deadcat.gif'></center></html>", statusCode == 0 ? "Unknown Error" : statusCode);
    }
}