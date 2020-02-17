package com.ibracero.retrum.data.remote

import arrow.core.Either
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreCollection
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreField
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreTable
import com.ibracero.retrum.data.remote.firestore.RetroRemote
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.data.remote.firestore.UserRemote
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore {

    private val db = FirebaseFirestore.getInstance()

    private var statementObserver: ListenerRegistration? = null
    private var retrosObserver: ListenerRegistration? = null

    suspend fun createRetro(userEmail: String, retroTitle: String): Either<ServerError, RetroRemote> {
        val retroUuid = suspendCoroutine<String?> { continuation ->
            val retro = hashMapOf(
                FirestoreField.RETRO_TITLE to retroTitle,
                FirestoreField.RETRO_CREATED to FieldValue.serverTimestamp()
            )

            db.collection(FirestoreTable.TABLE_RETROS)
                .add(retro)
                .addOnSuccessListener {
                    continuation.resume(it.id)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }

        return retroUuid?.let { uuid ->
            suspendCoroutine<Either<ServerError, RetroRemote>> { continuation ->
                db.collection(FirestoreTable.TABLE_USERS)
                    .document(userEmail)
                    .update(FirestoreField.USER_FIELD, FieldValue.arrayUnion(uuid))
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

    suspend fun observeUserRetros(userEmail: String): Either<ServerError, List<RetroRemote>> {
        val retroUuids = suspendCoroutine<List<String>?> { continuation ->
            db.collection(FirestoreTable.TABLE_USERS)
                .document(userEmail)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null) continuation.resume(doc.get(FirestoreField.USER_FIELD) as List<String>)
                    else continuation.resume(null)
                }
                .addOnFailureListener { continuation.resume(null) }
        }

        return retroUuids?.let {
            suspendCoroutine<Either<ServerError, List<RetroRemote>>> { continuation ->
                retrosObserver?.remove()
                retrosObserver = db.collection(FirestoreTable.TABLE_RETROS)
                    .addSnapshotListener { snapshot, _ ->
                        val retros = snapshot?.documents
                            ?.filter { doc -> it.contains(doc.id) }
                            ?.map { doc ->
                                RetroRemote(
                                    uuid = doc.id,
                                    title = doc.getString(FirestoreField.RETRO_TITLE).orEmpty(),
                                    timestamp = (doc.getTimestamp(FirestoreField.RETRO_CREATED)?.seconds ?: 0) * 1000
                                )
                            }

                        if (!retros.isNullOrEmpty() && !snapshot.metadata.hasPendingWrites()) {
                            Timber.d("Retros update $retros")
                            continuation.resume(Either.right(retros.toList()))
                        } else continuation.resume(Either.left(ServerError.GetRetrosError))
                    }
            }
        } ?: Either.left(ServerError.GetRetrosError)
    }

    fun observeStatements(userEmail: String, retroUuid: String, onUpdate: (List<StatementRemote>) -> Unit) {
        joinRetro(userEmail, retroUuid)

        statementObserver?.remove()
        statementObserver = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .addSnapshotListener { snapshot, _ ->
                val statements = snapshot?.documents?.map { doc ->
                    val author = doc.getString(FirestoreField.STATEMENT_AUTHOR).orEmpty()
                    StatementRemote(
                        uuid = doc.id,
                        retroUuid = retroUuid,
                        userEmail = author,
                        statementType = doc.getString(FirestoreField.STATEMENT_TYPE).orEmpty(),
                        description = doc.getString(FirestoreField.STATEMENT_DESCRIPTION).orEmpty(),
                        timestamp = (doc.getTimestamp(FirestoreField.STATEMENT_CREATED)?.seconds ?: 0) * 1000,
                        isRemovable = author == userEmail
                    )
                }

                if (!statements.isNullOrEmpty() && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Statements update $statements")
                    onUpdate(statements.toList())
                }
            }
    }

    private fun joinRetro(userEmail: String, retroUuid: String) {
        db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
            .update(FirestoreField.USER_FIELD, FieldValue.arrayUnion(retroUuid))
            .addOnSuccessListener {
                Timber.d("User $userEmail joined $retroUuid")
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
            FirestoreField.STATEMENT_AUTHOR to statementRemote.userEmail,
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

    fun bindUser(email: String, firstName: String, lastName: String, photoUrl: String) {
        val data = hashMapOf(
            FirestoreField.USER_FIRST_NAME to firstName,
            FirestoreField.USER_LAST_NAME to lastName,
            FirestoreField.USER_PHOTO_URL to photoUrl
        )

        db.collection(FirestoreTable.TABLE_USERS)
            .document(email)
            .set(data, SetOptions.merge())
    }
}