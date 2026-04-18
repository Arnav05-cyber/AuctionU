"use client"

import { useState, useEffect } from "react"

interface TimeLeft {
  days: number
  hours: number
  minutes: number
  seconds: number
  expired: boolean
}

export function useCountdown(endTime: string): TimeLeft {
  const [timeLeft, setTimeLeft] = useState<TimeLeft>(() => calculate(endTime))

  useEffect(() => {
    const id = setInterval(() => {
      setTimeLeft(calculate(endTime))
    }, 1000)
    return () => clearInterval(id)
  }, [endTime])

  return timeLeft
}

function calculate(endTime: string): TimeLeft {
  const diff = new Date(endTime).getTime() - Date.now()

  if (diff <= 0) {
    return { days: 0, hours: 0, minutes: 0, seconds: 0, expired: true }
  }

  return {
    days: Math.floor(diff / (1000 * 60 * 60 * 24)),
    hours: Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)),
    minutes: Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60)),
    seconds: Math.floor((diff % (1000 * 60)) / 1000),
    expired: false,
  }
}
