'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useAuth } from '@/context/auth-context'
import { Button } from '@/components/ui/button'
import { Gavel, Eye, EyeOff, Loader2, Check, X } from 'lucide-react'

const PW_RULES = [
  { label: 'Uppercase letter', test: (p: string) => /[A-Z]/.test(p) },
  { label: 'Lowercase letter', test: (p: string) => /[a-z]/.test(p) },
  { label: 'Digit', test: (p: string) => /\d/.test(p) },
  { label: 'Special char (!@#$%^&*()-+)', test: (p: string) => /[!@#$%^&*()\-+]/.test(p) },
  { label: 'Min 4 characters', test: (p: string) => p.length >= 4 },
]

export default function SignupPage() {
  const { signup, verifySignup, login } = useAuth()
  const router = useRouter()

  const [form, setForm] = useState({
    userName: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
  })
  const [showPw, setShowPw] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [step, setStep] = useState<'form' | 'otp'>('form')
  const [otp, setOtp] = useState('')

  const emailValid = /^[A-Za-z0-9+_.-]+@snu\.edu\.in$/.test(form.email)
  const allPwRulesPass = PW_RULES.every((r) => r.test(form.password))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    if (!emailValid) {
      setError('Email must end with @snu.edu.in')
      return
    }
    if (!allPwRulesPass) {
      setError('Password does not meet all requirements')
      return
    }

    setLoading(true)
    try {
      await signup(form)
      setStep('otp')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Signup failed'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await verifySignup(form.email, otp)
      await login({ userName: form.email, password: form.password })
      router.push('/')
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Invalid or expired verification code'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  const update = (field: string, value: string) => setForm({ ...form, [field]: value })

  return (
    <div className="min-h-[calc(100vh-56px)] flex items-center justify-center px-4 py-12 grid-bg">
      <div className="fixed top-1/4 left-1/2 -translate-x-1/2 w-[500px] h-[500px] bg-emerald-500/[0.04] rounded-full blur-[120px] pointer-events-none" />

      <div className="relative z-10 w-full max-w-lg">
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
            <h2 className="text-xl font-semibold text-white mb-1">
              {step === 'form' ? 'Create your account' : 'Check your email'}
            </h2>
            <p className="text-sm text-muted-foreground">
              {step === 'form' ? (
                <>Join your campus marketplace — only <span className="text-emerald-400 font-medium">@snu.edu.in</span> emails allowed</>
              ) : (
                <>We've sent a 6-digit verification code to <span className="text-white">{form.email}</span></>
              )}
            </p>
          </div>

          {step === 'form' ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              {/* Name row */}
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">First Name</label>
                <input type="text" value={form.firstName} onChange={(e) => update('firstName', e.target.value)} required
                  className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                  placeholder="Arnav" />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Last Name</label>
                <input type="text" value={form.lastName} onChange={(e) => update('lastName', e.target.value)} required
                  className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                  placeholder="Kumar" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Username</label>
              <input type="text" value={form.userName} onChange={(e) => update('userName', e.target.value)} required
                className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                placeholder="arnav05" />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Email</label>
              <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required
                className={`w-full h-11 px-4 rounded-xl bg-white/[0.03] border text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 transition ${
                  form.email && !emailValid ? 'border-red-500/40 focus:ring-red-500/30' : 'border-white/[0.06] focus:ring-emerald-500/30'
                }`}
                placeholder="yourname@snu.edu.in" />
              {form.email && !emailValid && (
                <p className="mt-1 text-xs text-red-400">Must be an @snu.edu.in email</p>
              )}
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Phone</label>
              <input type="tel" value={form.phoneNumber} onChange={(e) => update('phoneNumber', e.target.value)} required
                className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                placeholder="+91 9876543210" />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Password</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={form.password}
                  onChange={(e) => update('password', e.target.value)}
                  required
                  className="w-full h-11 px-4 pr-10 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                  placeholder="Strong password"
                />
                <button type="button" onClick={() => setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 cursor-pointer">
                  {showPw ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>

              {/* Password strength indicators */}
              {form.password && (
                <div className="mt-2 space-y-1">
                  {PW_RULES.map((rule) => {
                    const passes = rule.test(form.password)
                    return (
                      <div key={rule.label} className="flex items-center gap-1.5 text-xs">
                        {passes ? (
                          <Check className="w-3 h-3 text-emerald-400" />
                        ) : (
                          <X className="w-3 h-3 text-slate-600" />
                        )}
                        <span className={passes ? 'text-emerald-400' : 'text-slate-600'}>{rule.label}</span>
                      </div>
                    )
                  })}
                </div>
              )}
            </div>

            {error && (
              <div className="px-3 py-2 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-400">
                {error}
              </div>
            )}

            <Button type="submit" disabled={loading || !emailValid || !allPwRulesPass} className="w-full" size="lg">
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Create Account'}
            </Button>
          </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider text-center">Verification Code</label>
                <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6}
                  className="w-full h-12 px-4 text-center tracking-[0.5em] text-xl rounded-xl bg-white/[0.03] border border-white/[0.06] text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                  placeholder="------" />
              </div>

              {error && (
                <div className="px-3 py-2 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-400">
                  {error}
                </div>
              )}

              <Button type="submit" disabled={loading || otp.length !== 6} className="w-full" size="lg">
                {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Verify & Join'}
              </Button>
              <button 
                type="button" 
                onClick={() => setStep('form')} 
                className="w-full mt-3 text-xs text-slate-500 hover:text-white transition"
              >
                Back to registration
              </button>
            </form>
          )}

          <p className="mt-6 text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link href="/login" className="text-emerald-400 hover:text-emerald-300 font-medium">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
