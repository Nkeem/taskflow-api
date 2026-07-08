package com.taskflow.dto.response;

public record ProjectStatisticsResponse(
        Long projectId,
        long totalTasks,
        long todo,
        long inProgress,
        long done,
        long highPriority,
        long overdue
) {
}
