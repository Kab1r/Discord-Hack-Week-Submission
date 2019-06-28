package hackweek.group.filterbot


class FilterMatches : ArrayList<Pair<String, Double>>() {
    fun sort() = this.sortByDescending { it.second }
    fun sorted() = this.sortedByDescending { it.second }
}



