package com.taskflow.common;

import java.util.List;

public record PagedResponse<T>(
        List<T> data,
        int page,
        int limit,
        long total,
        int totalPages
) {
    public static <T> PagedResponse<T> of(List<T> data, int page, int limit, long total) {
        int totalPages = (int) Math.ceil((double) total / limit);
        return new PagedResponse<>(data, page, limit, total, totalPages);
    }
}