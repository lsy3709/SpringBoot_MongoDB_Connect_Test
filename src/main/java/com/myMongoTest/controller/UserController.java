package com.myMongoTest.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.myMongoTest.document.User;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class UserController {

	   private final UserService userService;
	
	@ResponseBody
	  @RequestMapping("/") 
	  public String home(){
	    System.out.println("Hello Boot!!");
	    userService.mongoInsert();
	    return "Hello Boot!!"; 
	  }

	@ResponseBody
	@PostMapping("/insertDb")
	public ResponseEntity<String> replyInsert(	@RequestBody User user){
		userService.mongoUserInsert(user);
		System.out.println("몽공 디비 추가 확인");
		return new ResponseEntity<String>("success",HttpStatus.OK);
	}
	
	@ResponseBody
	@GetMapping("/findAll")
	public List<User> list( ){
		List<User> userList = userService.mongoFindAll();
		return userList;
	}
	
	  @RequestMapping("/hello")
	  public String hello(Model model ){
		System.out.println("안녕하세요");
		List<User> userList = userService.mongoFindAll();
		System.out.println("userList"+ userList);
		model.addAttribute("user",  userList);
		return "hello";
	  } 
	  
	  @RequestMapping("/updateForm/{id}")
	  public String updateForm(	Model model , @PathVariable Long id){
//		  public String updateForm(	){
		User user = userService.mongoFindOne(id);
		System.out.println("user"+ user);
		model.addAttribute("user",  user);
		return "updateForm";
	  } 
	  
	  @ResponseBody
		@DeleteMapping("/dbDelete/{id}")
		public Long delete(@PathVariable Long id) {
			System.out.println("삭제 실행전: "+id);	
			userService.deleteDb("_id", id);
			System.out.println("삭제 실행후: "+id);
			return id;
	  
}
}
