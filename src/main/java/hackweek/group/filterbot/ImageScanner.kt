package hackweek.group.filterbot

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.*
import com.google.protobuf.ByteString
import net.dv8tion.jda.core.entities.Message
import java.io.File

/**
 * ImageScanner is able to handle and scan messages.
 * @param database Database of filters
 */
class ImageScanner(
    private val database: Database,
    gcpAuth: GoogleCredentials
) : Scanner<Message> {
    private val visionClient = ImageAnnotatorClient.create(
        ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider(
                FixedCredentialsProvider
                    .create(gcpAuth)
            ).build()!!
    )

    /**
     * Handles message by scanning message,
     * deleting and sending message
     * if scan got a hit on message
     * @param input Message to Handle
     */
    override fun handle(input: Message) {
        val scan = scan(input)
        if (scan.first) {
            val result = scan.second!!.sorted()
            input.delete().queue()
            var msg = "Image sent by ${input.author.name} was filtered due to filter(s):"
            result.forEach { msg += " ${it.first} (${"%.2f".format(it.second * 100)}%)," }
            input.channel.sendMessage(msg.substring(0, msg.length - 1)).queue()
        }
    }


    /**
     * Scans message for images and image urls
     *  @param input Message to Scan
     *  @return Pair with hasMatch and FilterMatches if hasMatch
     */
    override fun scan(input: Message): Pair<Boolean, FilterMatches?> {
        val filters = database.getFilters(input.guild.id)
        val matches = FilterMatches()
        input.attachments.forEach { attachment ->
            if (attachment.isImage) {
                val img = downloadImage(attachment)!!
                val response = requestImage(img)
                val responses = response.responsesList!!
                responses.forEach { res ->
                    res.labelAnnotationsList.forEach { annotation ->
                        filters.forEach { filter ->
                            if (filter.equals(annotation.description, ignoreCase = true))
                                matches.add(
                                    Pair(
                                        annotation.description,
                                        annotation.score.toDouble()
                                    )
                                )
                        }
                    }
                }
            }
            // TODO add scan for images without attachments (just urls)
        }

        return Pair(matches.isNotEmpty(), if (matches.isNotEmpty()) matches else null)
    }

    /**
     * Applies label detection with Google Cloud Vision
     * @param img Image to do label detection on
     * @return Image annotations response
     */
    private fun requestImage(img: Image): BatchAnnotateImagesResponse {
        val feat = Feature.newBuilder()
            .setType(Feature.Type.LABEL_DETECTION)
            .build()
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feat)
            .setImage(img)
            .build()

        return visionClient.batchAnnotateImages(listOf(request))!!
    }

    /**
     * Downloads an image
     * @param attachment image attachment to download
     * @return downloaded image or null
     */
    private fun downloadImage(attachment: Message.Attachment): Image? {
        val download = File(attachment.fileName)
        val imgBuilder = Image.newBuilder()
        if (attachment.download(download)) {
            imgBuilder.content = ByteString.copyFrom(download.readBytes())
            download.delete()
        } else println(attachment)

        return imgBuilder.build()
    }
}