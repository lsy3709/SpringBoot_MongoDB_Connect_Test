package com.myMongoTest.controller;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.myMongoTest.document.Memo;
import com.myMongoTest.document.User2;
import com.myMongoTest.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 - 회원 관리 (목록, 상세, 권한 변경, 탈퇴).
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminMemberController {

    private final UserService userService;

    @GetMapping
    public String dashboard(Model model) {
        List<Memo> memoList = userService.mongoFindAllMemo();
        List<User2> userList = userService.mongoFindAllUser2();
        model.addAttribute("memoCount", memoList.size());
        model.addAttribute("userCount", userList.size());
        return "admin_dashboard";
    }

    @GetMapping("/inventory")
    public String inventory(Model model) {
        List<Memo> memoList = userService.mongoFindAllMemo();
        model.addAttribute("count", memoList.size());
        return "admin";
    }

    @GetMapping("/members")
    public String memberList(Model model) {
        List<User2> users = userService.mongoFindAllUser2();
        model.addAttribute("users", users);
        return "admin_members";
    }

    @GetMapping("/members/{id}")
    public String memberDetail(@PathVariable String id, Model model, RedirectAttributes ra) {
        User2 user = userService.mongoFindOneUser2ById(new ObjectId(id));
        if (user == null) {
            ra.addFlashAttribute("errorMsg", "해당 회원을 찾을 수 없습니다.");
            return "redirect:/admin/members";
        }
        model.addAttribute("user", user);
        return "admin_member_detail";
    }

    @PostMapping("/members/{id}/role")
    public String updateRole(@PathVariable String id, @RequestParam String role,
                             RedirectAttributes ra) {
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            ra.addFlashAttribute("errorMsg", "유효하지 않은 권한입니다.");
            return "redirect:/admin/members/" + id;
        }
        userService.mongoUser2UpdateRole(id, role);
        ra.addFlashAttribute("successMsg", "권한이 변경되었습니다.");
        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/members/{id}/withdraw")
    public String withdraw(@PathVariable String id, RedirectAttributes ra) {
        boolean deleted = userService.mongoUser2Delete(id);
        if (!deleted) {
            ra.addFlashAttribute("errorMsg", "탈퇴 처리할 수 없습니다. (마지막 관리자는 탈퇴 불가)");
            return "redirect:/admin/members/" + id;
        }
        ra.addFlashAttribute("successMsg", "회원 탈퇴 처리되었습니다.");
        return "redirect:/admin/members";
    }
}
