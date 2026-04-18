'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/context/auth-context'
import { Button } from '@/components/ui/button'
import { Gavel, Eye, EyeOff, Loader2 } from 'lucide-react'

export default function LoginPage() {
  const { login } = useAuth()
  const router = useRouter()

  const [form, setForm] = useState({ userName: '', password: '' })
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await login(form)
      router.push('/')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Login failed'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-[calc(100vh-56px)] flex items-center justify-center px-4 grid-bg">
      {/* Ambient glow */}
      <div className="fixed top-1/3 left-1/2 -translate-x-1/2 w-[500px] h-[500px] bg-emerald-500/[0.04] rounded-full blur-[120px] pointer-events-none" />

      <div className="relative z-10 w-full max-w-md">
        {/* Logo */}
        <div className="flex items-center justify-center gap-3 mb-8">
          <div className="w-12 h-12 rounded-xl bg-emerald-500/15 border border-emerald-500/25 flex items-center justify-center glow-pulse">
            <Gavel className="w-6 h-6 text-emerald-400" />
          </div>
          <h1 className="text-3xl font-bold text-white">
            Auction<span className="text-emerald-400">U</span>
          </h1>
        </div>

        {/* Card */}
        <div className="glass rounded-2xl p-8">
          <div className="text-center mb-6">
            <h2 className="text-xl font-semibold text-white mb-1">Welcome back</h2>
            <p className="text-sm text-muted-foreground">
              Sign in to your campus marketplace account
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">
                Username
              </label>
              <input
                type="text"
                value={form.userName}
                onChange={(e) => setForm({ ...form, userName: e.target.value })}
                required
                className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500/30 transition"
                placeholder="Enter your username"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">
                Password
              </label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  required
                  className="w-full h-11 px-4 pr-10 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 focus:border-emerald-500/30 transition"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  onClick={() => setShowPw(!showPw)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 cursor-pointer"
                >
                  {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            {error && (
              <div className="px-3 py-2 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-400">
                {error}
              </div>
            )}

            <Button type="submit" disabled={loading} className="w-full" size="lg">
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Sign In'}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-500">
            Don&apos;t have an account?{' '}
            <Link href="/signup" className="text-emerald-400 hover:text-emerald-300 font-medium">
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
