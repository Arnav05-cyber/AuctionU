"use client"

import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from "react"
import { apiFetch, authFetch, setTokens, clearTokens, getAccessToken, getRefreshToken, ApiError } from "@/lib/api"
import type { UserIdentity, LoginRequest, SignupRequest, AuthTokens } from "@/types/auction"

interface AuthState {
  user: UserIdentity | null
  loading: boolean
  login: (data: LoginRequest) => Promise<void>
  signup: (data: SignupRequest) => Promise<void>
  verifySignup: (email: string, otp: string) => Promise<void>
  logout: () => Promise<void>
  deleteAccount: () => Promise<void>
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthState | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserIdentity | null>(null)
  const [loading, setLoading] = useState(true)

  // Check session on mount
  const checkSession = useCallback(async () => {
    const token = getAccessToken()
    if (!token) {
      setLoading(false)
      return
    }

    try {
      const identity = await authFetch<UserIdentity>("/auth/v1/ping")
      setUser(identity)
    } catch {
      // Token expired — try to refresh
      try {
        await refreshAccessToken()
        const identity = await authFetch<UserIdentity>("/auth/v1/ping")
        setUser(identity)
      } catch {
        clearTokens()
        setUser(null)
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    checkSession()
  }, [checkSession])

  const login = async (data: LoginRequest) => {
    // Backend TokenController uses "userName" in the actual getter
    const res = await apiFetch<{ accessToken: string; token?: string; refreshToken?: string }>(
      "/auth/v1/login",
      {
        method: "POST",
        body: JSON.stringify(data),
      }
    )

    // Login returns { accessToken, token } while signup returns { accessToken, refreshToken }
    const refresh = res.refreshToken ?? res.token ?? ""
    setTokens(res.accessToken, refresh)

    const identity = await authFetch<UserIdentity>("/auth/v1/ping")
    setUser(identity)
  }

  const signup = async (data: SignupRequest) => {
    await apiFetch<string>(
      "/auth/v1/signup",
      {
        method: "POST",
        body: JSON.stringify(data),
      }
    )
  }

  const verifySignup = async (email: string, otp: string) => {
    await apiFetch<string>("/auth/v1/verify-signup", {
      method: "POST",
      body: JSON.stringify({ email, otp }),
    })
  }

  const logout = async () => {
    try {
      await authFetch("/auth/v1/logout", { method: "POST" })
    } catch {
      // Ignore — we're clearing local state either way
    }
    clearTokens()
    setUser(null)
  }

  const deleteAccount = async () => {
    try {
      // Delete user profile first
      await authFetch("/api/v1/users/me", { method: "DELETE" }).catch(() => {})
      // Delete auth account and tokens
      await authFetch("/auth/v1/delete-account", { method: "DELETE" })
    } finally {
      clearTokens()
      setUser(null)
    }
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, signup, verifySignup, logout, deleteAccount, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error("useAuth must be used within AuthProvider")
  return ctx
}

/**
 * Silent refresh — exchanges the stored refresh token for a new access token.
 */
async function refreshAccessToken() {
  const rt = getRefreshToken()
  if (!rt) throw new Error("No refresh token")

  const res = await apiFetch<{ accessToken: string; token?: string; refreshToken?: string }>(
    "/auth/v1/refreshToken",
    {
      method: "POST",
      body: JSON.stringify({ refreshToken: rt }),
    }
  )

  const newRefresh = res.refreshToken ?? res.token ?? rt
  setTokens(res.accessToken, newRefresh)
}
