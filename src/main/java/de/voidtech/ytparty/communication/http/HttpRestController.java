package main.java.de.voidtech.ytparty.communication.http;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import main.java.de.voidtech.ytparty.service.FileReader;
import main.java.de.voidtech.ytparty.service.UserService;

@RestController
public class HttpRestController {
	
	@Autowired
	private FileReader fileReader;
	
	@Autowired
	private UserService userService;

	@RequestMapping(value = "/")
	public String indexRoute() {
		return fileReader.getTextFileContents("html/index.html");
	}
	
	@RequestMapping(value = "/terms.html")
	public String termsRoute() {
		return fileReader.getTextFileContents("html/terms.html");
	}
  
	@RequestMapping(value = "/primarystyle.css")
	public String primaryStyleRoute() {
		return fileReader.getTextFileContents("css/primarystyle.css");
	}
	
	@RequestMapping(value = "/playerstyle.css")
	public String playerStyleRoute() {
		return fileReader.getTextFileContents("css/playerstyle.css");
	}
	
	@RequestMapping(value = "/player.html")
	public String playerRoute() {
		return fileReader.getTextFileContents("html/player.html");
	}
	
	@RequestMapping(value = "/player.js")
	public String playerScriptRoute() {
		return fileReader.getTextFileContents("js/player.js");
	}
	
	@RequestMapping(value = "/favicon.png", produces = {"image/png"})
	public byte[] faviconRoute() {
		return fileReader.getBinaryFileContents("img/favicon.png");
	}
	
	@RequestMapping(value = "/deadcat.gif", produces = {"image/gif"})
	public byte[] catGifRoute() {
		return fileReader.getBinaryFileContents("img/deadcat.gif");
	}
	
	@RequestMapping(value = "/login.html")
	public String loginRoute() {
		return fileReader.getTextFileContents("html/login.html");
	}
	
	@RequestMapping(value = "/signup.html")
	public String signupRoute() {
		return fileReader.getTextFileContents("html/signup.html");
	}
	
	@RequestMapping(value = "/about.html")
	public String aboutRoute() {
		return fileReader.getTextFileContents("html/about.html");
	}
	
	@RequestMapping(value = "/signup.js")
	public String signupScriptRoute() {
		return fileReader.getTextFileContents("js/signup.js");
	}

	@RequestMapping(value = "/login.js")
	public String signInScriptRoute() {
		return fileReader.getTextFileContents("js/login.js");
	}
	
	@RequestMapping(value = "/accountsettings.html")
	public String accountRoute() {
		return fileReader.getTextFileContents("html/accountsettings.html");
	}
	
	@RequestMapping(value = "/settings.js")
	public String settingsScriptRoute() {
		return fileReader.getTextFileContents("js/settings.js");
	}
	
	@RequestMapping(value = "/home.html")
	public String homeRoute() {
		return fileReader.getTextFileContents("html/home.html");
	}
	
	@RequestMapping(value = "/createroom.js")
	public String createRoomScriptRoute() {
		return fileReader.getTextFileContents("js/createroom.js");
	}
	
	@RequestMapping(value = "/createroom.html")
	public String createRoomRoute() {
		return fileReader.getTextFileContents("html/createroom.html");
	}
	
	@RequestMapping(value = "/joinroom.js")
	public String joinRoomScriptRoute() {
		return fileReader.getTextFileContents("js/joinroom.js");
	}
	
	@RequestMapping(value = "/joinroom.html")
	public String joinRoomRoute() {
		return fileReader.getTextFileContents("html/joinroom.html");
	}
	
	@RequestMapping(value = "/doesonestillequalone.html")
	public String easterEggRoute() {
		return fileReader.getTextFileContents("/html/doesonestillequalone.html");
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public String signupUser(@RequestBody String content) {
		JSONObject parameters = new JSONObject(content);
		return userService.createUser(parameters);
	}
}