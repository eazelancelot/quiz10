package com.example.quiz10.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.example.quiz10.constants.ResMessage;
import com.example.quiz10.constants.SelectType;
import com.example.quiz10.entity.Feedback;
import com.example.quiz10.entity.Ques;
import com.example.quiz10.entity.Quiz;
import com.example.quiz10.repository.FeedbackDao;
import com.example.quiz10.repository.QuesDao;
import com.example.quiz10.repository.QuizDao;
import com.example.quiz10.service.ifs.QuizService;
import com.example.quiz10.vo.BasicRes;
import com.example.quiz10.vo.CreateUpdateReq;
import com.example.quiz10.vo.DeleteReq;
import com.example.quiz10.vo.FeedbackRes;
import com.example.quiz10.vo.FillinReq;
import com.example.quiz10.vo.QuizRes;
import com.example.quiz10.vo.SearchReq;
import com.example.quiz10.vo.SearchRes;
import com.example.quiz10.vo.StatisticsRes;
import com.example.quiz10.vo.StatisticsVo;

@Service
public class QuizServiceImpl implements QuizService {

	@Autowired
	private QuizDao quizDao;

	@Autowired
	private QuesDao quesDao;

	@Autowired
	private FeedbackDao feedbackDao;

	// @Transactional import 的 library， javax 和 springframework 都可以使用
	// 兩者差異可參照 PPT spring boot_02 @Transactional 部分
	@Transactional
	@Override
	public BasicRes create(CreateUpdateReq req) {
		// 基本的屬性判斷已交由 @Valide
		// 開始時間不能比結束時間晚
		if (req.getStartDate().isAfter(req.getEndDate())) {
			return new BasicRes(ResMessage.DATA_ERROR.getCode(), ResMessage.DATA_ERROR.getMessage());
		}
		// 判斷問題型態非文字時，選項要有值
		List<Ques> quesList = req.getQuesList();
		for (Ques item : quesList) {
//			if(!item.getType().equalsIgnoreCase(SelectType.TEXT.getType())) {
//				
//			}
			// 不使用上面的寫法是因為從 req 過來的參數值可能會有第4種的值
			// 下面判斷式的寫法就必須是 SINGLE 或 MULTI 其中之一
			if (item.getType().equalsIgnoreCase(SelectType.SINGLE.getType())
					|| item.getType().equalsIgnoreCase(SelectType.MULTI.getType())) {
				// 確定是單或多選，選項就必須要有值
				// 前面有驚嘆號，表示否定的意思
				if (!StringUtils.hasText(item.getOptions())) {
					return new BasicRes(ResMessage.OPTIONS_ERROR.getCode(), //
							ResMessage.OPTIONS_ERROR.getMessage());
				}
			}
		}
		// 因為 Quiz 中的 id 是 AI 自動生成的流水號，要讓 quizDao 執行 save 後可以把該 id 的值回傳，
		// 必須要在 Quiz 此 Entity 中將資料型態為 int 的屬性 id
		// 加上 @GeneratedValue(strategy = GenerationType.IDENTITY)
		Quiz res = quizDao.save(new Quiz(req.getName(), req.getDescription(), req.getStartDate(), //
				req.getEndDate(), req.isPublished()));
		// 將返回的 res 中的 id(quiz_id) 塞到 Ques 中的 quizId 此屬性上
		quesList.forEach(item -> {
			item.setQuizId(res.getId());
		});
		quesDao.saveAll(quesList);
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public BasicRes update(CreateUpdateReq req) {
		// 基本的屬性判斷已交由 @Valide
		// 開始時間不能比結束時間晚
		if (req.getStartDate().isAfter(req.getEndDate())) {
			return new BasicRes(ResMessage.DATA_ERROR.getCode(), ResMessage.DATA_ERROR.getMessage());
		}
		// 判斷問題型態非文字時，選項要有值
		List<Ques> quesList = req.getQuesList();
		for (Ques item : quesList) {
			// 檢查問題中的 quizId 和 問卷中的 id 是否相同
			if (item.getQuizId() != req.getId()) {
				return new BasicRes(ResMessage.QUIZ_ID_NOT_MATCH.getCode(), //
						ResMessage.QUIZ_ID_NOT_MATCH.getMessage());
			}
//					if(!item.getType().equalsIgnoreCase(SelectType.TEXT.getType())) {
//						
//					}
			// 不使用上面的寫法是因為從 req 過來的參數值可能會有第4種的值
			// 下面判斷式的寫法就必須是 SINGLE 或 MULTI 其中之一
			if (item.getType().equalsIgnoreCase(SelectType.SINGLE.getType())
					|| item.getType().equalsIgnoreCase(SelectType.MULTI.getType())) {
				// 確定是單或多選，選項就必須要有值
				// 前面有驚嘆號，表示否定的意思
				if (!StringUtils.hasText(item.getOptions())) {
					return new BasicRes(ResMessage.OPTIONS_ERROR.getCode(), //
							ResMessage.OPTIONS_ERROR.getMessage());
				}
			}
		}
		// 檢查要修改的問卷是否已存在
		if (!quizDao.existsById(req.getId())) {
			return new BasicRes(ResMessage.QUIZ_NOT_FOUND.getCode(), //
					ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		quizDao.save(new Quiz(req.getId(), req.getName(), req.getDescription(), req.getStartDate(), //
				req.getEndDate(), req.isPublished()));
		// 刪除此張問卷所有的問題
		quesDao.deleteByQuizId(req.getId());
		// 新增更新後的問題
		quesDao.saveAll(quesList);
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public BasicRes delete(DeleteReq req) {
		// 進行中的問卷不能刪除: 找出要刪除的 idList 中是否有包含進行中的問卷
		// 進行中的問卷條件: 1. 已發佈 且 2. 當前時間大於等於開始日期 且 3. 當前時間小於等於結束日期
		boolean res = quizDao.existsByIdInAndPublishedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(//
				req.getQuizIdList(), LocalDate.now(), LocalDate.now());
		if (res) { // 等同於 res == true，表示要刪除的問卷ID中有正在進行中的
			return new BasicRes(ResMessage.QUIZ_IN_PROGRESS.getCode(), //
					ResMessage.QUIZ_IN_PROGRESS.getMessage());
		}
		quizDao.deleteAllById(req.getQuizIdList());
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public SearchRes search(SearchReq req) {
		// TODO Auto-generated method stub
		String quizName = req.getQuizName();
		LocalDate startDate = req.getStartDate();
		LocalDate endDate = req.getEndDate();
		if (!StringUtils.hasText(quizName)) {
			// quizName 不帶值時，意思就是非搜尋條件之一，所以其值可能會是空字串或是 null
			// 全空白也當成是非搜尋條件之一
			// 會把 quizName 變成空字串是因為 containing，其搜尋的欄位值帶空字串時，會撈全部
			quizName = "";
		}
		if (startDate == null) {
			startDate = LocalDate.of(1970, 1, 1);
		}
		if (endDate == null) {
			endDate = LocalDate.of(2999, 12, 31);
		}
		List<Quiz> res = quizDao.findByNameContainingAndStartDateGreaterThanEqualAndEndDateLessThanEqual(//
				quizName, startDate, endDate);
		List<QuizRes> quizResList = new ArrayList<>();
		// 方法 1: 使用 foreach 根據每個 quizId 去撈對應的 Ques
		// 缺點: 依據 Quiz 的多寡決定連接DB撈取資料的次數；問卷越多，連接DB次數就越多
		for (Quiz item : res) {
			// 根據 quizId 撈取每張問卷裡的 quesList
			int quizId = item.getId();
			List<Ques> quesList = quesDao.findByQuizId(quizId);
			// 建立不同問卷的 QuizRes 用來放 Quiz 和對應的 List<Ques>
			// 若參數太多使用建構方法容易混亂，建議用以下的方式
			QuizRes quizRes = new QuizRes();
			quizRes.setId(quizId);
			quizRes.setName(item.getName());
			quizRes.setDescription(item.getDescription());
			quizRes.setStartDate(item.getStartDate());
			quizRes.setEndDate(item.getEndDate());
			quizRes.setPublished(item.isPublished());
			quizRes.setQuesList(quesList);
			// 把每張不同的問卷+問題放進 List<QuizRes> 中
			quizResList.add(quizRes);
		}
		// 方法 2: 先蒐集所有符合的 quizId，再一次撈取符合條件的所有 Ques；不管問卷多寡，固定就是連接 DB 2次
		List<Integer> quizIdList = new ArrayList<>();
		for (Quiz item : res) {
			quizIdList.add(item.getId());
		}
		List<Ques> quesList = quesDao.findByQuizIdIn(quizIdList);
		// 將符合搜尋條件的 res(所有的問卷) 和 quesList(所有的問題) 配對
		List<QuizRes> quizResList2 = new ArrayList<>();
		for (Quiz item : res) {
			int quizId = item.getId();
			List<Ques> returnQuesList = new ArrayList<>();
			for (Ques quesItem : quesList) {
				// 判斷 Quiz 和 Ques 中的 quizId 是否有相同
				if (quizId == quesItem.getQuizId()) {
					// quizId 一樣，就加進要返回的 QuesList 中
					returnQuesList.add(quesItem);
				}
			}
			QuizRes quizRes = new QuizRes();
			quizRes.setId(quizId);
			quizRes.setName(item.getName());
			quizRes.setDescription(item.getDescription());
			quizRes.setStartDate(item.getStartDate());
			quizRes.setEndDate(item.getEndDate());
			quizRes.setPublished(item.isPublished());
			quizRes.setQuesList(returnQuesList);
			// 把每張不同的問卷+問題放進 List<QuizRes> 中
			quizResList2.add(quizRes);
		}
//		return new SearchRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), quizResList2);
		return new SearchRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), quizResList);
	}

	@Override
	public BasicRes fillin(FillinReq req) {
		// 先檢查 req 中的 List<Feedback>，所有的 Feedback 的 quizId 和 email 都是一樣的
		// 就是 quizId 和 email 只會有一個
		List<Feedback> feedbackList = req.getFeedbackList();
		Set<Integer> quizIdSet = new HashSet<>();
		Set<String> emailSet = new HashSet<>();
		for (Feedback item : feedbackList) {
			quizIdSet.add(item.getQuizId());
			emailSet.add(item.getEmail());
		}
		// 因為 set 的值不會有重複，所以兩個 set 的大小都不是1的話就表示 quizId 或 email 不一致
		if (quizIdSet.size() != 1 || emailSet.size() != 1) {
			return new BasicRes(ResMessage.QUIZ_ID_OR_EMAIL_INCONSISTENT.getCode(), //
					ResMessage.QUIZ_ID_OR_EMAIL_INCONSISTENT.getMessage());
		}
		int quizId = feedbackList.get(0).getQuizId();
		// 檢查同一個 email + quizId 是否已存在(同一個 email 已經填寫過同一張問卷)
		if (feedbackDao.existsByQuizIdAndEmail(quizId, //
				req.getFeedbackList().get(0).getEmail())) {
			return new BasicRes(ResMessage.EMAIL_DUPLICATE.getCode(), //
					ResMessage.EMAIL_DUPLICATE.getMessage());
		}
		// 檢查問卷是否可以填寫的狀態: 1. 已發佈 2. 當前時間大於等於開始時間 3. 當前時間小於等於結束時間
		// 前面有驚嘆號，表示找不到已發佈且當前時間是介於開始時間與結束時間之間的資料
		if (!quizDao.existsByIdInAndPublishedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(//
				List.of(quizId), LocalDate.now(), LocalDate.now())) {
			return new BasicRes(ResMessage.CANNOT_FILLIN_QUIZ.getCode(), //
					ResMessage.CANNOT_FILLIN_QUIZ.getMessage());
		}
		// 檢查答案
		List<Ques> quesList = quesDao.findByQuizId(quizId);
		// 檢查問題和答案的筆數是否一樣
		if (feedbackList.size() != quesList.size()) {
			return new BasicRes(ResMessage.FILLIN_INCOMPLETE.getCode(), //
					ResMessage.FILLIN_INCOMPLETE.getMessage());
		}
		// 先蒐集必填的 quId 和 單選的 quId 以及問題的編號
		Set<Integer> necessaryQuIds = new HashSet<>();
		Set<Integer> singleQuIds = new HashSet<>();
		Set<Integer> quIds = new HashSet<>();
		for (Ques quesItem : quesList) {
			quIds.add(quesItem.getId());
			if (quesItem.isNecessary()) {
				necessaryQuIds.add(quesItem.getId());
			}
			if (quesItem.getType().equalsIgnoreCase(SelectType.SINGLE.getType())) {
				singleQuIds.add(quesItem.getId());
			}
		}
		Map<Integer, List<String>> quIdAnsMap = new HashMap<>();
		for (Feedback item : feedbackList) {
			int quId = item.getQuId();
			// 檢查答案的題號是否與問題題號相符
			if (!quIds.contains(quId)) { // 前面有驚嘆號，表示答案題號不包含在問題題號中
				return new BasicRes(ResMessage.QUID_MISMATCH.getCode(), //
						ResMessage.QUID_MISMATCH.getMessage());
			}
			// 檢查必填且要有答案
			// 必填但沒答案
			if (necessaryQuIds.contains(quId) && !StringUtils.hasText(item.getAns())) {
				return new BasicRes(ResMessage.FILLIN_IS_NECESSARY.getCode(), //
						ResMessage.FILLIN_IS_NECESSARY.getMessage());
			}
			List<String> ansList = List.of(item.getAns().split(";"));
			// 檢查單選時，不能有多個答案
			if (singleQuIds.contains(quId) && ansList.size() > 1) {
				return new BasicRes(ResMessage.SINGLE_CHOICE_QUES.getCode(), //
						ResMessage.SINGLE_CHOICE_QUES.getMessage());
			}
			// 先將題號與對應的答案蒐集，用來後續比對答案與選項用
			quIdAnsMap.put(quId, ansList);
		}
		// 檢查非文字時，答案與選項要一致
		for (Ques item : quesList) {
			String type = item.getType();
			if (!type.equalsIgnoreCase(SelectType.TEXT.getType())) {
				// quIdAnsMap.get(quId): 是根據 key 來取得 map 中對應的 value
				List<String> ansList = quIdAnsMap.get(item.getId());
				List<String> optionList = List.of(item.getOptions().split(";"));
				// 相同的問題，判斷答案是否與選項一致
				if (!optionList.containsAll(ansList)) {
					return new BasicRes(ResMessage.OPTION_ANSWER_MISMATCH.getCode(), //
							ResMessage.OPTION_ANSWER_MISMATCH.getMessage());
				}
			}
		}
		feedbackDao.saveAll(feedbackList);
		return new BasicRes(ResMessage.SUCCESS.getCode(), //
				ResMessage.SUCCESS.getMessage());
	}

	@Override
	public StatisticsRes statistics(int quizId) {
		// 先撈取 quiz 的相關資訊
		Optional<Quiz> op = quizDao.findById(quizId);
		if (op.isEmpty()) {
			return new StatisticsRes(ResMessage.QUIZ_NOT_FOUND.getCode(), //
					ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		Quiz quiz = op.get();
		String quizName = quiz.getName();
		// 先從 ques 撈取非文字類型的問題(文字類型的問題不列入統計)
		List<Ques> quesList = quesDao.findByQuizIdAndTypeNot(quizId, SelectType.TEXT.getType());
		// 蒐集單選或多選問題的 Id，後續用來撈取 feedback 的資料用
		List<Integer> quIdList = new ArrayList<>();
		// 先蒐集問題編號與選項，之後可確定 feedback 中的答案一定會與問題選項一致，避免 feedback 中的答案非原來的選項
		// 問題編號 選項 次數
		Map<Integer, Map<String, Integer>> quIdOptionCountMap = new HashMap<>();
		List<StatisticsVo> statisticsList = new ArrayList<>();
		for (Ques item : quesList) {
			quIdList.add(item.getId());
			Map<String, Integer> optionCountMap = new HashMap<>();
			List<String> optionList = List.of(item.getOptions().split(";"));
			for (String option : optionList) {
				optionCountMap.put(option, 0);
			}
			quIdOptionCountMap.put(item.getId(), optionCountMap);
			StatisticsVo vo = new StatisticsVo();
			vo.setQuId(item.getId());
			vo.setQu(item.getOptions());
			statisticsList.add(vo);
		}
		// =========================
		// 上面的 for 迴圈執行之後會是類似以下的概念
		// 問題編號 選項 次數
		// Map<Integer, Map<String, Integer>> quIdOptionCountMap = new HashMap<>();
		// 1 蛋餅 0
		// 1 三明治 0
		// 1 飯糰 0
		// 1 漢堡 0
		// 以上相同 key 的資料，只會有一筆，就是問題編號 1，但對應的 value 中的 map 就會有4個資料
		// 可以寫個 test 資料就可以得知其資料格式
		// 2 奶茶 0
		// 以此類推
		// =========================
		//
		List<Feedback> feedbackList = feedbackDao.findByQuizIdAndQuIdIn(quizId, quIdList);
		// ==========================
		for (Feedback item : feedbackList) {
			// 根據問題編號把 選項次數的 map 從 quIdOptionCountMap 中取出
			Map<String, Integer> optionCountMap = quIdOptionCountMap.get(item.getQuId());
			// 將 feedback 中的 ans 字串轉成 List
			List<String> ansList = List.of(item.getAns().split(";"));
			for (String ans : ansList) {
				// 根據選項取出對應的次數
				int count = optionCountMap.get(ans);
				// 將原本的次數 +1
				count++;
				// 把相同的 選項-次數 放回到 map 中
				optionCountMap.put(ans, count);
			}
			// 更新 quIdOptionCountMap 中的資料
			quIdOptionCountMap.put(item.getQuId(), optionCountMap);
		}
		for (StatisticsVo item : statisticsList) {
			int quId = item.getQuId();
			Map<String, Integer> optionCountMap = quIdOptionCountMap.get(quId);
			item.setOptionCountMap(optionCountMap);
			// 上面3行程式碼可用下面一行表示
			// item.setOptionCountMap(quIdOptionCountMap.get(item.getQuId()));
		}
		return new StatisticsRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
				quizName, statisticsList);
	}

	@Override
	public FeedbackRes feedback(int quizId) {
		if(quizId <= 0) {
			return new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
					new ArrayList<>());
		}
		List<Feedback> res = feedbackDao.findByQuizId(quizId);
		return new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
				res);

		//3元式寫法
//		return quizId <= 0 ? new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
//				new ArrayList<>()) : new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
//						feedbackDao.findByQuizId(quizId));
	}

}
