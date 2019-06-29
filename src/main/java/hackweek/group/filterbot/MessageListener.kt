package hackweek.group.filterbot

import com.google.auth.oauth2.GoogleCredentials
import net.dv8tion.jda.core.entities.EmbedType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

/**
 * MessageListener handles messages received
 * @param gcpAuth Google Cloud Credentials for message scanning
 * @param gcpProjectID Project ID for Google Cloud Project
 * @property database Database of guilds with filters
 */
class MessageListener(gcpAuth: GoogleCredentials, gcpProjectID: String) : ListenerAdapter() {
    val database = Database(gcpAuth, gcpProjectID)
    private val commandManager = CommandManager(database)
    private val textScanner = TextScanner(database)
    private val imageScanner = ImageScanner(database)

    /**
     * Handles with message events
     * @param event MessageReceivedEvent to handle
     */
    override fun onMessageReceived(event: MessageReceivedEvent?) {
        super.onMessageReceived(event)

        if (event == null) return // null events
        if (event.author.id == event.jda.selfUser.id) return // Ignore own messages

        if (event.guild == null) { // non guild events
            event.channel.sendMessage("This bot does not support direct messages or groups").queue()
            return
        }

        if (event.message.isCommand(event.guild.id)) {
            commandManager.handle(event)
        } else {

            if (event.message.hasImage()) {
                imageScanner.handle(event.message)
            }

            if (event.message.hasVideo()) {

            }

            if (event.message.hasText()) {
                textScanner.handle(event.message)
            }
        }

    }

    /**
     * Private Extension function to check if a message is a command
     * @param guildID to check command prefix
     * @return if this message is a command
     */
    private fun Message.isCommand(guildID: String): Boolean =
        this.contentStripped.toLowerCase()
            .startsWith(database.getCommandPrefix(guildID))
}

/**
 * Extension function
 * @return if message has an image
 */
fun Message.hasImage(): Boolean {
    this.attachments.forEach {
        if (it.isImage) return true
    }

    if (this.contentStripped.isNullOrEmpty()) return false
    val text = this.contentStripped.toLowerCase()
    return text.contains(".png") ||
            text.contains(".jpg") ||
            text.contains(".jpeg") ||
            text.contains(".tiff") ||
            text.contains(".bmp")

}

/**
 * Extension Function
 * @return if message has a video
 */
fun Message.hasVideo(): Boolean {
    if (this.embeds.isNullOrEmpty())
        return false
    else {
        this.embeds.forEach {
            if (it.type == EmbedType.VIDEO)
                return true
        }
        return false
    }
}

/**
 * Extension Function
 * @return if message has text
 */
fun Message.hasText(): Boolean =
    this.contentStripped.isNotEmpty()