package hackweek.group.filterbot

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder


private val GCP_PROJECT_ID: String = System.getenv("GCP_PROJECT_ID")
private val DISCORD_TOKEN: String = System.getenv("DISCORD_TOKEN")
private val GOOGLE_CREDENTIALS: GoogleCredentials = ServiceAccountCredentials.getApplicationDefault()

/**
 * Main function for Filter Discord Bot
 * @see GCP_PROJECT_ID Google Cloud Project ID Environment variable must be set
 * @see DISCORD_TOKEN Discord Bot Token Environment variable must be set
 * @see GOOGLE_CREDENTIALS GOOGLE_APPLICATION_CREDENTIALS Environment variable with path to service account json file
 */
fun main() {
    val jdaBot: JDA = JDABuilder(DISCORD_TOKEN).build()

    jdaBot.addEventListener(
        MessageListener(gcpAuth = GOOGLE_CREDENTIALS, gcpProjectID = GCP_PROJECT_ID)
    )
}