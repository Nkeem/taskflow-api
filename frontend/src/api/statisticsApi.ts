import { apiClient } from "./client";
import type { ProjectStatistics } from "../types";

export const statisticsApi = {
  async getProjectStatistics(projectId: number): Promise<ProjectStatistics> {
    const response = await apiClient.get<ProjectStatistics>(`/projects/${projectId}/stats`);
    return response.data;
  }
};
