package com.ibracero.retrum.data.remote.cloudstore

import com.google.firebase.firestore.FirebaseFirestore
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.TABLE_RETROS
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.TABLE_USERS
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseDataStore {

    companion object {
        const val RETRO_UUID = "EZBGkeRIaYnftxblLXFu"
        const val USER_UUID = "W2KCUn3Dz4Wy35CzQZmc"
    }

    private object DatabaseInfo {
        const val TABLE_USERS = "users"
        const val TABLE_RETROS = "retros"
    }

    private val db = FirebaseFirestore.getInstance()
    private var userRemote: UserRemote = UserRemote()

    fun observeUser(onUpdate: (UserRemote) -> Unit) {
        db.collection(TABLE_USERS)
            .document(USER_UUID)
            .collection("retros")
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(retroUuids = snapshot?.documents?.map { it.id } ?: emptyList())
                onUpdate(userRemote)
            }

        db.collection(TABLE_USERS)
            .document(USER_UUID)
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(
                    email = snapshot?.getString("email").orEmpty(),
                    firstName = snapshot?.getString("first_name").orEmpty(),
                    lastName = snapshot?.getString("last_name").orEmpty()
                )
                onUpdate(userRemote)
            }
    }

    fun observeStatements(retroUuid: String, onUpdate: (List<StatementRemote>) -> Unit) {
        db.collection(TABLE_RETROS)
            .document(retroUuid)
            .collection("statements")
            .addSnapshotListener { snapshot, _ ->
                val statements = snapshot?.documents?.map { doc ->
                    StatementRemote(
                        uuid = doc.id,
                        retroUuid = retroUuid,
                        userEmail = doc.getString("user_email").orEmpty(),
                        statementType = doc.getString("type").orEmpty(),
                        description = doc.getString("description").orEmpty()
                    )
                }
                onUpdate(statements?.toList() ?: emptyList())
            }
    }


    fun observeUserRetros(onUpdate: (List<RetroRemote>) -> Unit) {
        db.collection(TABLE_USERS)
            .document(USER_UUID)
            .collection("retros")
            .addSnapshotListener { snapshot, _ ->
                val retros = snapshot?.documents?.map { doc ->
                    RetroRemote(doc.id, doc.getString("title").orEmpty())
                }
                onUpdate(retros?.toList() ?: emptyList())
            }
    }

    fun addStatementToBoard(retroUuid: String, statementRemote: StatementRemote) {
        val item = hashMapOf(
            "user_email" to statementRemote.userEmail,
            "type" to statementRemote.statementType,
            "description" to statementRemote.description
        )

        addItemToBoard(retroUuid, item)
    }

    suspend fun createRetro(retroTitle: String): RetroRemote {
        val retroUuid = suspendCoroutine<String> { continuation ->
            db.collection(TABLE_RETROS)
                .document()
                .get()
                .addOnSuccessListener {
                    continuation.resume(it.id)
                }
        }

        return suspendCoroutine { continuation ->

            val item = hashMapOf("title" to retroTitle)

            db.collection(TABLE_USERS)
                .document(USER_UUID)
                .collection("retros")
                .document(retroUuid)
                .set(item)
                .addOnSuccessListener {
                    continuation.resume(RetroRemote(uuid = retroUuid, title = retroTitle))
                }
        }
    }

    private fun addItemToBoard(retroUuid: String, item: HashMap<String, String>) {
        db.collection(TABLE_RETROS)
            .document(retroUuid)
            .collection("statements")
            .add(item)
            .addOnSuccessListener {
                Timber.d("Item added $it.")
            }
    }
}