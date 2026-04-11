package com.taskflow.task.dto;

import java.util.List;
import java.util.Map;

public record TaskStatsResponse(
        Map<String, Long> byStatus,
        List<AssigneeStats> byAssignee,
        long total
) {
    public record AssigneeStats(
            String userId,   // null = unassigned
            String name,
            long count
    ) {}
}