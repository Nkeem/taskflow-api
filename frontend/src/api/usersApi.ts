import { apiClient } from "./client";
import type { CreateUserRequest, User } from "../types";

export const usersApi = {
  async createUser(payload: CreateUserRequest): Promise<User> {
    const response = await apiClient.post<User>("/users", payload);
    return response.data;
  },

  async getUsers(): Promise<User[]> {
    const response = await apiClient.get<User[]>("/users");
    return response.data;
  },

  async getUserById(id: number): Promise<User> {
    const response = await apiClient.get<User>(`/users/${id}`);
    return response.data;
  }
};
