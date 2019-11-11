package com.ibracero.retrum.data.remote.cloudstore

import com.google.firebase.firestore.FirebaseFirestore
import com.ibracero.retrum.data.local.TABLE_USER
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

    suspend fun loadRetro(): RetroRemote {

        val title = suspendCoroutine<String> { continuation ->
            db.collection(TABLE_RETROS)
                .document(RETRO_UUID)
                .get()
                .addOnSuccessListener {
                    continuation.resume(it.getString("title").orEmpty())
                }
        }

        return RetroRemote(
            uuid = RETRO_UUID,
            title = title
        )
    }

    /*suspend fun loadStatements(): List<StatementRemote> {

        val retroRef = db.collection(TABLE_RETROS)
            .document(RETRO_UUID)

        return suspendCoroutine { continuation ->
            retroRef.collection("statements")
                .get()
                .addOnSuccessListener {
                    val positives = mutableListOf<StatementRemote>()
                    for (doc in it) {
                        positives.add(
                            StatementRemote(
                                uuid = doc.id,
                                userEmail = doc.getString("user_email").orEmpty(),
                                description = doc.getString("description").orEmpty(),
                                retroUuid = RETRO_UUID,
                                statementType = doc.getString("type").orEmpty()
                            )
                        )
                    }
                    continuation.resume(positives.toList())
                }
        }
    }*/

    fun observeStatements(onUpdate: (StatementRemote) -> Unit) {
        db.collection(TABLE_RETROS)
            .document(RETRO_UUID)
            .collection("statements")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.forEach { doc ->
                    val statement = StatementRemote(
                        uuid = doc.id,
                        retroUuid = RETRO_UUID,
                        userEmail = doc.getString("user_email").orEmpty(),
                        statementType = doc.getString("type").orEmpty(),
                        description = doc.getString("description").orEmpty()
                    )
                    Timber.d("Update statement: $statement")
                    onUpdate(statement)
                }
            }
    }


    fun observeUserRetros(onUpdate: (RetroRemote) -> Unit) {
        db.collection(TABLE_USERS)
            .document(USER_UUID)
            .collection("retros")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.forEach { doc ->
                    val retro = RetroRemote(doc.id, doc.getString("title").orEmpty())
                    Timber.d("Update retro: $retro")
                    onUpdate(retro)
                }
            }
    }

    fun addStatementToBoard(statementRemote: StatementRemote) {
        val item = hashMapOf(
            "user_email" to statementRemote.userEmail,
            "type" to statementRemote.statementType,
            "description" to statementRemote.description
        )

        addItemToBoard(item)
    }

    private fun addItemToBoard(item: HashMap<String, String>) {
        db.collection(TABLE_RETROS)
            .document(RETRO_UUID)
            .collection("statements")
            .add(item)
    }
}