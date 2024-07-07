package main.java.de.voidtech.ytparty.api.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class HttpRestController {

	@GetMapping("/")
	public RedirectView redirectToIndex(RedirectAttributes attributes) {
		return new RedirectView("/html/index.html");
	}

}