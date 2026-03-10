package com.myMongoTest.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myMongoTest.document.Memo;
import com.myMongoTest.document.User2;
import com.myMongoTest.service.UserService;
import com.myMongoTest.support.PasswordPolicyValidator;
import com.myMongoTest.support.ValidationResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
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
		// 회원가입 전체 흐름 디버깅용 로그
		if (user != null) {
			log.info("[회원가입 요청] email={}, role={}", user.getEmail(), user.getRole());
		} else {
			log.warn("[회원가입 요청] user 파라미터가 null 입니다.");
		}

		ValidationResult passwordResult = PasswordPolicyValidator.validate(user.getPassword());
		if (passwordResult != null) {
			log.warn("[회원가입 실패] 비밀번호 정책 위반: code={}, args={}",
					passwordResult.getMessageCode(), passwordResult.getArgs());
			String msg = messageSource.getMessage(passwordResult.getMessageCode(),
					passwordResult.getArgs(), locale);
			redirectAttributes.addFlashAttribute("joinErrorMsg", msg);
			redirectAttributes.addFlashAttribute("preservedEmail", user != null ? user.getEmail() : "");
			return "redirect:/joinForm";
		}
		String email = user.getEmail();
		log.debug("[회원가입 검증] 중복 이메일 체크 시작: email={}", email);
		if (userService.mongoFindOneUser2Email(email) != null) {
			log.warn("[회원가입 실패] 중복된 이메일: email={}", email);
			redirectAttributes.addFlashAttribute("joinErrorMsg",
					messageSource.getMessage("join.error.duplicate_email", null, locale));
			redirectAttributes.addFlashAttribute("preservedEmail", email);
			return "redirect:/joinForm";
		}
		String rawPassword = user.getPassword();
		String encodedPassword = passwordEncoder.encode(rawPassword);
		log.info("[회원가입 처리] 비밀번호 인코딩 완료: email={}, rawLength={}, encodedPrefix={}",
				email,
				rawPassword != null ? rawPassword.length() : 0,
				encodedPassword != null && encodedPassword.length() >= 7 ? encodedPassword.substring(0, 7) : "(null)");

		user.setPassword(encodedPassword);
		userService.mongoUser2Insert(user);
		log.info("[회원가입 성공] email={}, role={}", email, user.getRole());

		redirectAttributes.addFlashAttribute("joinSuccessMsg",
				messageSource.getMessage("join.success", null, locale));
		return "redirect:/login";
	}

	// /admin → AdminMemberController#dashboard

	@RequestMapping("/")
	public String main(Model model) {
		return "main";
	}
}
