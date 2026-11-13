type Props = {
    search: string
    onSearchChange: (value: string) => void
    cuisines: string[]
    cuisine: string
    onCuisineChange: (value: string) => void
    onlyOpen: boolean
    onOnlyOpenChange: (value: boolean) => void
}

export default function SearchBar(props: Props) {
    return (
        <div className="search-bar">
            <input
                type="search"
                placeholder="Restaurant, Küche oder Adresse suchen…"
                value={props.search}
                onChange={e => props.onSearchChange(e.target.value)}
            />
            <select value={props.cuisine} onChange={e => props.onCuisineChange(e.target.value)}>
                <option value="">Alle Küchen</option>
                {props.cuisines.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
            <label className="toggle">
                <input
                    type="checkbox"
                    checked={props.onlyOpen}
                    onChange={e => props.onOnlyOpenChange(e.target.checked)}
                />
                Jetzt geöffnet
            </label>
        </div>
    )
}
