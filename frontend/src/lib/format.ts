import type { TaskPriority, TaskStatus } from "../types";

export function formatDateTime(value?: string | null): string {
  if (!value) {
    return "No deadline";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("en", {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  }).format(date);
}

export function formatStatus(status: TaskStatus): string {
  const labels: Record<TaskStatus, string> = {
    TODO: "Todo",
    IN_PROGRESS: "In progress",
    DONE: "Done"
  };

  return labels[status];
}

export function formatPriority(priority: TaskPriority): string {
  const labels: Record<TaskPriority, string> = {
    LOW: "Low",
    MEDIUM: "Medium",
    HIGH: "High"
  };

  return labels[priority];
}
