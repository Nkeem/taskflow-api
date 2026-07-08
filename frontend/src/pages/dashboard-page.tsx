import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import {
  Activity,
  BarChart3,
  Database,
  FolderKanban,
  ListTodo,
  RefreshCcw,
  Server,
  Trash2,
  UserPlus
} from "lucide-react";

import { getApiErrorMessage } from "../api/apiError";
import { apiClient } from "../api/client";
import { projectsApi } from "../api/projectsApi";
import { statisticsApi } from "../api/statisticsApi";
import { TaskFilters, tasksApi } from "../api/tasksApi";
import { usersApi } from "../api/usersApi";
import { DashboardLayout } from "../components/layout/dashboard-layout";
import { Alert } from "../components/ui/alert";
import { Badge } from "../components/ui/badge";
import { Button } from "../components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import { Input } from "../components/ui/input";
import { Select } from "../components/ui/select";
import { Textarea } from "../components/ui/textarea";
import { formatDateTime, formatPriority, formatStatus } from "../lib/format";
import type {
  Project,
  ProjectStatistics,
  Task,
  TaskPriority,
  TaskStatus,
  User
} from "../types";

type HealthStatus = "checking" | "up" | "down";
type StatusFilter = "ALL" | TaskStatus;
type PriorityFilter = "ALL" | TaskPriority;

interface TaskFormState {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  deadline: string;
}

const defaultTaskForm: TaskFormState = {
  title: "",
  description: "",
  status: "TODO",
  priority: "MEDIUM",
  deadline: "2027-01-01T10:00"
};

const emptyStatistics: ProjectStatistics = {
  projectId: 0,
  totalTasks: 0,
  todo: 0,
  inProgress: 0,
  done: 0,
  highPriority: 0,
  overdue: 0
};

const statusOptions: TaskStatus[] = ["TODO", "IN_PROGRESS", "DONE"];
const priorityOptions: TaskPriority[] = ["LOW", "MEDIUM", "HIGH"];

function normalizeDeadline(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

function getStatusBadgeClass(status: TaskStatus): string {
  const classes: Record<TaskStatus, string> = {
    TODO: "bg-slate-50 text-slate-700",
    IN_PROGRESS: "bg-blue-50 text-blue-700",
    DONE: "bg-emerald-50 text-emerald-700"
  };

  return classes[status];
}

function getPriorityBadgeClass(priority: TaskPriority): string {
  const classes: Record<TaskPriority, string> = {
    LOW: "bg-slate-50 text-slate-700",
    MEDIUM: "bg-amber-50 text-amber-700",
    HIGH: "bg-rose-50 text-rose-700"
  };

  return classes[priority];
}

export function DashboardPage() {
  const [healthStatus, setHealthStatus] = useState<HealthStatus>("checking");
  const [users, setUsers] = useState<User[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [statistics, setStatistics] = useState<ProjectStatistics | null>(null);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>("ALL");
  const [priorityFilter, setPriorityFilter] = useState<PriorityFilter>("ALL");
  const [initialLoading, setInitialLoading] = useState(true);
  const [projectLoading, setProjectLoading] = useState(false);
  const [setupLoading, setSetupLoading] = useState(false);
  const [taskSubmitting, setTaskSubmitting] = useState(false);
  const [rowActionId, setRowActionId] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [activityMessages, setActivityMessages] = useState<string[]>([]);
  const [taskForm, setTaskForm] = useState<TaskFormState>(defaultTaskForm);

  const selectedProject = useMemo(
    () => projects.find((project) => project.id === selectedProjectId) ?? null,
    [projects, selectedProjectId]
  );

  const selectedUser = useMemo(
    () => users.find((user) => user.id === selectedUserId) ?? null,
    [users, selectedUserId]
  );

  const addActivity = useCallback((message: string) => {
    const time = new Intl.DateTimeFormat("en", {
      hour: "2-digit",
      minute: "2-digit",
      second: "2-digit"
    }).format(new Date());

    setActivityMessages((current) => [`${time} - ${message}`, ...current].slice(0, 5));
  }, []);

  const checkHealth = useCallback(async () => {
    setHealthStatus("checking");

    try {
      await apiClient.get("/health");
      setHealthStatus("up");
      addActivity("Backend health check passed");
    } catch {
      setHealthStatus("down");
      addActivity("Backend health check failed");
    }
  }, [addActivity]);

  const getFilters = useCallback((): TaskFilters => {
    const filters: TaskFilters = {};

    if (statusFilter !== "ALL") {
      filters.status = statusFilter;
    }

    if (priorityFilter !== "ALL") {
      filters.priority = priorityFilter;
    }

    return filters;
  }, [priorityFilter, statusFilter]);

  const loadProjectData = useCallback(
    async (projectId: number) => {
      setProjectLoading(true);
      setErrorMessage(null);

      try {
        const [projectTasks, projectStatistics] = await Promise.all([
          tasksApi.getTasksByProject(projectId, getFilters()),
          statisticsApi.getProjectStatistics(projectId)
        ]);

        setTasks(projectTasks);
        setStatistics(projectStatistics);
        addActivity("Statistics refreshed");
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setProjectLoading(false);
      }
    },
    [addActivity, getFilters]
  );

  useEffect(() => {
    async function loadInitialData() {
      setInitialLoading(true);
      setErrorMessage(null);

      await checkHealth();

      try {
        const [loadedUsers, loadedProjects] = await Promise.all([
          usersApi.getUsers(),
          projectsApi.getProjects()
        ]);

        setUsers(loadedUsers);
        setProjects(loadedProjects);
        setSelectedUserId(loadedUsers[0]?.id ?? null);
        setSelectedProjectId(loadedProjects[0]?.id ?? null);
      } catch (error) {
        setErrorMessage(getApiErrorMessage(error));
      } finally {
        setInitialLoading(false);
      }
    }

    void loadInitialData();
  }, [checkHealth]);

  useEffect(() => {
    if (selectedProjectId) {
      void loadProjectData(selectedProjectId);
    } else {
      setTasks([]);
      setStatistics(null);
    }
  }, [loadProjectData, selectedProjectId]);

  async function handleCreateDemoUser() {
    setSetupLoading(true);
    setErrorMessage(null);

    try {
      const timestamp = Date.now();
      const createdUser = await usersApi.createUser({
        username: `demo_${timestamp}`,
        email: `demo_${timestamp}@example.com`
      });

      setUsers((current) => [createdUser, ...current]);
      setSelectedUserId(createdUser.id);
      addActivity("Demo user created");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setSetupLoading(false);
    }
  }

  async function handleCreateProject() {
    if (!selectedUserId) {
      setErrorMessage("Create or select a user first.");
      return;
    }

    setSetupLoading(true);
    setErrorMessage(null);

    try {
      const createdProject = await projectsApi.createProject({
        name: "TaskFlow Demo Project",
        description: "Demo project created from React dashboard",
        ownerId: selectedUserId
      });

      setProjects((current) => [createdProject, ...current]);
      setSelectedProjectId(createdProject.id);
      addActivity("Project created for selected user");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setSetupLoading(false);
    }
  }

  function handleProjectChange(projectId: number | null) {
    setSelectedProjectId(projectId);

    if (projectId) {
      addActivity("Project selected");
    }
  }

  async function handleCreateTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!selectedProjectId) {
      setErrorMessage("Create or select a project first.");
      return;
    }

    if (!selectedUserId) {
      setErrorMessage("Create or select a user first.");
      return;
    }

    setTaskSubmitting(true);
    setErrorMessage(null);

    try {
      await tasksApi.createTask(selectedProjectId, {
        ...taskForm,
        deadline: normalizeDeadline(taskForm.deadline),
        assigneeId: selectedUserId
      });

      setTaskForm(defaultTaskForm);
      await loadProjectData(selectedProjectId);
      addActivity("Task created. Statistics cache was evicted on backend.");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setTaskSubmitting(false);
    }
  }

  async function handleTaskStatusChange(taskId: number, status: TaskStatus) {
    if (!selectedProjectId) {
      return;
    }

    setRowActionId(taskId);
    setErrorMessage(null);

    try {
      await tasksApi.updateTaskStatus(taskId, { status });
      await loadProjectData(selectedProjectId);
      addActivity("Task status changed. Kafka event was published and statistics cache was evicted.");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setRowActionId(null);
    }
  }

  async function handleDeleteTask(taskId: number) {
    if (!selectedProjectId) {
      return;
    }

    setRowActionId(taskId);
    setErrorMessage(null);

    try {
      await tasksApi.deleteTask(taskId);
      await loadProjectData(selectedProjectId);
      addActivity("Task deleted. Statistics cache was evicted.");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setRowActionId(null);
    }
  }

  const shownStatistics = statistics ?? emptyStatistics;
  const healthLabel =
    healthStatus === "up"
      ? "Backend online"
      : healthStatus === "down"
        ? "Backend offline"
        : "Checking backend";

  return (
    <DashboardLayout>
      <section className="space-y-6">
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-soft">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div className="max-w-2xl">
              <div className="mb-3 flex flex-wrap items-center gap-2">
                <Badge className={healthStatus === "up" ? "bg-emerald-50 text-emerald-700" : ""}>
                  {healthLabel}
                </Badge>
                <Badge>Functional demo UI</Badge>
              </div>
              <h2 className="text-2xl font-semibold tracking-normal text-slate-950">
                Manage TaskFlow projects and tasks
              </h2>
              <p className="mt-2 text-sm leading-6 text-slate-600">
                Create demo data, filter tasks, refresh statistics, and trigger backend Redis/Kafka
                behavior from one dashboard.
              </p>
            </div>
            <div className="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
              <Server className="h-5 w-5 text-slate-500" aria-hidden="true" />
              <div>
                <p className="text-sm font-medium text-slate-950">API proxy</p>
                <p className="text-xs text-slate-500">/api {"->"} localhost:8080</p>
              </div>
            </div>
          </div>
        </div>

        {errorMessage && <Alert>{errorMessage}</Alert>}

        <div className="grid gap-4 xl:grid-cols-[1fr_360px]">
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Quick setup</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 lg:grid-cols-2">
                  <div className="space-y-3">
                    <label className="text-sm font-medium text-slate-700" htmlFor="user-select">
                      User
                    </label>
                    <Select
                      id="user-select"
                      value={selectedUserId ?? ""}
                      onChange={(event) =>
                        setSelectedUserId(event.target.value ? Number(event.target.value) : null)
                      }
                    >
                      <option value="">No user selected</option>
                      {users.map((user) => (
                        <option key={user.id} value={user.id}>
                          {user.username} - {user.email}
                        </option>
                      ))}
                    </Select>
                    <Button
                      className="w-full"
                      type="button"
                      onClick={handleCreateDemoUser}
                      disabled={setupLoading}
                    >
                      <UserPlus className="mr-2 h-4 w-4" aria-hidden="true" />
                      Create demo user
                    </Button>
                  </div>

                  <div className="space-y-3">
                    <label className="text-sm font-medium text-slate-700" htmlFor="project-select">
                      Project
                    </label>
                    <Select
                      id="project-select"
                      value={selectedProjectId ?? ""}
                      onChange={(event) =>
                        handleProjectChange(event.target.value ? Number(event.target.value) : null)
                      }
                    >
                      <option value="">No project selected</option>
                      {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                          {project.name}
                        </option>
                      ))}
                    </Select>
                    <Button
                      className="w-full"
                      type="button"
                      onClick={handleCreateProject}
                      disabled={setupLoading || !selectedUserId}
                    >
                      <FolderKanban className="mr-2 h-4 w-4" aria-hidden="true" />
                      Create project for user
                    </Button>
                  </div>
                </div>

                <div className="mt-4 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600">
                  Selected context:{" "}
                  <span className="font-medium text-slate-950">
                    {selectedUser?.username ?? "No user"}
                  </span>{" "}
                  /{" "}
                  <span className="font-medium text-slate-950">
                    {selectedProject?.name ?? "No project"}
                  </span>
                </div>
              </CardContent>
            </Card>

            <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {[
                { label: "Total tasks", value: shownStatistics.totalTasks, icon: ListTodo },
                { label: "Todo", value: shownStatistics.todo, icon: ListTodo },
                { label: "In progress", value: shownStatistics.inProgress, icon: RefreshCcw },
                { label: "Done", value: shownStatistics.done, icon: BarChart3 },
                { label: "High priority", value: shownStatistics.highPriority, icon: Activity },
                { label: "Overdue", value: shownStatistics.overdue, icon: Database }
              ].map((item) => {
                const Icon = item.icon;
                return (
                  <Card key={item.label}>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0">
                      <CardTitle>{item.label}</CardTitle>
                      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                        <Icon className="h-4 w-4" aria-hidden="true" />
                      </div>
                    </CardHeader>
                    <CardContent>
                      <p className="text-2xl font-semibold tracking-normal text-slate-950">
                        {selectedProjectId ? item.value : "-"}
                      </p>
                    </CardContent>
                  </Card>
                );
              })}
            </div>

            <Card>
              <CardHeader>
                <CardTitle>Create task</CardTitle>
              </CardHeader>
              <CardContent>
                <form className="grid gap-4 lg:grid-cols-2" onSubmit={handleCreateTask}>
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-700" htmlFor="task-title">
                      Title
                    </label>
                    <Input
                      id="task-title"
                      value={taskForm.title}
                      onChange={(event) =>
                        setTaskForm((current) => ({ ...current, title: event.target.value }))
                      }
                      placeholder="Prepare dashboard demo"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-700" htmlFor="task-deadline">
                      Deadline
                    </label>
                    <Input
                      id="task-deadline"
                      type="datetime-local"
                      value={taskForm.deadline}
                      onChange={(event) =>
                        setTaskForm((current) => ({ ...current, deadline: event.target.value }))
                      }
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-700" htmlFor="task-status">
                      Status
                    </label>
                    <Select
                      id="task-status"
                      value={taskForm.status}
                      onChange={(event) =>
                        setTaskForm((current) => ({
                          ...current,
                          status: event.target.value as TaskStatus
                        }))
                      }
                    >
                      {statusOptions.map((status) => (
                        <option key={status} value={status}>
                          {formatStatus(status)}
                        </option>
                      ))}
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-slate-700" htmlFor="task-priority">
                      Priority
                    </label>
                    <Select
                      id="task-priority"
                      value={taskForm.priority}
                      onChange={(event) =>
                        setTaskForm((current) => ({
                          ...current,
                          priority: event.target.value as TaskPriority
                        }))
                      }
                    >
                      {priorityOptions.map((priority) => (
                        <option key={priority} value={priority}>
                          {formatPriority(priority)}
                        </option>
                      ))}
                    </Select>
                  </div>

                  <div className="space-y-2 lg:col-span-2">
                    <label
                      className="text-sm font-medium text-slate-700"
                      htmlFor="task-description"
                    >
                      Description
                    </label>
                    <Textarea
                      id="task-description"
                      value={taskForm.description}
                      onChange={(event) =>
                        setTaskForm((current) => ({
                          ...current,
                          description: event.target.value
                        }))
                      }
                      placeholder="Short task details"
                    />
                  </div>

                  <div className="lg:col-span-2">
                    <Button
                      type="submit"
                      disabled={taskSubmitting || !selectedProjectId || !selectedUserId}
                    >
                      Create task
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          </div>

          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle>Activity</CardTitle>
              </CardHeader>
              <CardContent>
                {activityMessages.length === 0 ? (
                  <p className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
                    Activity messages will appear here.
                  </p>
                ) : (
                  <div className="space-y-2">
                    {activityMessages.map((message) => (
                      <div
                        key={message}
                        className="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-600"
                      >
                        {message}
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Backend signals</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3 text-sm text-slate-600">
                  <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                    Redis statistics cache is refreshed when tasks change.
                  </div>
                  <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
                    Kafka task status events are published by the backend.
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        <Card>
          <CardHeader className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <CardTitle>Tasks</CardTitle>
              <p className="mt-1 text-sm text-slate-500">
                {selectedProject
                  ? `Showing tasks for ${selectedProject.name}`
                  : "Select or create a project to load tasks."}
              </p>
            </div>

            <div className="grid gap-2 sm:grid-cols-2">
              <Select
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value as StatusFilter)}
              >
                <option value="ALL">All statuses</option>
                {statusOptions.map((status) => (
                  <option key={status} value={status}>
                    {formatStatus(status)}
                  </option>
                ))}
              </Select>
              <Select
                value={priorityFilter}
                onChange={(event) => setPriorityFilter(event.target.value as PriorityFilter)}
              >
                <option value="ALL">All priorities</option>
                {priorityOptions.map((priority) => (
                  <option key={priority} value={priority}>
                    {formatPriority(priority)}
                  </option>
                ))}
              </Select>
            </div>
          </CardHeader>
          <CardContent>
            {initialLoading || projectLoading ? (
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                Loading dashboard data...
              </div>
            ) : tasks.length === 0 ? (
              <div className="rounded-lg border border-slate-200 bg-slate-50 px-4 py-8 text-center text-sm text-slate-500">
                No tasks match the current project and filters.
              </div>
            ) : (
              <div className="overflow-hidden rounded-lg border border-slate-200">
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[820px] text-left text-sm">
                    <thead className="bg-slate-50 text-xs uppercase text-slate-500">
                      <tr>
                        <th className="px-4 py-3 font-medium">Title</th>
                        <th className="px-4 py-3 font-medium">Status</th>
                        <th className="px-4 py-3 font-medium">Priority</th>
                        <th className="px-4 py-3 font-medium">Deadline</th>
                        <th className="px-4 py-3 font-medium">Assignee</th>
                        <th className="px-4 py-3 font-medium">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-200 bg-white">
                      {tasks.map((task) => (
                        <tr key={task.id}>
                          <td className="px-4 py-3">
                            <p className="font-medium text-slate-950">{task.title}</p>
                            <p className="mt-1 line-clamp-1 text-xs text-slate-500">
                              {task.description || "No description"}
                            </p>
                          </td>
                          <td className="px-4 py-3">
                            <Badge className={getStatusBadgeClass(task.status)}>
                              {formatStatus(task.status)}
                            </Badge>
                          </td>
                          <td className="px-4 py-3">
                            <Badge className={getPriorityBadgeClass(task.priority)}>
                              {formatPriority(task.priority)}
                            </Badge>
                          </td>
                          <td className="px-4 py-3 text-slate-600">
                            {formatDateTime(task.deadline)}
                          </td>
                          <td className="px-4 py-3 text-slate-600">
                            {task.assignee?.username ?? "Unassigned"}
                          </td>
                          <td className="px-4 py-3">
                            <div className="flex items-center gap-2">
                              <Select
                                className="h-9 min-w-32"
                                value={task.status}
                                disabled={rowActionId === task.id}
                                onChange={(event) =>
                                  void handleTaskStatusChange(
                                    task.id,
                                    event.target.value as TaskStatus
                                  )
                                }
                              >
                                {statusOptions.map((status) => (
                                  <option key={status} value={status}>
                                    {formatStatus(status)}
                                  </option>
                                ))}
                              </Select>
                              <Button
                                type="button"
                                variant="danger"
                                className="h-9 px-3"
                                disabled={rowActionId === task.id}
                                onClick={() => void handleDeleteTask(task.id)}
                              >
                                <Trash2 className="h-4 w-4" aria-hidden="true" />
                              </Button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </section>
    </DashboardLayout>
  );
}
