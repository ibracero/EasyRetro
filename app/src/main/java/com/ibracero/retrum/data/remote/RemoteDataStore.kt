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
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore {

    private val db = FirebaseFirestore.getInstance()
    private var userRemote: UserRemote = UserRemote()

    private var statementObserver: ListenerRegistration? = null
    private var retrosObserver: ListenerRegistration? = null
    private var userObserver: ListenerRegistration? = null


    fun observeUser(userEmail: String, onUpdate: (UserRemote) -> Unit) {
        db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(retroUuids = snapshot?.documents?.map { it.id } ?: emptyList())
                onUpdate(userRemote)
            }

        db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
            .addSnapshotListener { snapshot, _ ->
                userRemote = userRemote.copy(
                    email = userEmail,
                    firstName = snapshot?.getString(FirestoreField.USER_FIRST_NAME).orEmpty(),
                    lastName = snapshot?.getString(FirestoreField.USER_LAST_NAME).orEmpty(),
                    photoUrl = snapshot?.getString(FirestoreField.USER_PHOTO_URL).orEmpty()
                )
                onUpdate(userRemote)
            }
    }

    fun observeUserRetros(userEmail: String, onUpdate: (List<RetroRemote>) -> Unit) {
        retrosObserver?.remove()
        retrosObserver = db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
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


    fun observeStatements(userEmail: String, retroUuid: String, onUpdate: (List<StatementRemote>) -> Unit) {

        db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
            .collection(FirestoreCollection.COLLECTION_RETROS)
            .document(retroUuid)

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

    suspend fun createRetro(userEmail: String, retroTitle: String): Either<ServerError, RetroRemote> {
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
                    .document(userEmail)
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

    fun removeRetro(userEmail: String, retroUuid: String) {
        db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .delete()

        db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
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

    fun bindUser(email: String, firstName: String, lastName: String, photoUrl: String) {
        val data = hashMapOf(
            FirestoreField.USER_FIRST_NAME to firstName,
            FirestoreField.USER_LAST_NAME to lastName,
            FirestoreField.USER_PHOTO_URL to photoUrl
        )

        //try to find user on Firebase. If it doesn't exist, create one
        db.collection(FirestoreTable.TABLE_USERS)
            .document(email)
            .set(data, SetOptions.merge())
    }
}