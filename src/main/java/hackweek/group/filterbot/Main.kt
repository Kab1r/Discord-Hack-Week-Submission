package hackweek.group.filterbot

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.JDABuilder
import java.net.URI
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*


private val GCP_PROJECT_ID: String = System.getenv("GCP_PROJECT_ID")
private val DISCORD_TOKEN: String = System.getenv("DISCORD_TOKEN")
private val GOOGLE_CREDENTIALS: GoogleCredentials =
    ServiceAccountCredentials.newBuilder()
        .setProjectId(GCP_PROJECT_ID)
        .setPrivateKeyId(System.getenv("private_key_id"))
        .setPrivateKey(generatePrivateKey(System.getenv("private_key")))
        .setClientEmail(System.getenv("client_email"))
        .setClientId(System.getenv("client_id"))
        .setTokenServerUri(URI(System.getenv("token_uri")))
        .build()
//ServiceAccountCredentials.fromStream(System.getenv("GOOGLE_APPLICATION_CREDENTIALS").byteInputStream())

fun generatePrivateKey(keyContent: String): PrivateKey {
    val newKeyContent = keyContent.replace("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
    val kf = KeyFactory.getInstance("RSA")!!
    val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(newKeyContent))
    return kf.generatePrivate(keySpecPKCS8)!!
}


fun main() {
    val jdaBot: JDA = JDABuilder(DISCORD_TOKEN).build()

    jdaBot.addEventListener(
        MessageListener(gcpAuth = GOOGLE_CREDENTIALS, gcpProjectID = GCP_PROJECT_ID)
    )
}