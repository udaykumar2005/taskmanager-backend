package com.uday.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByUser(User user);

    List<Task> findByUserOrderByOrderIndex(User user);

    Optional<Task> findByIdAndUser(int id, User user);

    List<Task> findByRecurringTrue();

    List<Task> findByRecurringTrueAndRecurrenceType(String recurrenceType);

}