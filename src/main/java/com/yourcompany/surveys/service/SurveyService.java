package com.yourcompany.surveys.service;

import com.yourcompany.surveys.dto.QuestionRequestDTO;
import com.yourcompany.surveys.dto.SurveyRequestDTO;
import com.yourcompany.surveys.dto.SurveyResponse;
import com.yourcompany.surveys.entity.Question;
import com.yourcompany.surveys.entity.QuestionType;
import com.yourcompany.surveys.entity.Survey;
import com.yourcompany.surveys.entity.User;
import com.yourcompany.surveys.mapper.QuestionMapper;
import com.yourcompany.surveys.mapper.SurveyMapper;
import com.yourcompany.surveys.repository.SurveyRepository;
import com.yourcompany.surveys.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyService {
    private final SurveyRepository surveyRepository;
    private final SurveyMapper surveyMapper;
    private final UserRepository userRepository;
    private final QuestionMapper questionMapper;

    public List<SurveyResponse> findAll() {
        List<Survey> surveys = surveyRepository.findAll();
        return surveys.stream()
                .map(surveyMapper::toResponse)
                .toList();
    }

    public Optional<SurveyResponse> findById(Long id) {
        Optional<Survey> survey = surveyRepository.findById(id);
        return survey.map(surveyMapper::toResponse);
    }

    public List<SurveyResponse> getByUser(Principal principal) {
        String username = principal.getName();
        Optional<User> user = userRepository.findByEmail(username);
        User creator = user.orElseThrow();
        List<Survey> surveys = surveyRepository.findByCreator(creator);
        return surveys.stream()
                .map(surveyMapper::toResponse)
                .toList();
    }

    @Transactional
    public SurveyResponse save(SurveyRequestDTO surveyRequest, Principal principal) {
        String username = principal.getName();
        Optional<User> user = userRepository.findByEmail(username);
        User creator = user.orElseThrow();
        Survey survey = surveyMapper.toEntity(surveyRequest);
        survey.setCreator(creator);
        survey = surveyRepository.save(survey);
        return surveyMapper.toResponse(survey);
    }

    public SurveyResponse update(Long id, SurveyRequestDTO surveyRequest) {
        Survey existingSurvey = surveyRepository.findById(id).orElseThrow();

        existingSurvey.setTitle(surveyRequest.title());
        existingSurvey.setDescription(surveyRequest.description());

        Map<Long, QuestionRequestDTO> requestQuestionsMap = surveyRequest.questions().stream()
                .collect(Collectors.toMap(QuestionRequestDTO::id, q -> q));

        Iterator<Question> existingQuestionsIterator = existingSurvey.getQuestions().iterator();
        while (existingQuestionsIterator.hasNext()) {
            Question existingQuestion = existingQuestionsIterator.next();

            if (requestQuestionsMap.containsKey(existingQuestion.getId())) {
                QuestionRequestDTO questionRequest = requestQuestionsMap.get(existingQuestion.getId());
                existingQuestion.setText(questionRequest.text());
                existingQuestion.setType(QuestionType.fromValue(questionRequest.type()));

                requestQuestionsMap.remove(existingQuestion.getId());
            } else {
                existingQuestionsIterator.remove();
            }
        }

        requestQuestionsMap.values().forEach(questionRequest -> {
            Question newQuestion = questionMapper.toEntity(questionRequest);
            newQuestion.setSurvey(existingSurvey);
            existingSurvey.getQuestions().add(newQuestion);
        });

        return surveyMapper.toResponse(surveyRepository.save(existingSurvey));
    }

    public void deleteById(Long id) {
        surveyRepository.deleteById(id);
    }
}