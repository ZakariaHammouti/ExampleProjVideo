import {useState} from "react"
import RestaurantList from "./components/RestaurantList.tsx"
import NearbySearch from "./components/NearbySearch.tsx"
import "./App.css"

type Tab = "own" | "nearby"

export default function App() {
    const [tab, setTab] = useState<Tab>("own")

    return (
        <div className="app">
            <header className="app-header">
                <h1>🍽️ Restaurant Finder</h1>
                <p>Finde gute Restaurants in deiner Nähe</p>
            </header>

            <nav className="tabs">
                <button className={tab === "own" ? "tab active" : "tab"} onClick={() => setTab("own")}>
                    Meine Restaurants
                </button>
                <button className={tab === "nearby" ? "tab active" : "tab"} onClick={() => setTab("nearby")}>
                    In der Nähe (OpenStreetMap)
                </button>
            </nav>

            <main>
                {tab === "own" ? <RestaurantList/> : <NearbySearch/>}
            </main>
        </div>
    )
}
