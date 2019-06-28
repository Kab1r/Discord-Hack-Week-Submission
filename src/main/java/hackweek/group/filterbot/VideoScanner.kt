package hackweek.group.filterbot

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest
import com.google.cloud.videointelligence.v1.Feature
import com.google.cloud.videointelligence.v1.LabelAnnotation
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient
import com.google.cloud.vision.v1.EntityAnnotation
import net.dv8tion.jda.core.entities.EmbedType
import net.dv8tion.jda.core.entities.Message


class VideoScanner(
    private val database: Database,
    gcpAuth: GoogleCredentials
) : Scanner<Message> {
    private val videoIntelligenceServiceClient = VideoIntelligenceServiceClient.create()

    override fun handle(input: Message) {
        val scan = scan(input)
        if (scan.first) {
            val result = scan.second!!.sorted()
            input.delete().queue()
            var msg = "Video sent by ${input.author.name} was filtered by"
            result.forEach { msg += " ${it.first} (${"%.2f".format(it.second * 100)}%)," }
            input.channel.sendMessage(msg.substring(0, msg.length - 1)).queue()
        }
    }


    override fun scan(input: Message): Pair<Boolean, FilterMatches?> {
        val matches = FilterMatches()
        input.embeds.forEach {
            if (it.type == EmbedType.VIDEO) {
                val url = it.url
                val request = AnnotateVideoRequest.newBuilder()
                    .setInputUri(url)
                    .addFeatures(Feature.LABEL_DETECTION).build()
                val response = videoIntelligenceServiceClient.annotateVideoAsync(request).get()
                val results = response.annotationResultsList

                results.forEach { res ->
                    res.segmentLabelAnnotationsList.forEach { annotation: LabelAnnotation ->

                        matches.add(
                            Pair(
                                annotation.entity.description,
                                annotation.segmentsList.first().confidence.toDouble()
                            )
                        )

                    }
                }
            }
        }
        return Pair(matches.isNotEmpty(), if (matches.isNotEmpty()) matches else null)
    }

}