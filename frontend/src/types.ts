export type RoleName = 'ADMIN' | 'CLIENT' | 'APPRAISER'

export interface UserView {
  userAccountId: number
  username: string
  email: string
  role: RoleName
}

export interface TokenPair {
  accessToken: string
  refreshToken: string
}

export interface AuthResponse {
  tokens: TokenPair
  user: UserView
}

export interface DistrictResponse {
  id: number
  districtName: string
  avgPrice: number | null
  demandLevel: number | null
}

export interface EstateResponse {
  id: number
  address: string
  districtId: number
  districtName: string | null
  rooms: number
  area: number
  condition: string
  propertyType: string
  price: number
  floor: number
  totalFloors: number
  description: string | null
  imagePath: string | null
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ReviewResponse {
  id: number
  estateId: number
  userAccountId: number
  username: string
  rating: number
  comment: string | null
  createdAt: string
}

export interface RecommendationResponse {
  recommendationId: number | null
  estateId: number
  address: string
  districtId: number
  districtName: string
  rooms: number
  area: number
  floor: number
  totalFloors: number
  condition: string
  propertyType: string
  price: number
  imagePath: string | null
  score: number
  scorePercent: number
  createdAt: string
}

export interface UserPreferenceResponse {
  preferenceId: number
  minPrice: number | null
  maxPrice: number | null
  minArea: number | null
  maxArea: number | null
  minRooms: number | null
  maxRooms: number | null
  minFloor: number | null
  maxFloor: number | null
  condition: string | null
}

export interface EstateValuationData {
  currentDistrictAvgPrice: number
  baseValue: number
  roomMultiplier: number
  demandMultiplier: number
  conditionMultiplier: number
  floorMultiplier: number
  estimatedValue: number
}

export interface EstateComparisonData {
  estate1: {
    id: number
    address: string
    price: number
    area: number
    rooms: number
    pricePerSquare: number
  }
  estate2: {
    id: number
    address: string
    price: number
    area: number
    rooms: number
    pricePerSquare: number
  }
  differences: {
    priceDiff: number
    areaDiff: number
    roomsDiff: number
    pricePerSquareDiff: number
  }
}

export interface EstatePredictionData {
  currentValue: number
  predictedValue: number
  growthRate: number
  months: number
}

export interface UserSummaryResponse {
  id: number
  username: string
  email: string
  role: RoleName
}

export interface AdminStatsResponse {
  usersCount: number
  estatesCount: number
  evaluationsCount: number
}

