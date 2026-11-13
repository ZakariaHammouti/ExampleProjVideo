import type {Restaurant} from "../types.ts"

type Props = {
    restaurant: Restaurant
}

export default function RestaurantCard({restaurant}: Props) {
    return (
        <article className="card">
            <div className="card-header">
                <h3>{restaurant.name}</h3>
                <span className="rating">★ {restaurant.rating.toFixed(1)}</span>
            </div>
            <p className="cuisine">
                {restaurant.cuisine} · {"€".repeat(restaurant.priceLevel)}
            </p>
            <p className="address">{restaurant.address}, {restaurant.city}</p>
            <span className={restaurant.openNow ? "badge open" : "badge closed"}>
                {restaurant.openNow ? "Geöffnet" : "Geschlossen"}
            </span>
        </article>
    )
}
