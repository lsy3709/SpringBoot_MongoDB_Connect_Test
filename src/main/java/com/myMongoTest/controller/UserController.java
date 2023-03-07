package com.myMongoTest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.myMongoTest.DTO.SearchDB;
import com.myMongoTest.document.LoginForm;
import com.myMongoTest.document.Users;
import com.myMongoTest.document.User2;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class UserController {

	   private final UserService userService;
	
	    @GetMapping(value = "/login/error")
	    public String loginError(Model model){
	        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
	        return "loginForm";
	    }
	  
	    @GetMapping(value = "/login")
	    public String loginMember(){
	        return "loginForm";
	    }
	
	@ResponseBody
	@PostMapping("/insertDb")
	public ResponseEntity<String> insertDb(	@RequestBody Users user){
		userService.mongoUserInsert(user);
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	
	  
	  @RequestMapping("/joinForm")
	  public String joinForm(Model model ){
		  model.addAttribute("User2", new User2());
		  System.out.println("joinFomr");
		return "joinForm";
	  } 
	
	@PostMapping("/joinUser")
	public String joinUser(User2 user){
		System.out.println("요청이 왔나요?");
		String email = user.getEmail();
		System.out.println("user.getEmail()"+user.getEmail());
		System.out.println("user.getPassword()"+user.getPassword());
		System.out.println("user.getRole()"+user.getRole());
		if(userService.mongoFindOneUser2Email(email) == null) {
		
			userService.mongoUser2Insert(user);
			return "redirect:/";
		}
		return "/joinForm";
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
	
	@ResponseBody
	@PostMapping("/searchDb")
	public List<Users> searchlist( @RequestBody SearchDB searchDB){
		List<Users> userList = userService.mongoSearchFindAll(searchDB);
		return userList;
	}
	

	  @RequestMapping("/admin")
	  public String hello(Model model ){
		List<Users> userList = userService.mongoFindAll();
		model.addAttribute("user",  userList);
		return "admin";
	  } 
	  
	  @RequestMapping("/")
	  public String main(Model model ){
		  Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		    boolean isAdmin = authentication.getAuthorities().stream()
		                            .anyMatch(a -> a.getAuthority().equals("ADMIN"));
		    // isAdmin 변수를 Model에 추가
		    model.addAttribute("isAdmin", isAdmin);
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
	  
	  @ResponseBody
		@DeleteMapping("/dbDelete/{id}")
		public Long delete(@PathVariable Long id) {
			userService.deleteDb("_id", id);
			return id;
	  
}
}
