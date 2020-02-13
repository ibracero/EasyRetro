package com.ibracero.retrum.data.remote

import arrow.core.Either
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ibracero.retrum.data.local.User
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
    private var userRemote: UserRemote = UserRemote()

    private var statementObserver: ListenerRegistration? = null
    private var retrosObserver: ListenerRegistration? = null
    private var userObserver: ListenerRegistration? = null


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

    fun observeUserRetros(onUpdate: (List<RetroRemote>) -> Unit) {
        retrosObserver?.remove()
        retrosObserver = db.collection(FirestoreTable.TABLE_USERS)
            .document(USER_UUID)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .addSnapshotListener { snapshot, _ ->
                val retros = snapshot?.documents?.map { doc ->
                    RetroRemote(
                        uuid = doc.id,
                        title = doc.getString(FirestoreField.RETRO_TITLE).orEmpty(),
                        timestamp = (doc.getTimestamp(FirestoreField.RETRO_CREATED)?.seconds ?: 0) * 1000
                    )
                }
                if (!retros.isNullOrEmpty() && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Retros update $retros")
                    onUpdate(retros.toList())
                }
            }
    }


    fun observeStatements(retroUuid: String, onUpdate: (List<StatementRemote>) -> Unit) {
        statementObserver?.remove()
        statementObserver = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .addSnapshotListener { snapshot, _ ->
                val statements = snapshot?.documents?.map { doc ->
                    val userEmail = doc.getString(FirestoreField.USER_EMAIL).orEmpty()
                    StatementRemote(
                        uuid = doc.id,
                        retroUuid = retroUuid,
                        userEmail = userEmail,
                        statementType = doc.getString(FirestoreField.STATEMENT_TYPE).orEmpty(),
                        description = doc.getString(FirestoreField.STATEMENT_DESCRIPTION).orEmpty(),
                        timestamp = (doc.getTimestamp(FirestoreField.STATEMENT_CREATED)?.seconds ?: 0) * 1000,
                        isRemovable = userEmail == "yo@yo.com"
                    )
                }

                if (!statements.isNullOrEmpty() && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Statements update $statements")
                    onUpdate(statements.toList())
                }
            }
    }

    fun observeRetroUsers(retroUuid: String, onUpdate: (List<String>) -> Unit) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_USERS)
            .addSnapshotListener { snapshot, _ ->
                val usersEmail = snapshot?.documents?.map { doc ->
                    doc.getString(FirestoreField.USER_EMAIL).orEmpty()
                }

                if (!usersEmail.isNullOrEmpty() && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Users update")
                    onUpdate(usersEmail)
                }
            }
    }

    fun addStatementToBoard(retroUuid: String, statementRemote: StatementRemote) {
        val item = hashMapOf(
            FirestoreField.USER_EMAIL to statementRemote.userEmail,
            FirestoreField.STATEMENT_TYPE to statementRemote.statementType,
            FirestoreField.STATEMENT_DESCRIPTION to statementRemote.description,
            FirestoreField.STATEMENT_CREATED to FieldValue.serverTimestamp()
        )

        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .add(item)
            .addOnSuccessListener {
                Timber.d("Statement added $item")
            }
    }

    suspend fun createRetro(retroTitle: String): Either<ServerError, RetroRemote> {
        val retroUuid = suspendCoroutine<String?> { continuation ->
            db.collection(FirestoreTable.TABLE_RETROS)
                .document()
                .get()
                .addOnSuccessListener {
                    continuation.resume(it.id)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

        return retroUuid?.let { uuid ->
            suspendCoroutine<Either<ServerError, RetroRemote>> { continuation ->
                val item = hashMapOf(
                    FirestoreField.RETRO_TITLE to retroTitle,
                    FirestoreField.RETRO_CREATED to FieldValue.serverTimestamp()
                )

                db.collection(FirestoreTable.TABLE_USERS)
                    .document(USER_UUID)
                    .collection(FirestoreCollection.COLLECTION_RETROS)
                    .document(uuid)
                    .set(item)
                    .addOnSuccessListener {
                        val retro = RetroRemote(uuid = uuid, title = retroTitle)
                        Timber.d("Retro added $retro")
                        continuation.resume(Either.right(retro))
                    }
                    .addOnFailureListener {
                        continuation.resume(Either.left(ServerError.CreateRetroError))
                    }
            }
        } ?: Either.left(ServerError.CreateRetroError)
    }

    fun removeRetro(retroUuid: String) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .delete()

        db.collection(FirestoreTable.TABLE_USERS)
            .document(USER_UUID)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .document(retroUuid)
            .delete()
    }

    fun removeStatement(retroUuid: String, statementUuid: String) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .document(statementUuid)
            .delete()
    }

    fun stopObservingStatements() {
        statementObserver?.remove()
    }

    fun stopObservingRetros() {
        retrosObserver?.remove()
    }
}