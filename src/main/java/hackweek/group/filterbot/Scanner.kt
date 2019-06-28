package hackweek.group.filterbot

interface Scanner<T> {
    fun handle(input: T)
    fun scan(input: T): Pair<Boolean, FilterMatches?>
}