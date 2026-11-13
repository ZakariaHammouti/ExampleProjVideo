import type {Place} from "../types.ts"

type Props = {
    place: Place
}

function formatDistance(km: number): string {
    return km < 1 ? `${Math.round(km * 1000)} m` : `${km.toFixed(1).replace(".", ",")} km`
}

export default function PlaceCard({place}: Props) {
    const mapUrl = `https://www.openstreetmap.org/?mlat=${place.latitude}&mlon=${place.longitude}#map=18/${place.latitude}/${place.longitude}`
    const address = [place.address, place.city].filter(Boolean).join(", ")

    return (
        <article className="card">
            <div className="card-header">
                <h3>{place.name}</h3>
                <span className="distance">{formatDistance(place.distanceKm)}</span>
            </div>
            <p className="cuisine">{place.cuisine ?? "Küche unbekannt"}</p>
            {address && <p className="address">{address}</p>}
            {place.openingHours && <p className="hours">🕒 {place.openingHours}</p>}
            <div className="card-links">
                <a href={mapUrl} target="_blank" rel="noreferrer">Auf Karte ansehen</a>
                {place.website && <a href={place.website} target="_blank" rel="noreferrer">Website</a>}
            </div>
        </article>
    )
}
