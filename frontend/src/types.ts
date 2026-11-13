export type Restaurant = {
    id: string
    name: string
    cuisine: string
    address: string
    city: string
    rating: number
    priceLevel: number
    latitude: number
    longitude: number
    openNow: boolean
}

/** Ein Restaurant aus OpenStreetMap (GET /api/places/nearby) */
export type Place = {
    id: string
    name: string
    cuisine: string | null
    address: string | null
    city: string | null
    latitude: number
    longitude: number
    distanceKm: number
    openingHours: string | null
    website: string | null
    source: "OSM"
}

export type NearbyResponse = {
    latitude: number
    longitude: number
    locationName: string
    provider: "Overpass" | "Nominatim"
    radiusKm: number
    places: Place[]
}
