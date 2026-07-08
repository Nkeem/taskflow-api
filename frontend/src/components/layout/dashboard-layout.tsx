import type { ReactNode } from "react";
import { Activity, BarChart3, FolderKanban, LayoutDashboard, ListTodo } from "lucide-react";

import { Badge } from "../ui/badge";

const navigation = [
  { label: "Dashboard", icon: LayoutDashboard, active: true },
  { label: "Projects", icon: FolderKanban },
  { label: "Tasks", icon: ListTodo },
  { label: "Statistics", icon: BarChart3 },
  { label: "Events", icon: Activity }
];

interface DashboardLayoutProps {
  children: ReactNode;
}

export function DashboardLayout({ children }: DashboardLayoutProps) {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <aside className="fixed inset-y-0 left-0 hidden w-64 border-r border-slate-200 bg-white px-4 py-5 lg:block">
        <div className="flex items-center gap-3 px-2">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-950 text-white">
            <FolderKanban className="h-5 w-5" aria-hidden="true" />
          </div>
          <div>
            <p className="text-base font-semibold leading-tight">TaskFlow</p>
            <p className="text-xs text-slate-500">Project dashboard</p>
          </div>
        </div>

        <nav className="mt-8 space-y-1">
          {navigation.map((item) => {
            const Icon = item.icon;
            return (
              <button
                key={item.label}
                type="button"
                className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
                  item.active
                    ? "bg-slate-950 text-white"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-950"
                }`}
              >
                <Icon className="h-4 w-4" aria-hidden="true" />
                {item.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <div className="lg:pl-64">
        <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/90 px-4 py-4 backdrop-blur md:px-8">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p className="text-sm font-medium text-slate-500">TaskFlow Dashboard</p>
              <h1 className="text-xl font-semibold tracking-normal text-slate-950">
                Project and task overview
              </h1>
            </div>
            <Badge>Backend API: localhost:8080</Badge>
          </div>
        </header>

        <main className="px-4 py-6 md:px-8">{children}</main>
      </div>
    </div>
  );
}
