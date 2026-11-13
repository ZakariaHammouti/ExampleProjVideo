import {useEffect, useState} from "react"
import SearchBar from "./SearchBar.tsx"
import RestaurantCard from "./RestaurantCard.tsx"
import {fetchCuisines, fetchRestaurants} from "../api.ts"
import type {Restaurant} from "../types.ts"

/** Eigene Restaurants aus MongoDB */
export default function RestaurantList() {
    const [restaurants, setRestaurants] = useState<Restaurant[]>([])
    const [cuisines, setCuisines] = useState<string[]>([])
    const [search, setSearch] = useState("")
    const [cuisine, setCuisine] = useState("")
    const [onlyOpen, setOnlyOpen] = useState(false)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetchCuisines().then(setCuisines).catch(() => setCuisines([]))
    }, [])

    useEffect(() => {
        fetchRestaurants(search, cuisine)
            .then(data => {
                setRestaurants(data)
                setError(null)
            })
            .catch(() => setError("Restaurants konnten nicht geladen werden. Läuft das Backend?"))
            .finally(() => setLoading(false))
    }, [search, cuisine])

    const visible = onlyOpen ? restaurants.filter(r => r.openNow) : restaurants

    return (
        <>
            <SearchBar
                search={search}
                onSearchChange={setSearch}
                cuisines={cuisines}
                cuisine={cuisine}
                onCuisineChange={setCuisine}
                onlyOpen={onlyOpen}
                onOnlyOpenChange={setOnlyOpen}
            />

            {error && <p className="message error">{error}</p>}
            {!error && loading && <p className="message">Lade Restaurants…</p>}
            {!error && !loading && visible.length === 0 && (
                <p className="message">Keine Restaurants gefunden.</p>
            )}

            <section className="grid">
                {visible.map(r => <RestaurantCard key={r.id} restaurant={r}/>)}
            </section>
        </>
    )
}
