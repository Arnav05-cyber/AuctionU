export type SaleType = "AUCTION" | "FIXED_PRICE"
export type ProductStatus = "ACTIVE" | "INACTIVE" | "PENDING" | "SOLD" | "DELETED"

export interface Product {
  id: number
  title: string
  description: string | null
  category: string | null
  startingPrice: number | null
  currentHighestBid: number | null
  auctionEndTime: string | null   // ISO 8601 LocalDateTime from backend
  createdAt: string | null
  sellerId: string
  status: ProductStatus
  highestBidderId: string | null
  isExpired: boolean
  quantity: number | null
  buyItNowPrice: number | null
  saleType: SaleType
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string // backend sends "token" on login, "refreshToken" on signup
}

export interface UserIdentity {
  userId: string
  username: string
  valid: boolean
}

export interface SignupRequest {
  userName: string
  email: string
  password: string
  firstName: string
  lastName: string
  phoneNumber: string
}

export interface LoginRequest {
  userName: string
  password: string
}
