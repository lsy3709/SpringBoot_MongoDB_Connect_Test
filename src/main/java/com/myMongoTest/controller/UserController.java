package com.myMongoTest.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myMongoTest.document.Memo;
import com.myMongoTest.document.User2;
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

	@RequestMapping("/admin")
	public String admin(Model model) {
		List<Memo> memoList = userService.mongoFindAllMemo();
		model.addAttribute("count", memoList.size());
		return "admin";
	}

	@RequestMapping("/")
	public String main(Model model) {
		return "main";
	}
}
