package hackweek.group.filterbot


class FilterMatches : ArrayList<Pair<String, Double>>() {
    fun sort() = this.sortBy { it.second }
    fun sorted() = this.sortedBy { it.second }
}



