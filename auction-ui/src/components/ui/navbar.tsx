'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { useAuth } from '@/context/auth-context'
import { Button } from '@/components/ui/button'
import {
  Gavel,
  Store,
  Plus,
  LogOut,
  User,
  Loader2,
} from 'lucide-react'

const NAV_ITEMS = [
  { href: '/', label: 'Auctions', icon: Gavel },
  { href: '/marketplace', label: 'Marketplace', icon: Store },
]

export function Navbar() {
  const { user, loading, logout, isAuthenticated } = useAuth()
  const pathname = usePathname()

  // Don't show navbar on auth pages
  if (pathname === '/login' || pathname === '/signup') return null

  return (
    <nav className="sticky top-0 z-50 w-full border-b border-white/[0.06] bg-[#050505]/80 backdrop-blur-xl">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          {/* ── Left: Logo + Nav Links ── */}
          <div className="flex items-center gap-6">
            <Link href="/" className="flex items-center gap-2.5 group">
              <div className="w-8 h-8 rounded-lg bg-emerald-500/15 border border-emerald-500/25 flex items-center justify-center group-hover:bg-emerald-500/25 transition">
                <Gavel className="w-4 h-4 text-emerald-400" />
              </div>
              <span className="text-lg font-bold text-white tracking-tight">
                Auction<span className="text-emerald-400">U</span>
              </span>
            </Link>

            <div className="hidden sm:flex items-center gap-1">
              {NAV_ITEMS.map(({ href, label, icon: Icon }) => {
                const active = pathname === href
                return (
                  <Link
                    key={href}
                    href={href}
                    className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-all ${
                      active
                        ? 'bg-emerald-500/10 text-emerald-400'
                        : 'text-slate-400 hover:text-slate-200 hover:bg-white/5'
                    }`}
                  >
                    <Icon className="w-3.5 h-3.5" />
                    {label}
                  </Link>
                )
              })}
            </div>
          </div>

          {/* ── Right: Actions ── */}
          <div className="flex items-center gap-3">
            {loading ? (
              <Loader2 className="w-4 h-4 text-slate-400 animate-spin" />
            ) : isAuthenticated ? (
              <>
                <Link href="/create">
                  <Button size="sm" className="gap-1.5">
                    <Plus className="w-3.5 h-3.5" />
                    <span className="hidden sm:inline">List Item</span>
                  </Button>
                </Link>

                <div className="flex items-center gap-2 glass rounded-lg px-3 py-1.5">
                  <User className="w-3.5 h-3.5 text-emerald-400" />
                  <span className="text-xs text-slate-300 font-medium max-w-[100px] truncate">
                    {user?.username}
                  </span>
                </div>

                <Button variant="ghost" size="icon" onClick={logout} title="Logout">
                  <LogOut className="w-4 h-4" />
                </Button>
              </>
            ) : (
              <>
                <Link href="/login">
                  <Button variant="outline" size="sm">Sign In</Button>
                </Link>
                <Link href="/signup">
                  <Button size="sm">Sign Up</Button>
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
