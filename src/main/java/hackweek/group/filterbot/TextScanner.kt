package hackweek.group.filterbot

import net.dv8tion.jda.core.entities.Message

/**
 * TextScanner is able to handle and scan messages
 * @param database database of filters
 */
class TextScanner(private val database: Database) {

    /**
     * Handles message by scanning message
     * and deleting if message if scan got a hit
     * @param message Message to handle
     */
    fun handle(message: Message) {
        if (scan(message).first)
            message.delete().queue()
    }

    /**
     *  Scans the message and
     * @return if message contains filter from database
     * @param message Message to Scan
     */
    fun scan(message: Message): Pair<Boolean, String?> {
        var text = message.contentStripped
        if (!text.isNullOrEmpty()) {
            text = text.toLowerCase()
            this.database.getFilters(message.guild.id).forEach {
                if (text.contains(it.toLowerCase())) return Pair(true, it)
            }
        }
        return Pair(false, null)

    }
}