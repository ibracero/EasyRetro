package com.ibracero.retrum.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreCollection
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreField
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreTable
import com.ibracero.retrum.data.remote.firestore.RetroRemote
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.data.remote.firestore.UserRemote
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore {

    companion object {
        const val USER_UUID = "W2KCUn3Dz4Wy35CzQZmc"
    }

    private val db = FirebaseFirestore.getInstance()
    private var userRemote: UserRemote =
        UserRemote()

    fun observeUser(onUpdate: (UserRemote) -> Unit) {
        db.collection(FirestoreTable.TABLE_USERS)
            .document(USER_UUID)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(retroUuids = snapshot?.documents?.map { it.id } ?: emptyList())
                onUpdate(userRemote)
            }

        db.collection(FirestoreTable.TABLE_USERS)
            .document(USER_UUID)
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(
                    email = snapshot?.getString(FirestoreField.USER_EMAIL).orEmpty(),
                    firstName = snapshot?.getString(FirestoreField.USER_FIRST_NAME).orEmpty(),
                    lastName = snapshot?.getString(FirestoreField.USER_LAST_NAME).orEmpty()
                )
                onUpdate(userRemote)
            }
    }

    fun observeStatements(retroUuid: String, onUpdate: (List<StatementRemote>) -> Unit) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .addSnapshotListener { snapshot, _ ->
                val statements = snapshot?.documents?.map { doc ->
                    StatementRemote(
                        uuid = doc.id,
                        retroUuid = retroUuid,
                        userEmail = doc.getString(FirestoreField.USER_EMAIL).orEmpty(),
                        statementType = doc.getString(FirestoreField.STATEMENT_TYPE).orEmpty(),
                        description = doc.getString(FirestoreField.STATEMENT_DESCRIPTION).orEmpty()
                    )
                }
                onUpdate(statements?.toList() ?: emptyList())
            }
    }


    fun observeUserRetros(onUpdate: (List<RetroRemote>) -> Unit) {
        db.collection(FirestoreTable.TABLE_USERS)
            .document(USER_UUID)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .addSnapshotListener { snapshot, _ ->
                val retros = snapshot?.documents?.map { doc ->
                    RetroRemote(
                        doc.id,
                        doc.getString(FirestoreField.RETRO_TITLE).orEmpty()
                    )
                }
                onUpdate(retros?.toList() ?: emptyList())
            }
    }

    fun addStatementToBoard(retroUuid: String, statementRemote: StatementRemote) {
        val item = hashMapOf(
            FirestoreField.USER_EMAIL to statementRemote.userEmail,
            FirestoreField.STATEMENT_TYPE to statementRemote.statementType,
            FirestoreField.STATEMENT_DESCRIPTION to statementRemote.description
        )

        addItemToBoard(retroUuid, item)
    }

    suspend fun createRetro(retroTitle: String): RetroRemote {
        val retroUuid = suspendCoroutine<String> { continuation ->
            db.collection(FirestoreTable.TABLE_RETROS)
                .document()
                .get()
                .addOnSuccessListener {
                    continuation.resume(it.id)
                }
        }

        return suspendCoroutine { continuation ->

            val item = hashMapOf(FirestoreField.RETRO_TITLE to retroTitle)

            db.collection(FirestoreTable.TABLE_USERS)
                .document(USER_UUID)
                .collection(FirestoreCollection.COLLECTION_RETROS)
                .document(retroUuid)
                .set(item)
                .addOnSuccessListener {
                    continuation.resume(
                        RetroRemote(
                            uuid = retroUuid,
                            title = retroTitle
                        )
                    )
                }
        }
    }

    private fun addItemToBoard(retroUuid: String, item: HashMap<String, String>) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .add(item)
            .addOnSuccessListener {
                Timber.d("Statement added $item.")
            }
    }
}