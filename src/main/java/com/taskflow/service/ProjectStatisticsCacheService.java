package com.taskflow.service;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatisticsCacheService {

    private final CacheManager cacheManager;

    public void evictProjectStatistics(Long projectId) {
        Cache cache = cacheManager.getCache("projectStatistics");

        if (cache != null) {
            cache.evict(projectId);
            log.info("Evicted statistics cache for project id: {}", projectId);
        }
    }
}
