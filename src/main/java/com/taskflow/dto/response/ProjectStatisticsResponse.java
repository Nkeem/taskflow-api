package com.taskflow.dto.response;

import java.io.Serializable;

public record ProjectStatisticsResponse(
        Long projectId,
        long totalTasks,
        long todo,
        long inProgress,
        long done,
        long highPriority,
        long overdue
) implements Serializable {
}
