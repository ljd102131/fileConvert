package lee.files.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/system")
public class openController {	
	
	@RequestMapping(value="/open",method=RequestMethod.GET)
	public String fileConvert() {
		System.out.println("进入啦");
		return "fileConvert";
	}
}
