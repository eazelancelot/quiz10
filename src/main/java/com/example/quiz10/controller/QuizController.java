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
	// �n�J���\��~��ϥ�
	@PostMapping(value = "quiz/create_login")
	public BasicRes createLogin(@Valid @RequestBody CreateUpdateReq req, HttpSession session) {
		// �b UserController �� login ��k���A�Y���n�J���\�A�N�|�z�L "uesr_name" �o�� key �� req ���� name �[�J session ��
		// ���T�Ȧs�� session ������k�O setAttribute�A���X�h�O�� getAttribute
		// key ���r�� user_name �n�����@�ˤ~����X������ value�F������ key ������ value �h�O null
		//======== �U�����g�k�A�p�G���X�� vaule �O null �ɡA�b�૬�� String �ɷ|����(NullPointerException)
		// String userName = (String)session.getAttribute("user_name");
		Object userNameObj = session.getAttribute("user_name");
		if(userNameObj == null) { 
			return new BasicRes(ResMessage.PLEASE_LOGIN_FIRST.getCode(), //
					ResMessage.PLEASE_LOGIN_FIRST.getMessage());
		}
		// �Y���򦳭n�ϥαq session ���X���ȡA�i�H�N userNameObj �j���૬���r��
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
	
	// �~���I�s�� API�A�����n�ϥ� JQuery ���覡: quiz/statistics?quizId=�ݨ��s��(�n���ݸ�)
	// quizId ���W�r�n�M��k�ѼƤ����ܼƦW�٤@��
	@PostMapping(value = "quiz/statistics")
	public StatisticsRes statistics(@RequestParam int quizId) {
		return quizService.statistics(quizId);
	}
	
	// �~���I�s�� API�A�����n�ϥ� JQuery ���覡: quiz/statistics?quiz_id=�ݨ��s��(�n���ݸ�)
	// �]�� @RequestParam ���� value �O quiz_id�A�ҥH�I�s���|�ݸ��᭱���r��N�|�O quiz_id
	// @RequestParam ���� required �w�]�O true
	// defaultValue �O����ѼƨS���ȩάO�ܼ� mapping ����ɡA�|���w�w�]��
	@PostMapping(value = "quiz/statistics1")
	public StatisticsRes statistics1(//
			@RequestParam(value = "quiz_id", required = false, defaultValue = "") int quizId) {
		return quizService.statistics(quizId);
	}
	
	// �I�s��API�� URL �O quiz/feedback?quizId=�ݨ��s��
	@PostMapping(value = "quiz/feedback")
	public FeedbackRes feedback(@RequestParam int quizId) {
		return quizService.feedback(quizId);
	}
}
