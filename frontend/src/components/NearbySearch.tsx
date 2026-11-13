import {type FormEvent, useState} from "react"
import PlaceCard from "./PlaceCard.tsx"
import {fetchNearby, type NearbyQuery} from "../api.ts"
import type {NearbyResponse} from "../types.ts"

const RADIUS_OPTIONS = [0.5, 1, 2, 5]

/** Echte Restaurants aus OpenStreetMap – per Standort oder per Stadt */
export default function NearbySearch() {
    const [city, setCity] = useState("")
    const [radiusKm, setRadiusKm] = useState(1)
    const [result, setResult] = useState<NearbyResponse | null>(null)
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)

    function search(query: NearbyQuery) {
        setLoading(true)
        setError(null)
        fetchNearby(query)
            .then(setResult)
            .catch((e: Error) => {
                setResult(null)
                setError(e.message)
            })
            .finally(() => setLoading(false))
    }

    function useMyLocation() {
        if (!navigator.geolocation) {
            setError("Dein Browser unterstützt keine Standortabfrage. Gib bitte eine Stadt ein.")
            return
        }
        setLoading(true)
        setError(null)
        navigator.geolocation.getCurrentPosition(
            pos => search({lat: pos.coords.latitude, lng: pos.coords.longitude, radiusKm}),
            err => {
                setLoading(false)
                setError(err.code === err.PERMISSION_DENIED
                    ? "Standortzugriff wurde abgelehnt. Gib bitte eine Stadt ein."
                    : "Standort konnte nicht ermittelt werden. Gib bitte eine Stadt ein.")
            },
            {timeout: 10000}
        )
    }

    function onSubmit(e: FormEvent) {
        e.preventDefault()
        if (!city.trim()) {
            setError("Bitte eine Stadt eingeben.")
            return
        }
        search({city: city.trim(), radiusKm})
    }

    return (
        <>
            <form className="search-bar" onSubmit={onSubmit}>
                <button type="button" className="secondary" onClick={useMyLocation} disabled={loading}>
                    📍 Mein Standort
                </button>
                <span className="or">oder</span>
                <input
                    type="search"
                    placeholder="Stadt oder Adresse, z. B. Köln"
                    value={city}
                    onChange={e => setCity(e.target.value)}
                />
                <select value={radiusKm} onChange={e => setRadiusKm(Number(e.target.value))}>
                    {RADIUS_OPTIONS.map(r => <option key={r} value={r}>{String(r).replace(".", ",")} km</option>)}
                </select>
                <button type="submit" disabled={loading}>Suchen</button>
            </form>

            {loading && <p className="message">Suche bei OpenStreetMap… (kann einige Sekunden dauern)</p>}
            {error && <p className="message error">{error}</p>}

            {result && !loading && (
                <>
                    <p className="message">
                        {result.places.length} Restaurants im Umkreis von {String(result.radiusKm).replace(".", ",")} km
                        um <strong>{result.locationName}</strong>
                    </p>
                    {result.provider === "Nominatim" && (
                        <p className="message hint">
                            Hinweis: Der Overpass-Server war überlastet, daher wurde die Ausweichsuche genutzt
                            (höchstens 40 Treffer).
                        </p>
                    )}
                    <section className="grid">
                        {result.places.map(p => <PlaceCard key={p.id} place={p}/>)}
                    </section>
                </>
            )}

            <p className="attribution">
                Daten © <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noreferrer">
                OpenStreetMap-Mitwirkende</a>
            </p>
        </>
    )
}
