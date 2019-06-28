package hackweek.group.filterbot

import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.FirestoreOptions
import com.google.cloud.firestore.WriteResult
import com.google.common.hash.Hashing.sha256
import java.nio.charset.Charset

/**
 * Firestore based Database to store specific data
 *
 * @constructor Creates a Firestore Database given credentials and a projectID
 * @param gcpAuth Google Cloud Credentials for message scanning
 * @param gcpProjectID Project ID for Google Cloud Project
 * */
class Database(gcpAuth: GoogleCredentials, gcpProjectID: String) {
    companion object {
        const val DEFAULT_COMMAND_PREFIX = "f!"
    }

    // Firestore Database
    private val db: Firestore = FirestoreOptions.getDefaultInstance()
        .toBuilder()
        .setProjectId(gcpProjectID)
        .setCredentials(gcpAuth)
        .build()
        .service

    /**
     * @return command prefix of a Guild
     *  @param guildID ID of a Guild
     * */
    fun getCommandPrefix(guildID: String): String {
        val guildIDHash = guildID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
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
        val guildIDHash = guildID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
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
        val guildIDHash = guildID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
        val data: MutableMap<String, Any> = docRef.get().get().data ?: mutableMapOf()
        return (data["filters"] ?: listOf<String>()) as List<String>
    }

    /**
     *  Sets filters for a Guild in database
     *  @param guildID ID of a Guild
     *  @param filters List of filters
     */
    fun setFilters(guildID: String, filters: MutableList<String>) {
        val guildIDHash = guildID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
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
        val guildIDHash = guildID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
        val data: MutableMap<String, Any> =
            docRef.get().get().data ?: mutableMapOf<String, MutableList<String>>() as MutableMap<String, Any>
        if (data["filters"] == null) data["filters"] = mutableListOf<String>()
        (data["filters"] as MutableList<String>).addAll(filters)
        val result: ApiFuture<WriteResult> = docRef.set(data)
        result.get()
    }

    /**
     * Adds role IDs that are considered authorized to database
     * @param guildID ID of a Guild
     * @param roleIds List of Authorized roleIDs
     */
    @Suppress("UNCHECKED_CAST")
    fun addAuthorizedRoleIDs(guildID: String, roleIds: List<String>) {
        val guildIDHash = guildID.hashed()
        val roleIDsHashed = mutableListOf<String>()
        roleIds.forEach { roleIDsHashed.add(it.hashed()) }
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
        val data: MutableMap<String, Any> =
            docRef.get().get().data ?: mutableMapOf<String, MutableList<String>>() as MutableMap<String, Any>
        if (data["authorizedRoleIDs"] == null) data["authorizedRoleIDs"] = mutableListOf<String>()
        (data["authorizedRoleIDs"] as MutableList<String>).addAll(roleIDsHashed)
        val result: ApiFuture<WriteResult> = docRef.set(data)
        result.get()
    }

    /**
     * De-authorizes role IDs and removes from database
     * @param guildID ID of a Guild
     * @param roleIds List of Unauthorized role IDs
     * @return if any role ID was removed
     */
    @Suppress("UNCHECKED_CAST")
    fun removeAuthorizedRoleIDs(guildID: String, roleIds: List<String>): Boolean {
        val guildIDHash = guildID.hashed()
        val roleIDsHashed = mutableListOf<String>()
        roleIds.forEach { roleIDsHashed.add(it.hashed()) }
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
        val data: MutableMap<String, Any> =
            docRef.get().get().data ?: mutableMapOf<String, MutableList<String>>() as MutableMap<String, Any>
        if (data["authorizedRoleIDs"] == null) data["authorizedRoleIDs"] = mutableListOf<String>()
        val authorizedRoleIDsHashed = data["authorizedRoleIDs"] as List<String>
        val newAuthorizedRoleIDsHashed = authorizedRoleIDsHashed.filter { !roleIDsHashed.contains(it) }
        return if (authorizedRoleIDsHashed == newAuthorizedRoleIDsHashed) false
        else {
            data["authorizedRoleIDs"] = newAuthorizedRoleIDsHashed
            val result: ApiFuture<WriteResult> = docRef.set(data)
            result.get()
            true
        }
    }

    /**
     * Checks if a role ID is authorized
     * @param guildID ID of a Guild
     * @param roleID role ID to check
     * @return if role ID is authorized
     */
    @Suppress("UNCHECKED_CAST")
    fun isAuthorizedRoleID(guildID: String, roleID: String): Boolean {
        val guildIDHash = guildID.hashed()
        val roleIDHashed = roleID.hashed()
        val docRef: DocumentReference = db.collection("guilds").document(guildIDHash)
        val data: MutableMap<String, Any> =
            docRef.get().get().data ?: mutableMapOf<String, MutableList<String>>() as MutableMap<String, Any>
        if (data["authorizedRoleIDs"] == null) data["authorizedRoleIDs"] = mutableListOf<String>()
        return (data["authorizedRoleIDs"] as MutableList<String>).contains(roleIDHashed)
    }

    /**
     * Hashes current string
     * @return Hashed String
     */
    private fun String.hashed() = sha256().hashString(this, Charset.defaultCharset()).toString()
}