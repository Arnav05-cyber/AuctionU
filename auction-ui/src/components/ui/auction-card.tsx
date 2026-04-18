'use client'

import { memo, useState, useEffect, useRef } from 'react'
import { motion } from 'framer-motion'
import Link from 'next/link'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { useCountdown } from '@/hooks/use-countdown'
import { authFetch } from '@/lib/api'
import { Timer, Gavel, TrendingUp, Zap, ShoppingCart, Package } from 'lucide-react'
import type { Product } from '@/types/auction'

interface AuctionCardProps {
  product: Product
  liveBid?: number
}

export const AuctionCard = memo(function AuctionCard({ product, liveBid }: AuctionCardProps) {
  const { title, currentHighestBid, auctionEndTime, status, saleType, buyItNowPrice, quantity } = product
  const isAuction = saleType === 'AUCTION'
  const displayBid = liveBid ?? (currentHighestBid ?? 0)
  const countdown = useCountdown(auctionEndTime ?? new Date().toISOString())
  const [bidding, setBidding] = useState(false)
  const [flash, setFlash] = useState(false)
  const prevBidRef = useRef(displayBid)

  useEffect(() => {
    if (displayBid !== prevBidRef.current) {
      setFlash(true)
      prevBidRef.current = displayBid
      const t = setTimeout(() => setFlash(false), 600)
      return () => clearTimeout(t)
    }
  }, [displayBid])

  const handleQuickBid = async (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setBidding(true)
    try {
      await authFetch(`/api/v1/products/${product.id}/bid`, {
        method: 'POST',
        body: JSON.stringify({ amount: displayBid + 10 }),
      })
    } catch {
      // Would show toast in production
    }
    setBidding(false)
  }

  const handleQuickPurchase = async (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setBidding(true)
    try {
      await authFetch(`/api/v1/products/${product.id}/purchase`, {
        method: 'POST',
        body: JSON.stringify({ quantity: 1 }),
      })
    } catch {
      // Would show toast in production
    }
    setBidding(false)
  }

  const isLive = status === 'ACTIVE' && (isAuction ? !countdown.expired : true)
  const urgency = isAuction && !countdown.expired && countdown.hours === 0 && countdown.minutes < 10

  const segments = countdown.expired
    ? [{ label: 'ENDED', value: '--' }]
    : [
        ...(countdown.days > 0 ? [{ label: 'D', value: String(countdown.days).padStart(2, '0') }] : []),
        { label: 'H', value: String(countdown.hours).padStart(2, '0') },
        { label: 'M', value: String(countdown.minutes).padStart(2, '0') },
        { label: 'S', value: String(countdown.seconds).padStart(2, '0') },
      ]

  return (
    <Link href={`/product/${product.id}`}>
      <motion.div
        layout
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        whileHover={{ y: -6, transition: { duration: 0.25 } }}
        className="group relative rounded-2xl overflow-hidden glass hover:border-emerald-500/30 transition-colors duration-300 cursor-pointer"
      >
        {/* Status bar */}
        <div className="flex items-center justify-between px-5 pt-4 pb-2">
          <div className="flex items-center gap-2">
            {isLive && isAuction && (
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75 live-dot" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
              </span>
            )}
            <Badge variant={isLive ? (isAuction ? 'live' : 'default') : 'secondary'}>
              {isAuction ? (isLive ? 'LIVE AUCTION' : status) : 'BUY NOW'}
            </Badge>
          </div>
          {urgency && isLive && (
            <Badge variant="destructive" className="text-[10px]">
              <Zap className="w-3 h-3 mr-1" />ENDING SOON
            </Badge>
          )}
        </div>

        {/* Title */}
        <div className="px-5 pb-3">
          <h3 className="text-base font-semibold text-foreground group-hover:text-emerald-400 transition-colors duration-200 line-clamp-2 leading-snug">
            {title}
          </h3>
          {product.category && (
            <span className="text-xs text-muted-foreground mt-1 inline-block">{product.category}</span>
          )}
        </div>

        {/* Price section */}
        <div className={`mx-5 px-4 py-3 rounded-xl bg-white/[0.02] border border-white/[0.04] ${flash ? 'bid-flash' : ''}`}>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-[11px] uppercase tracking-wider text-muted-foreground mb-0.5 flex items-center gap-1.5">
                {isAuction ? (
                  <><TrendingUp className="w-3 h-3 text-emerald-500" />Current Bid</>
                ) : (
                  <><Package className="w-3 h-3 text-emerald-500" />Price</>
                )}
              </p>
              <p className="text-2xl font-bold text-emerald-400 tabular-nums">
                ${isAuction ? displayBid.toLocaleString('en-US') : (buyItNowPrice ?? 0).toLocaleString('en-US')}
              </p>
            </div>
            <div className="w-10 h-10 rounded-xl bg-emerald-500/10 flex items-center justify-center">
              {isAuction ? <Gavel className="w-5 h-5 text-emerald-500" /> : <ShoppingCart className="w-5 h-5 text-emerald-500" />}
            </div>
          </div>
        </div>

        {/* Countdown or stock */}
        <div className="px-5 pt-3 pb-2">
          {isAuction ? (
            <>
              <div className="flex items-center gap-1.5 mb-2">
                <Timer className="w-3.5 h-3.5 text-muted-foreground" />
                <span className="text-[11px] uppercase tracking-wider text-muted-foreground">Time Remaining</span>
              </div>
              <div className="flex gap-1.5">
                {segments.map((seg, i) => (
                  <div key={i} className="flex items-baseline gap-0.5">
                    <span className={`text-lg font-bold tabular-nums ${urgency && isLive ? 'text-red-400' : 'text-slate-200'}`}>{seg.value}</span>
                    <span className="text-[10px] text-muted-foreground font-medium">{seg.label}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="flex items-center gap-1.5">
              <Package className="w-3.5 h-3.5 text-muted-foreground" />
              <span className="text-sm text-slate-400">{quantity ?? 0} in stock</span>
            </div>
          )}
        </div>

        {/* Action button */}
        <div className="px-5 pt-2 pb-5">
          {isAuction ? (
            <Button onClick={handleQuickBid} disabled={bidding || !isLive} className="w-full" size="lg">
              {bidding ? 'Placing...' : !isLive ? 'Auction Ended' : (
                <><Zap className="w-4 h-4" />Quick Bid +$10</>
              )}
            </Button>
          ) : (
            <Button onClick={handleQuickPurchase} disabled={bidding || !isLive || (quantity ?? 0) === 0} className="w-full" size="lg">
              {bidding ? 'Purchasing...' : (quantity ?? 0) === 0 ? 'Sold Out' : (
                <><ShoppingCart className="w-4 h-4" />Buy Now</>
              )}
            </Button>
          )}
        </div>

        <div className="absolute inset-x-0 bottom-0 h-px bg-gradient-to-r from-transparent via-emerald-500/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
      </motion.div>
    </Link>
  )
})
