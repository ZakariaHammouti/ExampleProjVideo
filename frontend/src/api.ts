import type {NearbyResponse, Restaurant} from "./types.ts"

/** Liest die Fehlermeldung aus der Backend-Antwort ({"message": "..."}) */
async function handle<T>(res: Response): Promise<T> {
    if (!res.ok) {
        let message = `Fehler ${res.status}`
        try {
            const body = await res.json()
            if (body.message) message = body.message
        } catch {
            // Antwort war kein JSON
        }
        throw new Error(message)
    }
    return res.json()
}

export function fetchRestaurants(search: string, cuisine: string): Promise<Restaurant[]> {
    const params = new URLSearchParams()
    if (search) params.set("search", search)
    if (cuisine) params.set("cuisine", cuisine)
    return fetch(`/api/restaurants?${params}`).then(res => handle<Restaurant[]>(res))
}

export function fetchCuisines(): Promise<string[]> {
    return fetch("/api/restaurants/cuisines").then(res => handle<string[]>(res))
}

export type NearbyQuery =
    | { city: string; radiusKm: number }
    | { lat: number; lng: number; radiusKm: number }

export function fetchNearby(query: NearbyQuery): Promise<NearbyResponse> {
    const params = new URLSearchParams({radiusKm: String(query.radiusKm)})
    if ("city" in query) {
        params.set("city", query.city)
    } else {
        params.set("lat", String(query.lat))
        params.set("lng", String(query.lng))
    }
    return fetch(`/api/places/nearby?${params}`).then(res => handle<NearbyResponse>(res))
}
