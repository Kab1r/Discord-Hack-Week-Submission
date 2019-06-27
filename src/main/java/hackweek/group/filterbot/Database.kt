package hackweek.group.filterbot

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.WriteResult

/**
 * Firestore based Database
 *
 * @constructor Creates a Firestore Database given credentials and a projectID
 * */
class Database(gcpAuth: GoogleCredentials, gcpProjectId: String) {
    companion object {
        const val DEFAULT_COMMAND_PREFIX = "f!"
    }

    // Firestore Database
    private val db: Firestore = FirestoreOptions.getDefaultInstance()
        .toBuilder()
        .setProjectId(gcpProjectId)
        .setCredentials(gcpAuth)
        .build()
        .service

    /**
     * @return command prefix of a Guild
     *  @param guildID ID of a Guild
     * */
    fun getCommandPrefix(guildID: String): String {
        val docRef: DocumentReference = db.collection("guilds").document(guildID)
        val data: MutableMap<String, Any> = docRef.get().get().data ?: return DEFAULT_COMMAND_PREFIX
        return data["commandPrefix"] as String? ?: DEFAULT_COMMAND_PREFIX
        // default command prefix if firestore returns null
    }

    /**
     * Adds or Sets a prefix to the database.
     * @param guildID ID of a Guild
     * @param prefix Command Prefix
     */
    fun setCommandPrefix(guildID: String, prefix: String) {
        val docRef: DocumentReference = db.collection("guilds").document(guildID)
        val data: MutableMap<String, Any> = docRef.get().get().data!!
        data["commandPrefix"] = prefix
        val result: ApiFuture<WriteResult> = docRef.set(data)
        result.get()
    }

    /**
     *  @return a list of filters for a Guild
     *  @param guildID ID of a Guild
     */
    @Suppress("UNCHECKED_CAST")
    fun getFilters(guildID: String): List<String> {
        val docRef: DocumentReference = db.collection("guilds").document(guildID)
        val data: MutableMap<String, Any> = docRef.get().get().data ?: mutableMapOf()
        return (data["filters"] ?: listOf<String>()) as List<String>
    }

    /**
     *  Sets filters for a Guild in database
     *  @param guildID ID of a Guild
     *  @param filters List of filters
     */
    fun setFilters(guildID: String, filters: MutableList<String>) {
        val docRef: DocumentReference = db.collection("guilds").document(guildID)
        val data: MutableMap<String, Any> = docRef.get().get().data ?: mutableMapOf()
        data["filters"] = filters
        val result: ApiFuture<WriteResult> = docRef.set(data)
        result.get()
    }

    /**
     *  Adds Filters for a Guild to database
     *  @param guildID ID of a Guild
     *  @param filters List of filters
     */
    @Suppress("UNCHECKED_CAST")
    fun addFilters(guildID: String, filters: MutableList<String>) {
        val docRef: DocumentReference = db.collection("guilds").document(guildID)
        val data: MutableMap<String, Any> =
            docRef.get().get().data ?: mutableMapOf<String, MutableList<String>>() as MutableMap<String, Any>
        if (data["filters"] == null) data["filters"] = mutableListOf<String>()
        (data["filters"] as MutableList<String>).addAll(filters)
        val result: ApiFuture<WriteResult> = docRef.set(data)
        result.get()
    }
}