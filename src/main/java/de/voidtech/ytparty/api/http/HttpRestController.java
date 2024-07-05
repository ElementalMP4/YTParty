package main.java.de.voidtech.ytparty.api.http;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import main.java.de.voidtech.ytparty.entities.ephemeral.Party;
import main.java.de.voidtech.ytparty.service.ConfigService;
import main.java.de.voidtech.ytparty.service.FileReader;
import main.java.de.voidtech.ytparty.service.PartyService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class HttpRestController {
	
	@Autowired
	private FileReader fileReader;
	
	@Autowired
	private ConfigService configService;
	
	@Autowired
	private PartyService partyService;

	@GetMapping("/")
	public RedirectView redirectToIndex(RedirectAttributes attributes) {
		return new RedirectView("/html/html/index.html");
	}

	@GetMapping(value = "/particle-config", produces = "text/json")
	public String particleConfigRoute() {
		String mode = configService.getParticleMode();
		return fileReader.getTextFileContents("/particle-config-" + mode + ".json");
	}	
	
	private String editPartyMetaTag(Party party) {
		String partyPage = fileReader.getTextFileContents("html/html/player.html");
		Document partyDoc = Jsoup.parse(partyPage);
		partyDoc.select("head > meta:nth-child(11)").attr("content", party.getOwnerName() + "'s room!");
		return partyDoc.toString();
	}
	
	@GetMapping(value = "/player", produces = "text/html")
	public String playerRoute(@RequestParam(required = false) String roomID) {
		if (roomID == null) return fileReader.getTextFileContents("html/html/noroom.html");
		else {
			Party party = partyService.getParty(roomID);
			if (party == null) return fileReader.getTextFileContents("html/html/noroom.html");
			else return editPartyMetaTag(party); 
		}
	}

}