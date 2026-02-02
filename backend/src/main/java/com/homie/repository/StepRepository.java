package com.homie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.homie.model.Step;

public interface StepRepository extends JpaRepository<Step, Long> {
    
}
