package com.example.quiz10.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.quiz10.constants.ResMessage;
import com.example.quiz10.service.ifs.UserService;
import com.example.quiz10.vo.BasicRes;
import com.example.quiz10.vo.UserReq;

@RestController
public class UserController {

	@Autowired
	private UserService userService;

	@PostMapping(value = "user/register")
	public BasicRes register(@Valid @RequestBody UserReq req) {
		return userService.register(req);
	}

	@PostMapping(value = "user/login")
	public BasicRes login(@Valid @RequestBody UserReq req, HttpSession session) {
		// session 的存活時間預設為30分鐘，可以透過以下方法設定時間長短；括號中的數字單位是秒
		// 數字 0 或負數，表示該 session 都不會過期
		session.setMaxInactiveInterval(300); // 300秒
		BasicRes res = userService.login(req);
		if (res.getCode() == 200) {
			// 若登入成功，把使用者名稱暫存在 session 中
			// 每個 client 與 server 之間的 session 都不一樣
			session.setAttribute("user_name", req.getName());
		}
		return res;
	}

	// 因為沒有 req，所以用 GetMapping
	@GetMapping(value = "user/logout")
	public BasicRes logout(HttpSession session) {
		// logout 就是要讓彼此之間通訊用的 session 失效(過期)
		session.invalidate();
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

}
