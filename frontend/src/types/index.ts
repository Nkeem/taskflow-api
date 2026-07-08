export interface User {
  id: number;
  username: string;
  email: string;
  createdAt: string;
}

export interface Project {
  id: number;
  name: string;
  description: string;
  owner: User;
  createdAt: string;
  updatedAt: string;
}

export type TaskStatus = "TODO" | "IN_PROGRESS" | "DONE";

export type TaskPriority = "LOW" | "MEDIUM" | "HIGH";

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  deadline: string;
  projectId: number;
  assignee: User | null;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectStatistics {
  projectId: number;
  totalTasks: number;
  todo: number;
  inProgress: number;
  done: number;
  highPriority: number;
  overdue: number;
}

export interface ErrorResponse {
  code: string;
  message: string;
}

export interface CreateUserRequest {
  username: string;
  email: string;
}

export interface CreateProjectRequest {
  name: string;
  description: string;
  ownerId: number;
}

export interface CreateTaskRequest {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  deadline: string;
  assigneeId?: number | null;
}

export interface UpdateTaskRequest {
  title?: string;
  description?: string;
  priority?: TaskPriority;
  deadline?: string;
  assigneeId?: number | null;
}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}
