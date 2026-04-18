import * as React from "react"
import { cn } from "@/lib/utils"

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "default" | "outline" | "ghost" | "secondary"
  size?: "default" | "sm" | "lg" | "icon"
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "default", size = "default", ...props }, ref) => {
    const variants: Record<string, string> = {
      default: "bg-emerald-500 text-white hover:bg-emerald-400 shadow-lg shadow-emerald-500/20",
      outline: "border border-white/10 bg-white/5 text-slate-200 hover:bg-white/10 hover:border-white/20",
      ghost: "text-slate-400 hover:text-slate-200 hover:bg-white/5",
      secondary: "bg-white/5 text-slate-200 hover:bg-white/10",
    }

    const sizes: Record<string, string> = {
      default: "h-10 px-4 py-2",
      sm: "h-8 px-3 text-sm",
      lg: "h-12 px-6 text-base",
      icon: "h-10 w-10",
    }

    return (
      <button
        className={cn(
          "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-lg text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500/50 disabled:pointer-events-none disabled:opacity-50 cursor-pointer",
          variants[variant],
          sizes[size],
          className
        )}
        ref={ref}
        {...props}
      />
    )
  }
)
Button.displayName = "Button"

export { Button }
