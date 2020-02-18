package com.ibracero.retrum.data.remote

import arrow.core.Either
import arrow.core.extensions.option.semiring.empty
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreCollection
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreField
import com.ibracero.retrum.data.remote.firestore.CloudFireStore.FirestoreTable
import com.ibracero.retrum.data.remote.firestore.RetroRemote
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.data.remote.firestore.UserRemote
import timber.log.Timber
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore {

    private val db = FirebaseFirestore.getInstance()

    private var statementObserver: ListenerRegistration? = null
    private var retrosObserver: ListenerRegistration? = null
    private var usersObserver: ListenerRegistration? = null

    suspend fun createRetro(userEmail: String, retroTitle: String): Either<ServerError, RetroRemote> {
        val userRef = db.collection(FirestoreTable.TABLE_USERS).document(userEmail)

        val retroValues = hashMapOf(
            FirestoreField.RETRO_TITLE to retroTitle,
            FirestoreField.RETRO_CREATED to FieldValue.serverTimestamp(),
            FirestoreField.RETRO_USERS to FieldValue.arrayUnion(userRef)
        )

        val retroUuid = UUID.randomUUID().toString()

        val retroRef = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)

        //Create retro (retros table)
        val retroCreated = suspendCoroutine<Boolean> { continuation ->
            retroRef.set(retroValues)
                .addOnSuccessListener {
                    Timber.d("Retro $retroUuid creaded with values: $retroValues")
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    Timber.e(it, "Couldn't create: $retroUuid")
                    continuation.resume(false)
                }
        }

        if (!retroCreated) return Either.left(ServerError.CreateRetroError)

        //Add created retro to user retro list
        return suspendCoroutine { continuation ->
            userRef
                .update(FirestoreField.USER_RETROS, FieldValue.arrayUnion(retroRef))
                .addOnSuccessListener {
                    Timber.d("Retro $retroUuid added to user: $userEmail ")
                    continuation.resume(Either.right(RetroRemote(uuid = retroUuid, title = retroTitle)))
                }
                .addOnFailureListener {
                    Timber.e(it, "Couldn't add $retroUuid to user: $userEmail")
                    continuation.resume(Either.left(ServerError.CreateRetroError))
                }
        }
    }

    suspend fun observeUserRetros(userEmail: String): Either<ServerError, List<RetroRemote>> {
        val retroDocs = suspendCoroutine<List<DocumentReference>> { continuation ->
            retrosObserver?.remove()
            retrosObserver = db.collection(FirestoreTable.TABLE_USERS)
                .document(userEmail)
                .addSnapshotListener { snapshot, _ ->
                    //                        val docs = snapshot?.get(FirestoreField.USER_RETROS) as List<DocumentReference>?
//                        val retros = retroDocs?.mapNotNull { it.get().result?.toObject(RetroRemote::class.java) }
                    continuation.resume(
                        snapshot?.get(FirestoreField.USER_RETROS) as List<DocumentReference>? ?: emptyList()
                    )
                }
        }

        return suspendCoroutine { cont ->
            Tasks.whenAllComplete(retroDocs.map { it.get() })
                .addOnSuccessListener { tasks ->
                    tasks.filter { it.isSuccessful }.map { (it.result as DocumentSnapshot).data }
                    //TODO HANDLE THIS
                }
        }
    }

    suspend fun observeStatements(userEmail: String, retroUuid: String): Either<ServerError, List<StatementRemote>> {
        joinRetro(userEmail, retroUuid)

        return suspendCoroutine { continuation ->
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
                        continuation.resume(Either.right(statements.toList()))
                    }
                }
        }
    }

    suspend fun observeRetroUsers(retroUuid: String): Either<ServerError, List<UserRemote>> {
        return suspendCoroutine { continuation ->
            usersObserver?.remove()
            usersObserver = db.collection(FirestoreTable.TABLE_RETROS)
                .document(retroUuid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) continuation.resume(Either.left(ServerError.GetRetrosError))
                    else {
                        val userDocs = snapshot.get(FirestoreField.RETRO_USERS) as List<DocumentReference>
                        val users = userDocs.mapNotNull { it.get().result?.toObject(UserRemote::class.java) }
                        continuation.resume(Either.right(users))
                    }
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

    fun stopObservingUserRetros() {
        retrosObserver?.remove()
    }

    fun stopObservingRetroUsers() {
        usersObserver?.remove()
    }

    fun createUser(email: String, firstName: String, lastName: String, photoUrl: String) {
        val data = hashMapOf(
            FirestoreField.USER_FIRST_NAME to firstName,
            FirestoreField.USER_LAST_NAME to lastName,
            FirestoreField.USER_PHOTO_URL to photoUrl
        )

        db.collection(FirestoreTable.TABLE_USERS)
            .document(email)
            .set(data, SetOptions.merge())
    }

    private fun joinRetro(userEmail: String, retroUuid: String) {
        val userRef = db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)

        val retroRef = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)

        retroRef.update(FirestoreField.RETRO_USERS, FieldValue.arrayUnion(userRef))
        userRef.update(FirestoreField.USER_RETROS, FieldValue.arrayUnion(retroRef))
    }
}