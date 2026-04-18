'use client'

import { useState, useEffect, useMemo } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { AuctionCard } from '@/components/ui/auction-card'
import { Badge } from '@/components/ui/badge'
import { useAuctionSocket } from '@/hooks/use-auction-socket'
import { authFetch, ApiError } from '@/lib/api'
import { useAuth } from '@/context/auth-context'
import { useRouter } from 'next/navigation'
import type { Product } from '@/types/auction'
import {
  Activity,
  Search,
  SlidersHorizontal,
  Wifi,
  WifiOff,
  Flame,
  Clock,
  LayoutGrid,
  Loader2,
  Gavel,
} from 'lucide-react'

type SortOption = "ending" | "price-high" | "price-low" | "newest"

export default function AuctionsPage() {
  const { isAuthenticated, loading: authLoading } = useAuth()
  const router = useRouter()
  const [auctions, setAuctions] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState<SortOption>('ending')

  // Get product IDs for WebSocket subscriptions
  const productIds = useMemo(() => auctions.map((a) => a.id), [auctions])
  const { bidUpdates, connected } = useAuctionSocket(productIds)

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login')
    }
  }, [authLoading, isAuthenticated, router])

  useEffect(() => {
    if (!isAuthenticated) return

    async function fetchAuctions() {
      try {
        const data = await authFetch<Product[]>('/api/v1/products/auction')
        setAuctions(data)
      } catch (err) {
        if (err instanceof ApiError) setError(err.message)
        else setError('Failed to load auctions')
      } finally {
        setLoading(false)
      }
    }
    fetchAuctions()
  }, [isAuthenticated])

  const filtered = useMemo(() => {
    let list = auctions.filter((a) => {
      if (search && !a.title.toLowerCase().includes(search.toLowerCase())) return false
      return true
    })

    list = [...list].sort((a, b) => {
      switch (sort) {
        case 'ending':
          return new Date(a.auctionEndTime ?? 0).getTime() - new Date(b.auctionEndTime ?? 0).getTime()
        case 'price-high':
          return (b.currentHighestBid ?? 0) - (a.currentHighestBid ?? 0)
        case 'price-low':
          return (a.currentHighestBid ?? 0) - (b.currentHighestBid ?? 0)
        case 'newest':
          return b.id - a.id
        default:
          return 0
      }
    })

    return list
  }, [auctions, search, sort])

  const activeCount = auctions.filter((a) => a.status === 'ACTIVE' && !a.isExpired).length

  if (authLoading || (!isAuthenticated && !authLoading)) {
    return (
      <div className="min-h-[calc(100vh-56px)] flex items-center justify-center">
        <Loader2 className="w-6 h-6 text-emerald-400 animate-spin" />
      </div>
    )
  }

  return (
    <div className="relative min-h-[calc(100vh-56px)] grid-bg">
      {/* Ambient gradient blobs */}
      <div className="fixed top-0 left-1/4 w-[600px] h-[600px] bg-emerald-500/[0.03] rounded-full blur-[120px] pointer-events-none" />
      <div className="fixed bottom-0 right-1/4 w-[400px] h-[400px] bg-emerald-600/[0.02] rounded-full blur-[100px] pointer-events-none" />

      <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <header className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-white mb-1">Live Auctions</h1>
              <p className="text-sm text-muted-foreground">Bid on items from your campus community in real-time.</p>
            </div>
            <div className="flex items-center gap-4 glass rounded-xl px-4 py-2.5">
              <div className="flex items-center gap-2 text-sm">
                <Activity className="w-4 h-4 text-emerald-500" />
                <span className="text-slate-300 font-medium">{activeCount}</span>
                <span className="text-muted-foreground">Live</span>
              </div>
              <div className="w-px h-4 bg-white/10" />
              <div className="flex items-center gap-1.5 text-sm">
                {connected ? (
                  <><Wifi className="w-3.5 h-3.5 text-emerald-500" /><span className="text-emerald-400 text-xs font-medium">Connected</span></>
                ) : (
                  <><WifiOff className="w-3.5 h-3.5 text-red-400" /><span className="text-red-400 text-xs font-medium">Offline</span></>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Toolbar */}
        <div className="mb-6 flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          <div className="relative w-full sm:max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <input type="text" placeholder="Search auctions..." value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full h-10 pl-10 pr-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 placeholder:text-slate-600 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition" />
          </div>
          <div className="flex items-center gap-1.5 glass rounded-xl px-3 py-1.5">
            <SlidersHorizontal className="w-3.5 h-3.5 text-muted-foreground" />
            <select value={sort} onChange={(e) => setSort(e.target.value as SortOption)}
              className="bg-transparent text-xs text-slate-300 outline-none cursor-pointer">
              <option value="ending" className="bg-[#0a0a0a]">Ending Soon</option>
              <option value="price-high" className="bg-[#0a0a0a]">Price: High → Low</option>
              <option value="price-low" className="bg-[#0a0a0a]">Price: Low → High</option>
              <option value="newest" className="bg-[#0a0a0a]">Newest First</option>
            </select>
          </div>
        </div>

        {/* Live indicator */}
        {connected && (
          <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
            className="mb-6 flex items-center gap-2 px-4 py-2 rounded-xl glass text-xs text-emerald-400">
            <span className="relative flex h-1.5 w-1.5">
              <span className="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 live-dot" />
              <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-emerald-500" />
            </span>
            Live feed active — bids update in real-time
          </motion.div>
        )}

        {/* Content */}
        {loading ? (
          <div className="flex items-center justify-center py-24">
            <Loader2 className="w-8 h-8 text-emerald-400 animate-spin" />
          </div>
        ) : error ? (
          <div className="flex flex-col items-center justify-center py-24 text-center">
            <p className="text-red-400 text-sm">{error}</p>
          </div>
        ) : filtered.length === 0 ? (
          <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
            className="flex flex-col items-center justify-center py-24 text-center">
            <div className="w-16 h-16 rounded-2xl bg-white/[0.03] border border-white/[0.06] flex items-center justify-center mb-4">
              <Search className="w-7 h-7 text-muted-foreground" />
            </div>
            <h3 className="text-lg font-semibold text-slate-200 mb-1">No auctions found</h3>
            <p className="text-sm text-muted-foreground max-w-xs">Try adjusting your search or check back later.</p>
          </motion.div>
        ) : (
          <motion.div layout className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            <AnimatePresence mode="popLayout">
              {filtered.map((product) => (
                <AuctionCard key={product.id} product={product} liveBid={bidUpdates.get(product.id)} />
              ))}
            </AnimatePresence>
          </motion.div>
        )}

        {/* Footer */}
        <footer className="mt-16 pb-8 flex items-center justify-center gap-2 text-xs text-slate-600">
          <Gavel className="w-3 h-3" />
          <span>AuctionU — Campus Marketplace</span>
        </footer>
      </div>
    </div>
  )
}
