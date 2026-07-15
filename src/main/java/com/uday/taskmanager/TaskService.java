package com.uday.taskmanager;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // Get logged-in user
    private User getCurrentUser() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        System.out.println("Current Username : " + username);

        User user = userRepository.findByUsername(username);

        System.out.println("Database User : " + user);

        return user;
    }

    // Get only current user's tasks
    public List<Task> getAllTasks() {

        User user = getCurrentUser();

        if (user == null) {
            System.out.println("ERROR: User not found in database!");
            throw new RuntimeException("User not found");
        }

        System.out.println("Fetching tasks for : " + user.getUsername());

        List<Task> tasks = taskRepository.findByUserOrderByOrderIndex(user);

        System.out.println("Tasks Found : " + tasks.size());

        return tasks;
    }

    // Add task
    public Task addTask(Task task) {

        User user = getCurrentUser();

        task.setUser(user);

        task.setOrderIndex(
                taskRepository.findByUser(user).size());

        if (task.getPriority() == null)
            task.setPriority("Medium");

        if (task.getCategory() == null)
            task.setCategory("General");

        if (task.getRecurrenceType() == null)
            task.setRecurrenceType("None");

        if (task.isRecurring()
                && "Weekly".equals(task.getRecurrenceType())) {

            task.setRecurrenceDay(
                    LocalDate.now().getDayOfWeek().name());
        }

        // Initialize completed date
        task.setCompletedDate(null);

        return taskRepository.save(task);
    }

    // Update task
    public Task updateTask(int id, Task updatedTask) {

        User user = getCurrentUser();

        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Basic Details
        task.setTitle(updatedTask.getTitle());
        task.setPriority(updatedTask.getPriority());
        task.setCategory(updatedTask.getCategory());

        // Due Date & Time
        task.setDueDate(updatedTask.getDueDate());
        task.setDueTime(updatedTask.getDueTime());

        // Notes
        task.setNotes(updatedTask.getNotes());

        // Completion
        task.setCompleted(updatedTask.isCompleted());

        if (updatedTask.isCompleted()) {
            task.setCompletedDate(LocalDate.now());
        } else {
            task.setCompletedDate(null);
        }

        // Recurring Settings
        task.setRecurring(updatedTask.isRecurring());
        task.setRecurrenceType(updatedTask.getRecurrenceType());

        if (updatedTask.isRecurring()) {

            if ("Weekly".equals(updatedTask.getRecurrenceType())) {

                if (task.getRecurrenceDay() == null) {
                    task.setRecurrenceDay(
                            LocalDate.now().getDayOfWeek().name());
                }

            } else {
                // Daily recurring
                task.setRecurrenceDay(null);
            }

        } else {

            task.setRecurrenceDay(null);

        }

        // Order
        task.setOrderIndex(updatedTask.getOrderIndex());

        return taskRepository.save(task);
    }

    // Delete task
    public void deleteTask(int id) {

        User user = getCurrentUser();

        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        taskRepository.delete(task);
    }

    public Task snoozeTask(int id) {

        User user = getCurrentUser();

        Task task = taskRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setSnoozeUntil(LocalDateTime.now().plusMinutes(1));

        return taskRepository.save(task);
    }
}