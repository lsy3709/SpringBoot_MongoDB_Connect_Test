package com.myMongoTest.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myMongoTest.document.Memo;
import com.myMongoTest.document.User2;
import com.myMongoTest.document.Users;
import com.myMongoTest.service.UserService;
import com.myMongoTest.support.PasswordPolicyValidator;
import com.myMongoTest.support.ValidationResult;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final MessageSource messageSource;

	@GetMapping(value = "/login/error")
	public String loginError(Model model, Locale locale) {
		model.addAttribute("loginErrorMsg", messageSource.getMessage("error.login.invalid", null, locale));
		return "loginForm";
	}

	    @GetMapping(value = "/login")
	    public String loginMember(){
	        return "loginForm";
	    }

	    /**
	     * 로그인 성공 후 세션 쿠키가 확실히 붙은 상태에서 목적지로 리다이렉트하기 위한 경유 페이지.
	     * target 쿼리 파라미터가 있으면 해당 경로로, 없으면 /admin 으로 보낸다. (상대 경로만 허용)
	     */
	    @GetMapping("/login/redirect")
	    public void loginRedirect(@RequestParam(value = "target", required = false) String target,
	                             jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
	        String safeTarget = target != null && target.startsWith("/") && !target.startsWith("//")
	            ? target : "/admin";
	        response.sendRedirect(safeTarget);
	    }
	
	@ResponseBody
	@PostMapping("/insertDb")
	public ResponseEntity<String> insertDb(	@RequestBody Users user){
		userService.mongoUserInsert(user);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}

	@RequestMapping("/joinForm")
	  public String joinForm(Model model) {
		  model.addAttribute("user", new User2());
		  return "joinForm";
	  }

	@PostMapping("/joinUser")
	public String joinUser(User2 user, RedirectAttributes redirectAttributes, Locale locale) {
		ValidationResult passwordResult = PasswordPolicyValidator.validate(user.getPassword());
		if (passwordResult != null) {
			String msg = messageSource.getMessage(passwordResult.getMessageCode(),
					passwordResult.getArgs(), locale);
			redirectAttributes.addFlashAttribute("joinErrorMsg", msg);
			return "redirect:/joinForm";
		}
		String email = user.getEmail();
		if (userService.mongoFindOneUser2Email(email) != null) {
			redirectAttributes.addFlashAttribute("joinErrorMsg",
					messageSource.getMessage("join.error.duplicate_email", null, locale));
			return "redirect:/joinForm";
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		userService.mongoUser2Insert(user);
		redirectAttributes.addFlashAttribute("joinSuccessMsg",
				messageSource.getMessage("join.success", null, locale));
		return "redirect:/login";
	}
	
	@ResponseBody
	@PostMapping("/updateDb")
	public ResponseEntity<String> updateDb(	@RequestBody Users user){
		userService.mongoUserUpdate(user);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping("/findAll")
	public List<Users> list( ){
		List<Users> userList = userService.mongoFindAll();
		return userList;
	}

	@RequestMapping("/admin")
	public String admin(Model model) {
		List<Users> userList = userService.mongoFindAll();
		List<Memo> memoList = userService.mongoFindAllMemo();
		int count = memoList.size();
		model.addAttribute("user", userList);
		  model.addAttribute("count",  count);
		return "admin";
	  } 
	  
	  @RequestMapping("/")
	  public String main(Model model ){
		List<Users> userList = userService.mongoFindAll();
		model.addAttribute("user",  userList);
		return "main";
	  } 

	  
	  @RequestMapping("/updateForm/{id}")
	  public String updateForm(	Model model , @PathVariable Long id){
		Users user = userService.mongoFindOne(id);
		model.addAttribute("user",  user);
		return "updateForm";
	}
}
