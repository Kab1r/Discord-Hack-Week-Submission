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
            var msg = "Image send by ${input.author.name} was filtered by"
            result.forEach { msg += " ${it.first} (${"%.2".format(it.second * 100)}%)," }
            input.channel.sendMessage(msg.substring(0, msg.length - 1))
        }
    }


    /**
     * Scans message for images and image urls
     *  @param input Message to Scan
     *  @return Pair with hasMatch and FilterMatches if hasMatch
     */
    override fun scan(input: Message): Pair<Boolean, FilterMatches?> {
        val matches = FilterMatches()
        input.attachments.forEach {
            if (it.isImage) {
                val img = downloadImage(it)!!
                val response = requestImage(img)
                val responses = response.responsesList!!

                responses.forEach { res ->
                    res.labelAnnotationsList.forEach { annotation: EntityAnnotation ->
                        matches.add(
                            Pair(
                                annotation.description,
                                annotation.score.toDouble()
                            )
                        )
                    }
                }
            }
            // TODO add scan for images without attachments (just urls)
        }

        return Pair(matches.isNotEmpty(), if (matches.isNotEmpty()) matches else null)
    }

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

    private fun downloadImage(it: Message.Attachment): Image? {
        val download = File(it.fileName)
        return if (it.download(download))
            Image.newBuilder()
                .setContent(
                    ByteString.copyFrom(
                        download.readBytes()
                    )
                ).build()!!
        else {
            println(it)
            null
        }
    }
}