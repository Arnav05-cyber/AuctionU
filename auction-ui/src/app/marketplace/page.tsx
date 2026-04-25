'use client'

import { useState, useEffect, useMemo } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { AuctionCard } from '@/components/ui/auction-card'
import { authFetch, ApiError } from '@/lib/api'
import { useAuth } from '@/context/auth-context'
import { useRouter } from 'next/navigation'
import type { Product } from '@/types/auction'
import {
  Search, SlidersHorizontal, Store, Loader2, Gavel,
} from 'lucide-react'

type SortOption = 'price-low' | 'price-high' | 'newest'

export default function MarketplacePage() {
  const { isAuthenticated, loading: authLoading } = useAuth()
  const router = useRouter()
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState<SortOption>('newest')

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push('/login')
  }, [authLoading, isAuthenticated, router])

  useEffect(() => {
    if (!isAuthenticated) return
    async function fetch() {
      try {
        const data = await authFetch<Product[]>('/api/v1/products/marketplace')
        setProducts(data)
      } catch (err) {
        if (err instanceof ApiError) setError(err.message)
        else setError('Failed to load marketplace')
      } finally { setLoading(false) }
    }
    fetch()
  }, [isAuthenticated])

  const filtered = useMemo(() => {
    let list = products.filter((p) => !search || p.title.toLowerCase().includes(search.toLowerCase()))
    list = [...list].sort((a, b) => {
      switch (sort) {
        case 'price-low': return (a.buyItNowPrice ?? 0) - (b.buyItNowPrice ?? 0)
        case 'price-high': return (b.buyItNowPrice ?? 0) - (a.buyItNowPrice ?? 0)
        case 'newest': return b.id - a.id
        default: return 0
      }
    })
    return list
  }, [products, search, sort])

  if (authLoading || !isAuthenticated) {
    return <div className="min-h-[calc(100vh-56px)] flex items-center justify-center"><Loader2 className="w-6 h-6 text-emerald-400 animate-spin" /></div>
  }

  return (
    <div className="relative min-h-[calc(100vh-56px)] grid-bg">
      <div className="fixed top-0 right-1/4 w-[500px] h-[500px] bg-emerald-500/[0.03] rounded-full blur-[120px] pointer-events-none" />
      <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <header className="mb-8">
          <div className="flex items-center gap-3 mb-2">
            <Store className="w-6 h-6 text-emerald-400" />
            <h1 className="text-2xl font-bold text-white">Marketplace</h1>
          </div>
          <p className="text-sm text-muted-foreground">Browse fixed-price items from your campus community.</p>
        </header>

        <div className="mb-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          <div className="relative w-full sm:max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input type="text" placeholder="Search items..." value={search} onChange={(e) => setSearch(e.target.value)}
              className="w-full h-10 pl-10 pr-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition" />
          </div>
          <div className="flex items-center gap-1.5 glass rounded-xl px-3 py-1.5">
            <SlidersHorizontal className="w-3.5 h-3.5 text-muted-foreground" />
            <select value={sort} onChange={(e) => setSort(e.target.value as SortOption)} className="bg-transparent text-xs text-slate-300 outline-none cursor-pointer">
              <option value="newest" className="bg-[#0a0a0a]">Newest First</option>
              <option value="price-low" className="bg-[#0a0a0a]">Price: Low → High</option>
              <option value="price-high" className="bg-[#0a0a0a]">Price: High → Low</option>
            </select>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-24"><Loader2 className="w-8 h-8 text-emerald-400 animate-spin" /></div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center py-24 text-center"><p className="text-red-400 text-sm">{error}</p></div>
        ) : filtered.length === 0 ? (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="flex flex-col items-center justify-center py-24 text-center">
            <div className="w-16 h-16 rounded-2xl bg-white/[0.03] border border-white/[0.06] flex items-center justify-center mb-4"><Search className="w-7 h-7 text-muted-foreground" /></div>
            <h3 className="text-lg font-semibold text-slate-200 mb-1">No items found</h3>
            <p className="text-sm text-muted-foreground max-w-xs">Try adjusting your search or list something new.</p>
          </motion.div>
        ) : (
          <motion.div layout className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            <AnimatePresence mode="popLayout">
              {filtered.map((product) => (
                <AuctionCard key={product.id} product={product} onDelete={(id) => setProducts((prev) => prev.filter((p) => p.id !== id))} />
              ))}
            </AnimatePresence>
          </motion.div>
        )}

        <footer className="mt-16 pb-8 flex items-center justify-center gap-2 text-xs text-slate-600">
          <Gavel className="w-3 h-3" /><span>AuctionU — Campus Marketplace</span>
        </footer>
      </div>
    </div>
  )
}
