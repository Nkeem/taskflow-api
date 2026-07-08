import { apiClient } from "./client";
import type {
  CreateTaskRequest,
  Task,
  TaskPriority,
  TaskStatus,
  UpdateTaskRequest,
  UpdateTaskStatusRequest
} from "../types";

export interface TaskFilters {
  status?: TaskStatus;
  priority?: TaskPriority;
}

export const tasksApi = {
  async createTask(projectId: number, payload: CreateTaskRequest): Promise<Task> {
    const response = await apiClient.post<Task>(`/projects/${projectId}/tasks`, payload);
    return response.data;
  },

  async getTasksByProject(projectId: number, filters: TaskFilters = {}): Promise<Task[]> {
    const response = await apiClient.get<Task[]>(`/projects/${projectId}/tasks`, {
      params: filters
    });
    return response.data;
  },

  async getTaskById(id: number): Promise<Task> {
    const response = await apiClient.get<Task>(`/tasks/${id}`);
    return response.data;
  },

  async updateTask(id: number, payload: UpdateTaskRequest): Promise<Task> {
    const response = await apiClient.patch<Task>(`/tasks/${id}`, payload);
    return response.data;
  },

  async updateTaskStatus(id: number, payload: UpdateTaskStatusRequest): Promise<Task> {
    const response = await apiClient.patch<Task>(`/tasks/${id}/status`, payload);
    return response.data;
  },

  async deleteTask(id: number): Promise<void> {
    await apiClient.delete(`/tasks/${id}`);
  }
};
