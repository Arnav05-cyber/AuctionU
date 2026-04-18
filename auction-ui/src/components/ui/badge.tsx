import * as React from "react"
import { cn } from "@/lib/utils"

export interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "secondary" | "outline" | "destructive" | "live"
}

function Badge({ className, variant = "default", ...props }: BadgeProps) {
  const variants: Record<string, string> = {
    default: "bg-emerald-500/15 text-emerald-400 border-emerald-500/30",
    secondary: "bg-white/5 text-slate-400 border-white/10",
    outline: "bg-transparent text-slate-300 border-white/15",
    destructive: "bg-red-500/15 text-red-400 border-red-500/30",
    live: "bg-emerald-500/20 text-emerald-400 border-emerald-500/40 animate-pulse",
  }

  return (
    <div
      className={cn(
        "inline-flex items-center rounded-md border px-2.5 py-0.5 text-xs font-semibold transition-colors",
        variants[variant],
        className
      )}
      {...props}
    />
  )
}

export { Badge }
