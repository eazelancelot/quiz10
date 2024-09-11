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

	// @Transactional import �� library�A javax �M springframework ���i�H�ϥ�
	// ��̮t���i�ѷ� PPT spring boot_02 @Transactional ����
	@Transactional
	@Override
	public BasicRes create(CreateUpdateReq req) {
		// �򥻪��ݩʧP�_�w��� @Valide
		// �}�l�ɶ�����񵲧��ɶ���
		if (req.getStartDate().isAfter(req.getEndDate())) {
			return new BasicRes(ResMessage.DATA_ERROR.getCode(), ResMessage.DATA_ERROR.getMessage());
		}
		// �P�_���D���A�D��r�ɡA�ﶵ�n����
		List<Ques> quesList = req.getQuesList();
		for (Ques item : quesList) {
//			if(!item.getType().equalsIgnoreCase(SelectType.TEXT.getType())) {
//				
//			}
			// ���ϥΤW�����g�k�O�]���q req �L�Ӫ��Ѽƭȥi��|����4�ت���
			// �U���P�_�����g�k�N�����O SINGLE �� MULTI �䤤���@
			if (item.getType().equalsIgnoreCase(SelectType.SINGLE.getType())
					|| item.getType().equalsIgnoreCase(SelectType.MULTI.getType())) {
				// �T�w�O��Φh��A�ﶵ�N�����n����
				// �e������ĸ��A��ܧ_�w���N��
				if (!StringUtils.hasText(item.getOptions())) {
					return new BasicRes(ResMessage.OPTIONS_ERROR.getCode(), //
							ResMessage.OPTIONS_ERROR.getMessage());
				}
			}
		}
		// �]�� Quiz ���� id �O AI �۰ʥͦ����y�����A�n�� quizDao ���� save ��i�H��� id ���Ȧ^�ǡA
		// �����n�b Quiz �� Entity ���N��ƫ��A�� int ���ݩ� id
		// �[�W @GeneratedValue(strategy = GenerationType.IDENTITY)
		Quiz res = quizDao.save(new Quiz(req.getName(), req.getDescription(), req.getStartDate(), //
				req.getEndDate(), req.isPublished()));
		// �N��^�� res ���� id(quiz_id) ��� Ques ���� quizId ���ݩʤW
		quesList.forEach(item -> {
			item.setQuizId(res.getId());
		});
		quesDao.saveAll(quesList);
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public BasicRes update(CreateUpdateReq req) {
		// �򥻪��ݩʧP�_�w��� @Valide
		// �}�l�ɶ�����񵲧��ɶ���
		if (req.getStartDate().isAfter(req.getEndDate())) {
			return new BasicRes(ResMessage.DATA_ERROR.getCode(), ResMessage.DATA_ERROR.getMessage());
		}
		// �P�_���D���A�D��r�ɡA�ﶵ�n����
		List<Ques> quesList = req.getQuesList();
		for (Ques item : quesList) {
			// �ˬd���D���� quizId �M �ݨ����� id �O�_�ۦP
			if (item.getQuizId() != req.getId()) {
				return new BasicRes(ResMessage.QUIZ_ID_NOT_MATCH.getCode(), //
						ResMessage.QUIZ_ID_NOT_MATCH.getMessage());
			}
//					if(!item.getType().equalsIgnoreCase(SelectType.TEXT.getType())) {
//						
//					}
			// ���ϥΤW�����g�k�O�]���q req �L�Ӫ��Ѽƭȥi��|����4�ت���
			// �U���P�_�����g�k�N�����O SINGLE �� MULTI �䤤���@
			if (item.getType().equalsIgnoreCase(SelectType.SINGLE.getType())
					|| item.getType().equalsIgnoreCase(SelectType.MULTI.getType())) {
				// �T�w�O��Φh��A�ﶵ�N�����n����
				// �e������ĸ��A��ܧ_�w���N��
				if (!StringUtils.hasText(item.getOptions())) {
					return new BasicRes(ResMessage.OPTIONS_ERROR.getCode(), //
							ResMessage.OPTIONS_ERROR.getMessage());
				}
			}
		}
		// �ˬd�n�ק諸�ݨ��O�_�w�s�b
		if (!quizDao.existsById(req.getId())) {
			return new BasicRes(ResMessage.QUIZ_NOT_FOUND.getCode(), //
					ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		quizDao.save(new Quiz(req.getId(), req.getName(), req.getDescription(), req.getStartDate(), //
				req.getEndDate(), req.isPublished()));
		// �R�����i�ݨ��Ҧ������D
		quesDao.deleteByQuizId(req.getId());
		// �s�W��s�᪺���D
		quesDao.saveAll(quesList);
		return new BasicRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage());
	}

	@Override
	public BasicRes delete(DeleteReq req) {
		// �i�椤���ݨ�����R��: ��X�n�R���� idList ���O�_���]�t�i�椤���ݨ�
		// �i�椤���ݨ�����: 1. �w�o�G �B 2. ��e�ɶ��j�󵥩�}�l��� �B 3. ��e�ɶ��p�󵥩󵲧����
		boolean res = quizDao.existsByIdInAndPublishedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(//
				req.getQuizIdList(), LocalDate.now(), LocalDate.now());
		if (res) { // ���P�� res == true�A��ܭn�R�����ݨ�ID�������b�i�椤��
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
			// quizName ���a�ȮɡA�N��N�O�D�j�M���󤧤@�A�ҥH��ȥi��|�O�Ŧr��άO null
			// ���ťդ]���O�D�j�M���󤧤@
			// �|�� quizName �ܦ��Ŧr��O�]�� containing�A��j�M�����ȱa�Ŧr��ɡA�|������
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
		// ��k 1: �ϥ� foreach �ھڨC�� quizId �h�������� Ques
		// ���I: �̾� Quiz ���h��M�w�s��DB������ƪ����ơF�ݨ��V�h�A�s��DB���ƴN�V�h
		for (Quiz item : res) {
			// �ھ� quizId �����C�i�ݨ��̪� quesList
			int quizId = item.getId();
			List<Ques> quesList = quesDao.findByQuizId(quizId);
			// �إߤ��P�ݨ��� QuizRes �Ψө� Quiz �M������ List<Ques>
			// �Y�ѼƤӦh�ϥΫغc��k�e���V�áA��ĳ�ΥH�U���覡
			QuizRes quizRes = new QuizRes();
			quizRes.setId(quizId);
			quizRes.setName(item.getName());
			quizRes.setDescription(item.getDescription());
			quizRes.setStartDate(item.getStartDate());
			quizRes.setEndDate(item.getEndDate());
			quizRes.setPublished(item.isPublished());
			quizRes.setQuesList(quesList);
			// ��C�i���P���ݨ�+���D��i List<QuizRes> ��
			quizResList.add(quizRes);
		}
		// ��k 2: ���`���Ҧ��ŦX�� quizId�A�A�@�������ŦX���󪺩Ҧ� Ques�F���ްݨ��h��A�T�w�N�O�s�� DB 2��
		List<Integer> quizIdList = new ArrayList<>();
		for (Quiz item : res) {
			quizIdList.add(item.getId());
		}
		List<Ques> quesList = quesDao.findByQuizIdIn(quizIdList);
		// �N�ŦX�j�M���� res(�Ҧ����ݨ�) �M quesList(�Ҧ������D) �t��
		List<QuizRes> quizResList2 = new ArrayList<>();
		for (Quiz item : res) {
			int quizId = item.getId();
			List<Ques> returnQuesList = new ArrayList<>();
			for (Ques quesItem : quesList) {
				// �P�_ Quiz �M Ques ���� quizId �O�_���ۦP
				if (quizId == quesItem.getQuizId()) {
					// quizId �@�ˡA�N�[�i�n��^�� QuesList ��
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
			// ��C�i���P���ݨ�+���D��i List<QuizRes> ��
			quizResList2.add(quizRes);
		}
//		return new SearchRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), quizResList2);
		return new SearchRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), quizResList);
	}

	@Override
	public BasicRes fillin(FillinReq req) {
		// ���ˬd req ���� List<Feedback>�A�Ҧ��� Feedback �� quizId �M email ���O�@�˪�
		// �N�O quizId �M email �u�|���@��
		List<Feedback> feedbackList = req.getFeedbackList();
		Set<Integer> quizIdSet = new HashSet<>();
		Set<String> emailSet = new HashSet<>();
		for (Feedback item : feedbackList) {
			quizIdSet.add(item.getQuizId());
			emailSet.add(item.getEmail());
		}
		// �]�� set ���Ȥ��|�����ơA�ҥH��� set ���j�p�����O1���ܴN��� quizId �� email ���@�P
		if (quizIdSet.size() != 1 || emailSet.size() != 1) {
			return new BasicRes(ResMessage.QUIZ_ID_OR_EMAIL_INCONSISTENT.getCode(), //
					ResMessage.QUIZ_ID_OR_EMAIL_INCONSISTENT.getMessage());
		}
		int quizId = feedbackList.get(0).getQuizId();
		// �ˬd�P�@�� email + quizId �O�_�w�s�b(�P�@�� email �w�g��g�L�P�@�i�ݨ�)
		if (feedbackDao.existsByQuizIdAndEmail(quizId, //
				req.getFeedbackList().get(0).getEmail())) {
			return new BasicRes(ResMessage.EMAIL_DUPLICATE.getCode(), //
					ResMessage.EMAIL_DUPLICATE.getMessage());
		}
		// �ˬd�ݨ��O�_�i�H��g�����A: 1. �w�o�G 2. ��e�ɶ��j�󵥩�}�l�ɶ� 3. ��e�ɶ��p�󵥩󵲧��ɶ�
		// �e������ĸ��A��ܧ䤣��w�o�G�B��e�ɶ��O����}�l�ɶ��P�����ɶ����������
		if (!quizDao.existsByIdInAndPublishedTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(//
				List.of(quizId), LocalDate.now(), LocalDate.now())) {
			return new BasicRes(ResMessage.CANNOT_FILLIN_QUIZ.getCode(), //
					ResMessage.CANNOT_FILLIN_QUIZ.getMessage());
		}
		// �ˬd����
		List<Ques> quesList = quesDao.findByQuizId(quizId);
		// �ˬd���D�M���ת����ƬO�_�@��
		if (feedbackList.size() != quesList.size()) {
			return new BasicRes(ResMessage.FILLIN_INCOMPLETE.getCode(), //
					ResMessage.FILLIN_INCOMPLETE.getMessage());
		}
		// ���`������ quId �M ��諸 quId �H�ΰ��D���s��
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
			// �ˬd���ת��D���O�_�P���D�D���۲�
			if (!quIds.contains(quId)) { // �e������ĸ��A��ܵ����D�����]�t�b���D�D����
				return new BasicRes(ResMessage.QUID_MISMATCH.getCode(), //
						ResMessage.QUID_MISMATCH.getMessage());
			}
			// �ˬd����B�n������
			// ������S����
			if (necessaryQuIds.contains(quId) && !StringUtils.hasText(item.getAns())) {
				return new BasicRes(ResMessage.FILLIN_IS_NECESSARY.getCode(), //
						ResMessage.FILLIN_IS_NECESSARY.getMessage());
			}
			List<String> ansList = List.of(item.getAns().split(";"));
			// �ˬd���ɡA���঳�h�ӵ���
			if (singleQuIds.contains(quId) && ansList.size() > 1) {
				return new BasicRes(ResMessage.SINGLE_CHOICE_QUES.getCode(), //
						ResMessage.SINGLE_CHOICE_QUES.getMessage());
			}
			// ���N�D���P���������׻`���A�Ψӫ����ﵪ�׻P�ﶵ��
			quIdAnsMap.put(quId, ansList);
		}
		// �ˬd�D��r�ɡA���׻P�ﶵ�n�@�P
		for (Ques item : quesList) {
			String type = item.getType();
			if (!type.equalsIgnoreCase(SelectType.TEXT.getType())) {
				// quIdAnsMap.get(quId): �O�ھ� key �Ө��o map �������� value
				List<String> ansList = quIdAnsMap.get(item.getId());
				List<String> optionList = List.of(item.getOptions().split(";"));
				// �ۦP�����D�A�P�_���׬O�_�P�ﶵ�@�P
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
		// ������ quiz ��������T
		Optional<Quiz> op = quizDao.findById(quizId);
		if (op.isEmpty()) {
			return new StatisticsRes(ResMessage.QUIZ_NOT_FOUND.getCode(), //
					ResMessage.QUIZ_NOT_FOUND.getMessage());
		}
		Quiz quiz = op.get();
		String quizName = quiz.getName();
		// ���q ques �����D��r���������D(��r���������D���C�J�έp)
		List<Ques> quesList = quesDao.findByQuizIdAndTypeNot(quizId, SelectType.TEXT.getType());
		// �`�����Φh����D�� Id�A����ΨӼ��� feedback ����ƥ�
		List<Integer> quIdList = new ArrayList<>();
		// ���`�����D�s���P�ﶵ�A����i�T�w feedback �������פ@�w�|�P���D�ﶵ�@�P�A�קK feedback �������׫D��Ӫ��ﶵ
		// ���D�s�� �ﶵ ����
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
		// �W���� for �j����椧��|�O�����H�U������
		// ���D�s�� �ﶵ ����
		// Map<Integer, Map<String, Integer>> quIdOptionCountMap = new HashMap<>();
		// 1 �J�� 0
		// 1 �T���v 0
		// 1 ���{ 0
		// 1 �~�� 0
		// �H�W�ۦP key ����ơA�u�|���@���A�N�O���D�s�� 1�A�������� value ���� map �N�|��4�Ӹ��
		// �i�H�g�� test ��ƴN�i�H�o�����Ʈ榡
		// 2 ���� 0
		// �H������
		// =========================
		//
		List<Feedback> feedbackList = feedbackDao.findByQuizIdAndQuIdIn(quizId, quIdList);
		// ==========================
		for (Feedback item : feedbackList) {
			// �ھڰ��D�s���� �ﶵ���ƪ� map �q quIdOptionCountMap �����X
			Map<String, Integer> optionCountMap = quIdOptionCountMap.get(item.getQuId());
			// �N feedback ���� ans �r���ন List
			List<String> ansList = List.of(item.getAns().split(";"));
			for (String ans : ansList) {
				// �ھڿﶵ���X����������
				int count = optionCountMap.get(ans);
				// �N�쥻������ +1
				count++;
				// ��ۦP�� �ﶵ-���� ��^�� map ��
				optionCountMap.put(ans, count);
			}
			// ��s quIdOptionCountMap �������
			quIdOptionCountMap.put(item.getQuId(), optionCountMap);
		}
		for (StatisticsVo item : statisticsList) {
			int quId = item.getQuId();
			Map<String, Integer> optionCountMap = quIdOptionCountMap.get(quId);
			item.setOptionCountMap(optionCountMap);
			// �W��3��{���X�i�ΤU���@����
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

		//3�����g�k
//		return quizId <= 0 ? new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
//				new ArrayList<>()) : new FeedbackRes(ResMessage.SUCCESS.getCode(), ResMessage.SUCCESS.getMessage(), //
//						feedbackDao.findByQuizId(quizId));
	}

}
