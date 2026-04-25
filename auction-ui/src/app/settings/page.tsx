'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { motion } from 'framer-motion'
import { useAuth } from '@/context/auth-context'
import { Button } from '@/components/ui/button'
import { Loader2, Settings, User, AlertTriangle, LogOut } from 'lucide-react'

export default function SettingsPage() {
  const { user, loading, isAuthenticated, deleteAccount, logout } = useAuth()
  const router = useRouter()
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!loading && !isAuthenticated) {
      router.push('/login')
    }
  }, [loading, isAuthenticated, router])

  const handleDelete = async () => {
    const confirmMessage = 'Are you absolutely sure you want to delete your account? This action is permanent and will remove all your data, including listings and bids. Type "DELETE" to confirm.'
    const input = prompt(confirmMessage)
    
    if (input !== 'DELETE') {
      if (input !== null) {
        alert('Account deletion cancelled. You must type "DELETE" exactly.')
      }
      return
    }

    setDeleting(true)
    setError('')
    try {
      await deleteAccount()
      // Context deleteAccount clears tokens and state. We can redirect to home.
      router.push('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete account')
      setDeleting(false)
    }
  }

  if (loading || !isAuthenticated) {
    return (
      <div className="min-h-[calc(100vh-56px)] flex items-center justify-center">
        <Loader2 className="w-6 h-6 text-emerald-400 animate-spin" />
      </div>
    )
  }

  return (
    <div className="relative min-h-[calc(100vh-56px)] grid-bg py-12">
      <div className="fixed top-0 right-1/3 w-[600px] h-[600px] bg-emerald-500/[0.03] rounded-full blur-[120px] pointer-events-none" />

      <div className="relative z-10 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        <header className="mb-8 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-white/[0.03] border border-white/[0.06] flex items-center justify-center">
            <Settings className="w-5 h-5 text-emerald-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">Account Settings</h1>
            <p className="text-sm text-muted-foreground">Manage your profile and account preferences.</p>
          </div>
        </header>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="space-y-6">
          {/* Profile Section */}
          <div className="glass rounded-2xl p-6">
            <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
              <User className="w-5 h-5 text-emerald-500" />
              Profile Details
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-muted-foreground mb-1">Username</label>
                <div className="px-4 py-2.5 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200">
                  {user?.username}
                </div>
              </div>
              <div>
                <label className="block text-xs font-medium text-muted-foreground mb-1">User ID</label>
                <div className="px-4 py-2.5 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-400 font-mono">
                  {user?.userId}
                </div>
              </div>
            </div>
          </div>

          {/* Danger Zone */}
          <div className="rounded-2xl bg-red-500/[0.02] border border-red-500/10 p-6">
            <h2 className="text-lg font-semibold text-red-400 mb-4 flex items-center gap-2">
              <AlertTriangle className="w-5 h-5" />
              Danger Zone
            </h2>
            
            <div className="space-y-4">
              <div className="flex flex-col sm:flex-row gap-4 sm:items-center justify-between p-4 rounded-xl bg-white/[0.02] border border-white/[0.04]">
                <div>
                  <h3 className="text-sm font-medium text-white">Log Out</h3>
                  <p className="text-xs text-slate-400 mt-1">Sign out of your account on this device.</p>
                </div>
                <Button variant="outline" onClick={logout} className="shrink-0 w-full sm:w-auto">
                  <LogOut className="w-4 h-4 mr-2" />
                  Log Out
                </Button>
              </div>

              <div className="flex flex-col sm:flex-row gap-4 sm:items-center justify-between p-4 rounded-xl bg-red-500/[0.04] border border-red-500/20">
                <div>
                  <h3 className="text-sm font-medium text-red-400">Delete Account</h3>
                  <p className="text-xs text-red-400/70 mt-1 max-w-sm">
                    Permanently delete your account and all associated data. This action cannot be undone.
                  </p>
                </div>
                <Button 
                  variant="outline" 
                  onClick={handleDelete} 
                  disabled={deleting}
                  className="shrink-0 w-full sm:w-auto border-red-500/20 text-red-400 hover:bg-red-500/10 hover:border-red-500/30"
                >
                  {deleting ? (
                    <Loader2 className="w-4 h-4 animate-spin mr-2" />
                  ) : (
                    <AlertTriangle className="w-4 h-4 mr-2" />
                  )}
                  {deleting ? 'Deleting...' : 'Delete Account'}
                </Button>
              </div>
              
              {error && (
                <div className="px-4 py-3 rounded-xl bg-red-500/10 border border-red-500/20 text-sm text-red-400">
                  {error}
                </div>
              )}
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
