'use client'

import { useState, useEffect, use } from 'react'
import { useRouter } from 'next/navigation'
import { motion } from 'framer-motion'
import { authFetch, ApiError } from '@/lib/api'
import { useAuth } from '@/context/auth-context'
import { useCountdown } from '@/hooks/use-countdown'
import { useAuctionSocket } from '@/hooks/use-auction-socket'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import type { Product } from '@/types/auction'
import {
  ArrowLeft, Gavel, ShoppingCart, Timer, TrendingUp, Package,
  Zap, Loader2, User, Clock, Trash2,
} from 'lucide-react'

export default function ProductDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const { isAuthenticated, loading: authLoading, user } = useAuth()
  const router = useRouter()
  const [product, setProduct] = useState<Product | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [bidAmount, setBidAmount] = useState('')
  const [purchaseQty, setPurchaseQty] = useState(1)
  const [actionLoading, setActionLoading] = useState(false)
  const [actionMsg, setActionMsg] = useState('')
  const [deleteLoading, setDeleteLoading] = useState(false)

  const { bidUpdates, subscribeTo, connected } = useAuctionSocket()
  const liveBid = bidUpdates.get(Number(id))

  const countdown = useCountdown(product?.auctionEndTime ?? new Date().toISOString())
  const isAuction = product?.saleType === 'AUCTION'
  const currentBid = liveBid ?? (product?.currentHighestBid ?? 0)
  const isLive = product?.status === 'ACTIVE' && (isAuction ? !countdown.expired : true)

  useEffect(() => {
    if (!authLoading && !isAuthenticated) router.push('/login')
  }, [authLoading, isAuthenticated, router])

  useEffect(() => {
    if (!isAuthenticated) return
    async function fetch() {
      try {
        const data = await authFetch<Product>(`/api/v1/products/${id}`)
        setProduct(data)
        setBidAmount(String((data.currentHighestBid ?? 0) + 10))
      } catch (err) {
        if (err instanceof ApiError) setError(err.message)
        else setError('Failed to load product')
      } finally { setLoading(false) }
    }
    fetch()
  }, [isAuthenticated, id])

  // Subscribe to WebSocket for this product
  useEffect(() => {
    if (connected && product) subscribeTo(product.id)
  }, [connected, product, subscribeTo])

  // Update bid input when live bid changes
  useEffect(() => {
    if (liveBid) setBidAmount(String(liveBid + 10))
  }, [liveBid])

  const handleBid = async () => {
    setActionLoading(true)
    setActionMsg('')
    try {
      await authFetch(`/api/v1/products/${id}/bid`, {
        method: 'POST',
        body: JSON.stringify({ amount: parseFloat(bidAmount) }),
      })
      setActionMsg('Bid placed successfully!')
    } catch (err) {
      setActionMsg(err instanceof Error ? err.message : 'Bid failed')
    } finally { setActionLoading(false) }
  }

  const handlePurchase = async () => {
    setActionLoading(true)
    setActionMsg('')
    try {
      await authFetch(`/api/v1/products/${id}/purchase`, {
        method: 'POST',
        body: JSON.stringify({ quantity: purchaseQty }),
      })
      setActionMsg('Purchase successful!')
      // Refresh product
      const updated = await authFetch<Product>(`/api/v1/products/${id}`)
      setProduct(updated)
    } catch (err) {
      setActionMsg(err instanceof Error ? err.message : 'Purchase failed')
    } finally { setActionLoading(false) }
  }

  const handleDelete = async () => {
    if (!confirm('Are you sure you want to delete this listing? This cannot be undone.')) return
    setDeleteLoading(true)
    try {
      await authFetch(`/api/v1/products/${id}`, { method: 'DELETE' })
      router.push(isAuction ? '/' : '/marketplace')
    } catch (err) {
      setActionMsg(err instanceof Error ? err.message : 'Delete failed')
      setDeleteLoading(false)
    }
  }

  if (authLoading || loading) {
    return <div className="min-h-[calc(100vh-56px)] flex items-center justify-center"><Loader2 className="w-6 h-6 text-emerald-400 animate-spin" /></div>
  }
  if (error || !product) {
    return (
      <div className="min-h-[calc(100vh-56px)] flex flex-col items-center justify-center gap-4 text-center">
        <p className="text-red-400">{error || 'Product not found'}</p>
        <Button variant="outline" onClick={() => router.back()}><ArrowLeft className="w-4 h-4 mr-2" />Go Back</Button>
      </div>
    )
  }

  const segments = countdown.expired
    ? [{ label: 'ENDED', value: '--' }]
    : [
        ...(countdown.days > 0 ? [{ label: 'Days', value: String(countdown.days).padStart(2, '0') }] : []),
        { label: 'Hours', value: String(countdown.hours).padStart(2, '0') },
        { label: 'Min', value: String(countdown.minutes).padStart(2, '0') },
        { label: 'Sec', value: String(countdown.seconds).padStart(2, '0') },
      ]

  return (
    <div className="relative min-h-[calc(100vh-56px)] grid-bg">
      <div className="fixed top-0 left-1/3 w-[500px] h-[500px] bg-emerald-500/[0.03] rounded-full blur-[120px] pointer-events-none" />

      <div className="relative z-10 max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Back button */}
        <button onClick={() => router.back()} className="flex items-center gap-2 text-sm text-slate-400 hover:text-slate-200 mb-6 transition cursor-pointer">
          <ArrowLeft className="w-4 h-4" />Back
        </button>

        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="glass rounded-2xl overflow-hidden">
          <div className="p-8">
            {/* Status row */}
            <div className="flex items-center gap-3 mb-4">
              <Badge variant={isLive ? (isAuction ? 'live' : 'default') : 'secondary'}>
                {isAuction ? (isLive ? 'LIVE AUCTION' : product.status) : 'FIXED PRICE'}
              </Badge>
              {product.category && (
                <Badge variant="outline">{product.category}</Badge>
              )}
              {isAuction && connected && isLive && (
                <div className="flex items-center gap-1.5 text-xs text-emerald-400">
                  <span className="relative flex h-1.5 w-1.5">
                    <span className="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 live-dot" />
                    <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-emerald-500" />
                  </span>
                  Live updates
                </div>
              )}
            </div>

            {/* Title */}
            <h1 className="text-2xl sm:text-3xl font-bold text-white mb-3">{product.title}</h1>
            {product.description && (
              <p className="text-slate-400 text-sm leading-relaxed mb-6">{product.description}</p>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Left: Price info */}
              <div className="space-y-4">
                {/* Price card */}
                <div className="rounded-xl bg-white/[0.02] border border-white/[0.04] p-5">
                  <p className="text-xs uppercase tracking-wider text-muted-foreground mb-1 flex items-center gap-1.5">
                    {isAuction ? <><TrendingUp className="w-3 h-3 text-emerald-500" />Current Bid</> : <><Package className="w-3 h-3 text-emerald-500" />Price</>}
                  </p>
                  <p className="text-3xl font-bold text-emerald-400 tabular-nums">
                    ${isAuction ? currentBid.toLocaleString('en-US') : (product.buyItNowPrice ?? 0).toLocaleString('en-US')}
                  </p>
                  {isAuction && product.startingPrice && (
                    <p className="text-xs text-slate-500 mt-1">Starting price: ${product.startingPrice.toLocaleString('en-US')}</p>
                  )}
                </div>

                {/* Countdown or stock */}
                {isAuction ? (
                  <div className="rounded-xl bg-white/[0.02] border border-white/[0.04] p-5">
                    <div className="flex items-center gap-1.5 mb-3">
                      <Timer className="w-4 h-4 text-muted-foreground" />
                      <span className="text-xs uppercase tracking-wider text-muted-foreground">Time Remaining</span>
                    </div>
                    <div className="grid grid-cols-4 gap-2">
                      {segments.map((seg, i) => (
                        <div key={i} className="text-center rounded-lg bg-white/[0.02] border border-white/[0.04] py-2">
                          <p className="text-xl font-bold tabular-nums text-slate-200">{seg.value}</p>
                          <p className="text-[10px] text-muted-foreground uppercase">{seg.label}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div className="rounded-xl bg-white/[0.02] border border-white/[0.04] p-5">
                    <div className="flex items-center gap-2">
                      <Package className="w-4 h-4 text-muted-foreground" />
                      <span className="text-sm text-slate-400"><span className="text-white font-semibold">{product.quantity ?? 0}</span> in stock</span>
                    </div>
                  </div>
                )}

                {/* Meta */}
                <div className="rounded-xl bg-white/[0.02] border border-white/[0.04] p-5 space-y-2 text-sm text-slate-400">
                  <div className="flex items-center gap-2"><User className="w-3.5 h-3.5" /><span>Seller: <span className="text-slate-300">{product.sellerId.slice(0, 8)}...</span></span></div>
                  {product.createdAt && (
                    <div className="flex items-center gap-2"><Clock className="w-3.5 h-3.5" /><span>Listed: {new Date(product.createdAt).toLocaleDateString()}</span></div>
                  )}
                </div>
              </div>

              {/* Right: Action panel */}
              <div className="space-y-4">
                <div className="rounded-xl bg-white/[0.02] border border-white/[0.04] p-5">
                  {isAuction ? (
                    <div className="space-y-4">
                      <h3 className="text-sm font-semibold text-white flex items-center gap-2"><Gavel className="w-4 h-4 text-emerald-400" />Place a Bid</h3>
                      <div>
                        <label className="block text-xs text-muted-foreground mb-1.5">Bid Amount ($)</label>
                        <input type="number" value={bidAmount} onChange={(e) => setBidAmount(e.target.value)}
                          min={currentBid + 1} step="1"
                          className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition tabular-nums" />
                        <p className="text-xs text-slate-600 mt-1">Min bid: ${(currentBid + 1).toLocaleString('en-US')}</p>
                      </div>
                      <Button onClick={handleBid} disabled={actionLoading || !isLive || parseFloat(bidAmount) <= currentBid || user?.userId === product.sellerId} className="w-full" size="lg">
                        {user?.userId === product.sellerId ? 'Your Auction' : actionLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : !isLive ? 'Auction Ended' : <><Gavel className="w-4 h-4" />Place Bid</>}
                      </Button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      <h3 className="text-sm font-semibold text-white flex items-center gap-2"><ShoppingCart className="w-4 h-4 text-emerald-400" />Purchase</h3>
                      <div>
                        <label className="block text-xs text-muted-foreground mb-1.5">Quantity</label>
                        <input type="number" value={purchaseQty} onChange={(e) => setPurchaseQty(Math.max(1, parseInt(e.target.value) || 1))}
                          min={1} max={product.quantity ?? 1}
                          className="w-full h-11 px-4 rounded-xl bg-white/[0.03] border border-white/[0.06] text-sm text-slate-200 focus:outline-none focus:ring-2 focus:ring-emerald-500/30 transition tabular-nums" />
                      </div>
                      <p className="text-sm text-slate-300">Total: <span className="text-emerald-400 font-bold">${((product.buyItNowPrice ?? 0) * purchaseQty).toLocaleString('en-US')}</span></p>
                      <Button onClick={handlePurchase} disabled={actionLoading || !isLive || (product.quantity ?? 0) === 0 || user?.userId === product.sellerId} className="w-full" size="lg">
                        {user?.userId === product.sellerId ? 'Your Item' : actionLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : (product.quantity ?? 0) === 0 ? 'Sold Out' : <><ShoppingCart className="w-4 h-4" />Purchase</>}
                      </Button>
                    </div>
                  )}

                  {actionMsg && (
                    <div className={`mt-3 px-3 py-2 rounded-lg text-xs ${actionMsg.includes('success') ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400' : 'bg-red-500/10 border border-red-500/20 text-red-400'}`}>
                      {actionMsg}
                    </div>
                  )}
              </div>

                {/* Delete button — seller only */}
                {user?.userId === product.sellerId && (
                  <div className="rounded-xl bg-red-500/[0.04] border border-red-500/10 p-5">
                    <h3 className="text-sm font-semibold text-red-400 flex items-center gap-2 mb-3">
                      <Trash2 className="w-4 h-4" />Danger Zone
                    </h3>
                    <p className="text-xs text-slate-500 mb-4">Permanently remove this listing. This action cannot be undone.</p>
                    <Button
                      variant="outline"
                      onClick={handleDelete}
                      disabled={deleteLoading}
                      className="w-full border-red-500/20 text-red-400 hover:bg-red-500/10 hover:border-red-500/30"
                    >
                      {deleteLoading ? <Loader2 className="w-4 h-4 animate-spin" /> : <><Trash2 className="w-4 h-4" />Delete Listing</>}
                    </Button>
                  </div>
                )}
              </div>
          </div>
        </motion.div>
      </div>
    </div>
  )
}
