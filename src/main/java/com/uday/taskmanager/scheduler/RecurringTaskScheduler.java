package com.uday.taskmanager.scheduler;

import com.uday.taskmanager.Task;
import com.uday.taskmanager.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;

    public RecurringTaskScheduler(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetRecurringTasks() {

        List<Task> tasks = taskRepository.findByRecurringTrue();

        LocalDate today = LocalDate.now();
        String todayDay = today.getDayOfWeek().name();

        for (Task task : tasks) {

            // Reset only completed recurring tasks
            if (!task.isCompleted()) {
                continue;
            }

            // ---------- DAILY ----------
            if ("Daily".equals(task.getRecurrenceType())) {

                task.setCompleted(false);
                task.setCompletedDate(null);

                if (task.getDueDate() != null) {
                    task.setDueDate(task.getDueDate().plusDays(1));
                }

                task.setSnoozeUntil(null);

                taskRepository.save(task);
            }

            // ---------- WEEKLY ----------
            else if ("Weekly".equals(task.getRecurrenceType())) {

                if (todayDay.equals(task.getRecurrenceDay())) {

                    task.setCompleted(false);
                    task.setCompletedDate(null);

                    if (task.getDueDate() != null) {
                        task.setDueDate(task.getDueDate().plusWeeks(1));
                    }

                    task.setSnoozeUntil(null);

                    taskRepository.save(task);
                }
            }
        }
    }
}