package com.example.quiz10.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.quiz10.constants.ResMessage;
import com.example.quiz10.service.ifs.QuizService;
import com.example.quiz10.vo.BasicRes;
import com.example.quiz10.vo.CreateUpdateReq;
import com.example.quiz10.vo.DeleteReq;
import com.example.quiz10.vo.FeedbackRes;
import com.example.quiz10.vo.FillinReq;
import com.example.quiz10.vo.SearchReq;
import com.example.quiz10.vo.SearchRes;
import com.example.quiz10.vo.StatisticsRes;

@RestController
public class QuizController {

	@Autowired
	private QuizService quizService;

	@PostMapping(value = "quiz/create")
	public BasicRes create(@Valid @RequestBody CreateUpdateReq req) {
		return quizService.create(req);
	}
	//==================
	// 登入成功後才能使用
	@PostMapping(value = "quiz/create_login")
	public BasicRes createLogin(@Valid @RequestBody CreateUpdateReq req, HttpSession session) {
		// 在 UserController 的 login 方法中，若有登入成功，就會透過 "uesr_name" 這個 key 把 req 中的 name 加入 session 中
		// 把資訊暫存到 session 中的方法是 setAttribute，取出則是用 getAttribute
		// key 的字串 user_name 要完全一樣才能取出對應的 value；取不到 key 對應的 value 則是 null
		//======== 下面的寫法，如果取出的 vaule 是 null 時，在轉型成 String 時會報錯(NullPointerException)
		// String userName = (String)session.getAttribute("user_name");
		Object userNameObj = session.getAttribute("user_name");
		if(userNameObj == null) { 
			return new BasicRes(ResMessage.PLEASE_LOGIN_FIRST.getCode(), //
					ResMessage.PLEASE_LOGIN_FIRST.getMessage());
		}
		// 若後續有要使用從 session 取出的值，可以將 userNameObj 強制轉型成字串
		// String userName = (String)userNameObj;
		return quizService.create(req);
	}
	//==================
	@PostMapping(value = "quiz/update")
	public BasicRes update(@Valid @RequestBody CreateUpdateReq req) {
		return quizService.update(req);
	}

	@PostMapping(value = "quiz/delete")
	public BasicRes delete(@Valid @RequestBody DeleteReq req) {
		return quizService.delete(req);
	}

	@PostMapping(value = "quiz/search")
	public SearchRes search(@RequestBody SearchReq req) {
		return quizService.search(req);
	}

	@PostMapping(value = "quiz/fillin")
	public BasicRes fillin(@Valid @RequestBody FillinReq req) {
		return quizService.fillin(req);
	}
	
	// 外部呼叫此 API，必須要使用 JQuery 的方式: quiz/statistics?quizId=問卷編號(要有問號)
	// quizId 的名字要和方法參數中的變數名稱一樣
	@PostMapping(value = "quiz/statistics")
	public StatisticsRes statistics(@RequestParam int quizId) {
		return quizService.statistics(quizId);
	}
	
	// 外部呼叫此 API，必須要使用 JQuery 的方式: quiz/statistics?quiz_id=問卷編號(要有問號)
	// 因為 @RequestParam 中的 value 是 quiz_id，所以呼叫路徑問號後面的字串就會是 quiz_id
	// @RequestParam 中的 required 預設是 true
	// defaultValue 是指當參數沒給值或是變數 mapping 不到時，會給定預設值
	@PostMapping(value = "quiz/statistics1")
	public StatisticsRes statistics1(//
			@RequestParam(value = "quiz_id", required = false, defaultValue = "") int quizId) {
		return quizService.statistics(quizId);
	}
	
	// 呼叫此API的 URL 是 quiz/feedback?quizId=問卷編號
	@PostMapping(value = "quiz/feedback")
	public FeedbackRes feedback(@RequestParam int quizId) {
		return quizService.feedback(quizId);
	}
}
