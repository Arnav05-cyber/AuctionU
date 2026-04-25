const GATEWAY_URL = process.env.NEXT_PUBLIC_GATEWAY_URL ?? "http://localhost:8080"

/**
 * Base fetch wrapper — no auth required.
 */
export async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const url = `${GATEWAY_URL}${path}`
  const res = await fetch(url, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      "ngrok-skip-browser-warning": "true",
      ...options.headers,
    },
  })

  if (!res.ok) {
    const text = await res.text().catch(() => "Unknown error")
    throw new ApiError(res.status, text)
  }

  // Handle 204 No Content
  const contentType = res.headers.get("content-type")
  if (!contentType || res.status === 204) {
    return undefined as T
  }

  if (contentType.includes("text/plain")) {
    return res.text() as unknown as T
  }

  return res.json()
}

/**
 * Authenticated fetch — injects Bearer token from localStorage.
 */
export async function authFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getAccessToken()
  if (!token) {
    throw new ApiError(401, "Not authenticated")
  }

  return apiFetch<T>(path, {
    ...options,
    headers: {
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  })
}

/**
 * Token management — persists to localStorage for simplicity.
 * In production, httpOnly cookies via a BFF would be more secure.
 */
export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("auctionu_access_token")
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null
  return localStorage.getItem("auctionu_refresh_token")
}

export function setTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem("auctionu_access_token", accessToken)
  localStorage.setItem("auctionu_refresh_token", refreshToken)
}

export function clearTokens() {
  localStorage.removeItem("auctionu_access_token")
  localStorage.removeItem("auctionu_refresh_token")
}

export class ApiError extends Error {
  status: number
  constructor(status: number, message: string) {
    super(message)
    this.status = status
    this.name = "ApiError"
  }
}
