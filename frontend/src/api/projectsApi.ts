import { apiClient } from "./client";
import type { CreateProjectRequest, Project } from "../types";

export const projectsApi = {
  async createProject(payload: CreateProjectRequest): Promise<Project> {
    const response = await apiClient.post<Project>("/projects", payload);
    return response.data;
  },

  async getProjects(): Promise<Project[]> {
    const response = await apiClient.get<Project[]>("/projects");
    return response.data;
  },

  async getProjectById(id: number): Promise<Project> {
    const response = await apiClient.get<Project>(`/projects/${id}`);
    return response.data;
  },

  async getProjectsByOwnerId(ownerId: number): Promise<Project[]> {
    const response = await apiClient.get<Project[]>(`/projects/owner/${ownerId}`);
    return response.data;
  },

  async deleteProject(id: number): Promise<void> {
    await apiClient.delete(`/projects/${id}`);
  }
};
