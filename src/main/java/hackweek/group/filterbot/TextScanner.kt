package hackweek.group.filterbot

import net.dv8tion.jda.core.entities.Message

/**
 * TextScanner is able to handle and scan messages
 * @param database database of filters
 */
class TextScanner(private val database: Database): Scanner<Message> {

    /**
     * Handles message by scanning message
     * and deleting if message if scan got a hit
     * @param input Message to handle
     */
    override fun handle(input: Message) {
        val scan = scan(input)
        if (scan.first) {
            val result = scan.second!!.sorted()
            input.delete().queue()
            var msg = "Image sent by ${input.author.name} was filtered due to filter(s):"
            result.forEach { msg += " ${it.first}," }
            input.channel.sendMessage(msg.substring(0, msg.length - 1)).queue()
        }
    }

    /**
     *  Scans the message and
     * @return if message contains filter from database
     * @param input Message to Scan
     */
    override fun scan(input: Message): Pair<Boolean, FilterMatches?> {
        val matches = FilterMatches()
        var text = input.contentStripped
        if (!text.isNullOrEmpty()) {
            text = text.toLowerCase()
            this.database.getFilters(input.guild.id).forEach {
                if (text.contains(it.toLowerCase()))
                    matches.add(
                        Pair(it, 1.0)
                    )
            }
        }
        return Pair(matches.isNotEmpty(), if (matches.isNotEmpty()) matches else null)

    }
}
