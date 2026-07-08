import { Activity, BarChart3, CheckCircle2, FolderKanban, ListTodo, Server } from "lucide-react";

import { DashboardLayout } from "../components/layout/dashboard-layout";
import { Badge } from "../components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";

const metrics = [
  {
    title: "Projects",
    value: "Ready",
    description: "Create, browse, and delete project records.",
    icon: FolderKanban
  },
  {
    title: "Tasks",
    value: "Tracked",
    description: "Manage task status, priority, assignee, and deadline.",
    icon: ListTodo
  },
  {
    title: "Statistics",
    value: "Cached",
    description: "Project task summaries are served through Redis caching.",
    icon: BarChart3
  },
  {
    title: "Kafka Events",
    value: "Enabled",
    description: "Task status changes publish events to the backend topic.",
    icon: Activity
  }
];

export function DashboardPage() {
  return (
    <DashboardLayout>
      <section className="space-y-6">
        <div className="rounded-lg border border-slate-200 bg-white p-5 shadow-soft">
          <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div className="max-w-2xl">
              <div className="mb-3 flex flex-wrap items-center gap-2">
                <Badge className="bg-emerald-50 text-emerald-700">Demo UI</Badge>
                <Badge>Vite + React + TypeScript</Badge>
              </div>
              <h2 className="text-2xl font-semibold tracking-normal text-slate-950">
                Clean frontend foundation for TaskFlow
              </h2>
              <p className="mt-2 text-sm leading-6 text-slate-600">
                Connect to the backend API to manage projects and tasks.
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

        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {metrics.map((metric) => {
            const Icon = metric.icon;
            return (
              <Card key={metric.title}>
                <CardHeader className="flex flex-row items-center justify-between space-y-0">
                  <CardTitle>{metric.title}</CardTitle>
                  <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
                    <Icon className="h-4 w-4" aria-hidden="true" />
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-2xl font-semibold tracking-normal text-slate-950">
                    {metric.value}
                  </p>
                  <p className="mt-2 text-sm leading-5 text-slate-600">{metric.description}</p>
                </CardContent>
              </Card>
            );
          })}
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Next UI milestones</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid gap-3 md:grid-cols-3">
              {["Project list", "Task board", "Statistics panel"].map((item) => (
                <div
                  key={item}
                  className="flex items-center gap-3 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3"
                >
                  <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
                  <span className="text-sm font-medium text-slate-700">{item}</span>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </section>
    </DashboardLayout>
  );
}
