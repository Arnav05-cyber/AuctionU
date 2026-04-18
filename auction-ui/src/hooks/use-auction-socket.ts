"use client"

import { useEffect, useRef, useCallback, useState } from "react"
import { Client } from "@stomp/stompjs"
import SockJS from "sockjs-client"

const GATEWAY_URL = process.env.NEXT_PUBLIC_GATEWAY_URL ?? "http://localhost:8080"

/**
 * STOMP over SockJS hook — matches the backend's WebSocketConfig:
 *   endpoint: /ws-auction (SockJS)
 *   broker prefix: /topic
 *
 * Returns a map of productId → latest bid amount, so individual
 * AuctionCards can subscribe granularly.
 */
export function useAuctionSocket(productIds?: number[]) {
  const clientRef = useRef<Client | null>(null)
  const [bidUpdates, setBidUpdates] = useState<Map<number, number>>(new Map())
  const [connected, setConnected] = useState(false)

  const connect = useCallback(() => {
    if (clientRef.current?.active) return

    const client = new Client({
      // SockJS factory — connects to /ws-auction via the Gateway
      webSocketFactory: () => new SockJS(`${GATEWAY_URL}/ws-auction`) as unknown as WebSocket,
      reconnectDelay: 3000,
      debug: () => {}, // Silent in prod

      onConnect: () => {
        setConnected(true)

        // If specific productIds are given, subscribe to each
        if (productIds && productIds.length > 0) {
          productIds.forEach((id) => {
            client.subscribe(`/topic/product/${id}`, (message) => {
              handleMessage(id, message.body)
            })
          })
        }
      },

      onDisconnect: () => {
        setConnected(false)
      },

      onStompError: (frame) => {
        console.error("[STOMP Error]", frame.headers["message"])
      },
    })

    client.activate()
    clientRef.current = client
  }, [productIds])

  const handleMessage = (productId: number, body: string) => {
    // Backend RedisSubscriber sends payload like "New bid: 150.00"
    const match = body.match(/([\d.]+)\s*$/)
    if (match) {
      const amount = parseFloat(match[1])
      if (!isNaN(amount)) {
        setBidUpdates((prev) => {
          const next = new Map(prev)
          next.set(productId, amount)
          return next
        })
      }
    }
  }

  /**
   * Subscribe to a single product — useful for the detail page.
   */
  const subscribeTo = useCallback((productId: number) => {
    const client = clientRef.current
    if (!client?.active) return

    client.subscribe(`/topic/product/${productId}`, (message) => {
      handleMessage(productId, message.body)
    })
  }, [])

  useEffect(() => {
    connect()
    return () => {
      clientRef.current?.deactivate()
    }
  }, [connect])

  return { bidUpdates, connected, subscribeTo }
}
