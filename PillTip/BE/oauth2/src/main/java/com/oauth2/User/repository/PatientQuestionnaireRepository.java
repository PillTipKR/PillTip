package com.oauth2.User.repository;

import com.oauth2.User.entity.PatientQuestionnaire;
import com.oauth2.User.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientQuestionnaireRepository extends JpaRepository<PatientQuestionnaire, Integer> {
    List<PatientQuestionnaire> findByUser(User user);
} 