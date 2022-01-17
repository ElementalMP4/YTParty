package main.java.de.voidtech.ytparty.api.http;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.service.ConfigService;
import main.java.de.voidtech.ytparty.service.FileReader;
import main.java.de.voidtech.ytparty.service.PartyService;

@RestController
public class HttpRestController {
	
	@Autowired
	private FileReader fileReader;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private PartyService partyService;
	
	@RequestMapping(value = "/", produces = "text/html")
	public String indexRoute() {
		return fileReader.getTextFileContents("html/index.html");
	}
	
	@RequestMapping(value = "/index.html", produces = "text/html")
	public String indexHtmlRoute() {
		return fileReader.getTextFileContents("html/index.html");
	}
	
	@RequestMapping(value = "/terms.html", produces = "text/html")
	public String termsRoute() {
		return fileReader.getTextFileContents("html/terms.html");
	}
  
	@RequestMapping(value = "/primarystyle.css", produces = "text/css")
	public String primaryStyleRoute() {
		return fileReader.getTextFileContents("css/primarystyle.css");
	}
	
	@RequestMapping(value = "/modal.css", produces = "text/css")
	public String modalStyleRoute() {
		return fileReader.getTextFileContents("css/modal.css");
	}
	
	@RequestMapping(value = "/playerstyle.css", produces = "text/css")
	public String playerStyleRoute() {
		return fileReader.getTextFileContents("css/playerstyle.css");
	}
	
	@RequestMapping(value = "/modal.js", produces = "application/javascript")
	public String modalScriptRoute() {
		return fileReader.getTextFileContents("js/modal.js");
	}
	
	@RequestMapping(value = "/player.js", produces = "application/javascript")
	public String playerScriptRoute() {
		return fileReader.getTextFileContents("js/player.js");
	}
	
	@RequestMapping(value = "/favicon.png", produces = "image/png")
	public byte[] faviconRoute() {
		return fileReader.getBinaryFileContents("img/favicon.png");
	}
	
	@RequestMapping(value = "/favicon.ico", produces = "image/png")
	public byte[] alternateFaviconRoute() {
		return fileReader.getBinaryFileContents("img/favicon.png");
	}
	
	@RequestMapping(value = "/waves.svg", produces = "image/svg+xml")
	public byte[] wavesRoute() {
		return fileReader.getBinaryFileContents("img/waves.svg");
	}
	
	@RequestMapping(value = "/deadcat.gif", produces = {"image/gif"})
	public byte[] catGifRoute() {
		return fileReader.getBinaryFileContents("img/deadcat.gif");
	}
	
	@RequestMapping(value = "/login.html", produces = "text/html")
	public String loginRoute() {
		return fileReader.getTextFileContents("html/login.html");
	}
	
	@RequestMapping(value = "/ping.html", produces = "text/html")
	public String pingRoute() {
		return fileReader.getTextFileContents("html/ping.html");
	}
	
	@RequestMapping(value = "/signup.html", produces = "text/html")
	public String signupRoute() {
		return fileReader.getTextFileContents("html/signup.html");
	}
	
	@RequestMapping(value = "/about.html", produces = "text/html")
	public String aboutRoute() {
		return fileReader.getTextFileContents("html/about.html");
	}
	
	@RequestMapping(value = "/signup.js", produces = "application/javascript")
	public String signupScriptRoute() {
		return fileReader.getTextFileContents("js/signup.js");
	}

	@RequestMapping(value = "/home.js", produces = "application/javascript")
	public String homeScriptRoute() {
		return fileReader.getTextFileContents("js/home.js");
	}
	
	@RequestMapping(value = "/login.js", produces = "application/javascript")
	public String signInScriptRoute() {
		return fileReader.getTextFileContents("js/login.js");
	}
	
	@RequestMapping(value = "/accountsettings.html", produces = "text/html")
	public String accountRoute() {
		return fileReader.getTextFileContents("html/accountsettings.html");
	}
	
	@RequestMapping(value = "/settings.js", produces = "application/javascript")
	public String settingsScriptRoute() {
		return fileReader.getTextFileContents("js/settings.js");
	}
	
	@RequestMapping(value = "/home.html", produces = "text/html")
	public String homeRoute() {
		return fileReader.getTextFileContents("html/home.html");
	}
	
	@RequestMapping(value = "/createroom.js", produces = "application/javascript")
	public String createRoomScriptRoute() {
		return fileReader.getTextFileContents("js/createroom.js");
	}
	
	@RequestMapping(value = "/createroom.html", produces = "text/html")
	public String createRoomRoute() {
		return fileReader.getTextFileContents("html/createroom.html");
	}
	
	@RequestMapping(value = "/doesonestillequalone.html", produces = "text/html")
	public String easterEggRoute() {
		return fileReader.getTextFileContents("/html/doesonestillequalone.html");
	}
	
	@RequestMapping(value = "/resetpassword.html", produces = "text/html")
	public String passwordResetRoute() {
		return fileReader.getTextFileContents("/html/resetpassword.html");
	}
	
	@RequestMapping(value = "/forgotpassword.html", produces = "text/html")
	public String forgotPasswordRoute() {
		return fileReader.getTextFileContents("/html/forgotpassword.html");
	}
	
	@RequestMapping(value = "/resetpassword.js", produces = "application/javascript")
	public String passwordResetScriptRoute() {
		return fileReader.getTextFileContents("/js/resetpassword.js");
	}
	
	@RequestMapping(value = "/forgotpassword.js", produces = "application/javascript")
	public String forgotPasswordScriptRoute() {
		return fileReader.getTextFileContents("/js/forgotpassword.js");
	}
	
	@RequestMapping(value = "/particle-config.json")
	public String particleConfigRoute() {
		String mode = configService.getParticleMode();
		return fileReader.getTextFileContents("/particle-config-" + mode + ".json");
	}	
	
	private String editPartyMetaTag(Party party) {
		String partyPage = fileReader.getTextFileContents("html/player.html");
		Document partyDoc = Jsoup.parse(partyPage);
		partyDoc.select("head > meta:nth-child(11)").attr("content", party.getOwnerName() + "'s room!");
		return partyDoc.toString();
	}
	
	@RequestMapping(value = "/player.html", produces = "text/html")
	public String playerRoute(@RequestParam(required = false) String roomID) {
		if (roomID == null) return fileReader.getTextFileContents("html/noroom.html");
		else {
			Party party = partyService.getParty(roomID);
			if (party == null) return fileReader.getTextFileContents("html/noroom.html");
			else return editPartyMetaTag(party); 
		}
	}
	
	@RequestMapping(value = "/beansandthreads", produces = "text/html")
	public String statsRoute() {
		Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
		List<String> threadList = new ArrayList<String>();
		threadSet.forEach(thread -> {
			threadList.add(thread.getName()); 
		});
		String response =
		 "Thread Count: " + Thread.activeCount()
		 + "<br>Threads:<br>" + String.join("<br>", threadList)
		 + "<br><br>Bean Count: " + context.getBeanDefinitionCount()
		 + "<br>Beans:<br>" + String.join("<br>", context.getBeanDefinitionNames());
		return response;
	}
}