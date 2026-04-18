"use client"

import { AuthProvider } from "@/context/auth-context"
import { Navbar } from "@/components/ui/navbar"

export function Providers({ children }: { children: React.ReactNode }) {
  return (
    <AuthProvider>
      <Navbar />
      <main className="flex-1">{children}</main>
    </AuthProvider>
  )
}
