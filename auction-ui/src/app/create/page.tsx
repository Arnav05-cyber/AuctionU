'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { motion } from 'framer-motion'
import { authFetch } from '@/lib/api'
import { useAuth } from '@/context/auth-context'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import type { Product } from '@/types/auction'
import {
  Gavel, ShoppingCart, Loader2, ArrowLeft, Plus,
} from 'lucide-react'

type FormSaleType = 'AUCTION' | 'FIXED_PRICE'

export default function CreateListingPage() {
  const { isAuthenticated, loading: authLoading } = useAuth()
  const router = useRouter()

  const [saleType, setSaleType] = useState<FormSaleType>('AUCTION')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const [form, setForm] = useState({
    title: '',
    description: '',
    category: '',
    startingPrice: '',
    auctionEndTime: '',
    buyItNowPrice: '',
    quantity: '1',
  })

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push('/login')
  }, [authLoading, isAuthenticated, router])

  const update = (field: string, value: string) => setForm({ ...form, [field]: value })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const body: Record<string, unknown> = {
        title: form.title,
        description: form.description || null,
        category: form.category || null,
        type: saleType,
        sellerId: 'placeholder', // Gateway injects X-User-Id
      }

      if (saleType === 'AUCTION') {
        body.startingPrice = parseFloat(form.startingPrice)
        body.auctionEndTime = new Date(form.auctionEndTime).toISOString().replace('Z', '')
      } else {
        body.buyItNowPrice = parseFloat(form.buyItNowPrice)
        body.quantity = parseInt(form.quantity)
      }

      await authFetch<Product>('/api/v1/products/create', {
        method: 'POST',
        body: JSON.stringify(body),
      })

      router.push(saleType === 'AUCTION' ? '/' : '/marketplace')
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create listing')
    } finally {
      setLoading(false)
    }
  }

  if (authLoading || !isAuthenticated) {
    return <div className="min-h-[calc(100vh-56px)] flex items-center justify-center"><Loader2 className="w-6 h-6 text-emerald-400 animate-spin" /></div>
  }

  return (
    <div className="relative min-h-[calc(100vh-56px)] grid-bg">
      <div className="fixed top-1/4 right-1/3 w-[500px] h-[500px] bg-emerald-500/[0.03] rounded-full blur-[120px] pointer-events-none" />

      <div className="relative z-10 max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <button onClick={() => router.back()} className="flex items-center gap-2 text-sm text-slate-400 hover:text-slate-200 mb-6 transition cursor-pointer">
          <ArrowLeft className="w-4 h-4" />Back
        </button>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="glass rounded-2xl p-8">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/15 border border-emerald-500/25 flex items-center justify-center">
              <Plus className="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-white">Create Listing</h1>
              <p className="text-sm text-muted-foreground">List an item for auction or fixed-price sale</p>
            </div>
          </div>

          {/* Sale type toggle */}
          <div className="flex items-center gap-2 mb-6 p-1 rounded-xl bg-white/[0.03] border border-white/[0.06]">
            {(['AUCTION', 'FIXED_PRICE'] as const).map((type) => (
              <button
                key={type}
                type="button"
                onClick={() => setSaleType(type)}
                className={`flex-1 flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg text-sm font-medium transition-all cursor-pointer ${
                  saleType === type
                    ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/25'
                    : 'text-slate-400 hover:text-slate-200 border border-transparent'
                }`}
              >
                {type === 'AUCTION' ? <Gavel className="w-4 h-4" /> : <ShoppingCart className="w-4 h-4" />}
                {type === 'AUCTION' ? 'Auction' : 'Fixed Price'}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Title *</label>
              <input type="text" value={form.title} onChange={(e) => update('title', e.target.value)} required
                className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                placeholder="MacBook Pro 16 — Barely Used" />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Description</label>
              <textarea value={form.description} onChange={(e) => update('description', e.target.value)} rows={3}
                className="w-full px-4 py-3 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition resize-none"
                placeholder="Describe your item..." />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Category</label>
              <input type="text" value={form.category} onChange={(e) => update('category', e.target.value)}
                className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition"
                placeholder="Electronics, Books, Furniture..." />
            </div>

            {saleType === 'AUCTION' ? (
              <>
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Starting Price ($) *</label>
                  <input type="number" value={form.startingPrice} onChange={(e) => update('startingPrice', e.target.value)} required min="1" step="0.01"
                    className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition tabular-nums"
                    placeholder="100" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Auction End Time *</label>
                  <input type="datetime-local" value={form.auctionEndTime} onChange={(e) => update('auctionEndTime', e.target.value)} required
                    className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition" />
                </div>
              </>
            ) : (
              <>
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Price ($) *</label>
                  <input type="number" value={form.buyItNowPrice} onChange={(e) => update('buyItNowPrice', e.target.value)} required min="1" step="0.01"
                    className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition tabular-nums"
                    placeholder="50" />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-400 mb-1.5 uppercase tracking-wider">Quantity *</label>
                  <input type="number" value={form.quantity} onChange={(e) => update('quantity', e.target.value)} required min="1"
                    className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition tabular-nums"
                    placeholder="1" />
                </div>
              </>
            )}

            {error && (
              <div className="px-3 py-2 rounded-lg bg-red-500/10 border border-red-500/20 text-xs text-red-400">{error}</div>
            )}

            <Button type="submit" disabled={loading} className="w-full" size="lg">
              {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : (
                <><Plus className="w-4 h-4" />Create {saleType === 'AUCTION' ? 'Auction' : 'Listing'}</>
              )}
            </Button>
          </form>
        </motion.div>
      </div>
    </div>
  )
}
