package com.easyretro.data.remote

import arrow.core.Either
import arrow.core.left
import com.easyretro.common.ConnectionManager
import com.easyretro.common.NetworkStatus
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreCollection
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreField
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreTable
import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.domain.Failure
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore(private val connectionManager: ConnectionManager) {

    private val db = FirebaseFirestore.getInstance()

    @ExperimentalCoroutinesApi
    suspend fun observeRetroUsers(retroUuid: String) =
        callbackFlow<Either<Failure, List<UserRemote>>> {

            val registrationObserver = db.collection(FirestoreTable.TABLE_RETROS)
                .document(retroUuid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null)
                        offer(Either.left(Failure.parse(exception)))

                    val userDocs =
                        (snapshot?.get(FirestoreField.RETRO_USERS) as List<DocumentReference>?) ?: emptyList()
                    Tasks.whenAllComplete(userDocs.map { it.get() })
                        .addOnSuccessListener { tasks ->
                            val users =
                                tasks.filter { it.isSuccessful }
                                    .mapNotNull {
                                        val documentSnapshot = it.result as DocumentSnapshot
                                        val values = documentSnapshot.data
                                        UserRemote(
                                            email = documentSnapshot.id,
                                            firstName = (values?.get(FirestoreField.USER_FIRST_NAME) as String?).orEmpty(),
                                            lastName = (values?.get(FirestoreField.USER_LAST_NAME) as String?).orEmpty(),
                                            photoUrl = (values?.get(FirestoreField.USER_PHOTO_URL) as String?).orEmpty()
                                        )
                                    }
                            if (!users.isNullOrEmpty() && snapshot?.metadata?.hasPendingWrites() == false) {
                                Timber.d("Users update $users")
                                offer(Either.right(users))
                            }
                        }
                }

            awaitClose {
                Timber.d("Stop observing retro $retroUuid users")
                registrationObserver.remove()
                cancel()
            }
        }

    @ExperimentalCoroutinesApi
    suspend fun observeStatements(
        userEmail: String,
        retroUuid: String
    ) = callbackFlow<Either<Failure, List<StatementRemote>>> {
        val registrationObserver = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .collection(FirestoreCollection.COLLECTION_STATEMENTS)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null)
                    offer(Either.left(Failure.parse(exception)))

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

                if (statements != null && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Statements update $statements")
                    offer(Either.right(statements.toList()))
                }
            }

        awaitClose {
            Timber.d("Stop observing retro $retroUuid statements")
            registrationObserver.remove()
            cancel()
        }
    }

    suspend fun createRetro(userEmail: String, retroTitle: String): Either<Failure, RetroRemote> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

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
        val retroEither = suspendCoroutine<Either<Failure, Unit>> { continuation ->
            retroRef.set(retroValues)
                .addOnSuccessListener {
                    Timber.d("Retro $retroUuid creaded with values: $retroValues")
                    continuation.resume(Either.right(Unit))
                }
                .addOnFailureListener {
                    Timber.e(it, "Couldn't create: $retroUuid")
                    continuation.resume(Either.left(Failure.parse(it)))
                }
        }

        if (retroEither is Either.Left)
            return retroEither

        //Add created retro to user retro list
        return suspendCoroutine { continuation ->
            userRef
                .update(FirestoreField.USER_RETROS, FieldValue.arrayUnion(retroRef))
                .addOnSuccessListener {
                    Timber.d("Retro $retroUuid added to user: $userEmail ")
                    continuation.resume(Either.right(RetroRemote(uuid = retroUuid, title = retroTitle)))
                }
                .addOnFailureListener {
                    Timber.e(it, "Couldn't add retro $retroUuid to user: $userEmail")
                    continuation.resume(Either.left(Failure.CreateRetroError))
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun getUserRetros(userEmail: String): Either<Failure, List<RetroRemote>> {
        return suspendCoroutine { continuation ->
            db.collection(FirestoreTable.TABLE_USERS)
                .document(userEmail)
                .get()
                .addOnSuccessListener { snapshot ->
                    val retroDocs = snapshot?.get(FirestoreField.USER_RETROS) as List<DocumentReference>? ?: emptyList()
                    Tasks.whenAllComplete(retroDocs.map { it.get() })
                        .addOnSuccessListener { tasks ->
                            val retros =
                                tasks.filter { it.isSuccessful }
                                    .mapNotNull {
                                        val documentSnapshot = it.result as DocumentSnapshot
                                        val values = documentSnapshot.data
                                        RetroRemote(
                                            uuid = documentSnapshot.id,
                                            title = (values?.get(FirestoreField.RETRO_TITLE) as String?).orEmpty()
                                        )
                                    }
                            Timber.d("Get retros request SUCCESS")
                            continuation.resume(Either.right(retros))
                        }
                }
                .addOnFailureListener {
                    Timber.e(it, "Get retros request FAILED")
                    continuation.resume(Either.left(Failure.parse(it)))
                }
        }
    }

    suspend fun addStatementToBoard(retroUuid: String, statementRemote: StatementRemote): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        val item = hashMapOf(
            FirestoreField.STATEMENT_AUTHOR to statementRemote.userEmail,
            FirestoreField.STATEMENT_TYPE to statementRemote.statementType,
            FirestoreField.STATEMENT_DESCRIPTION to statementRemote.description,
            FirestoreField.STATEMENT_CREATED to FieldValue.serverTimestamp()
        )

        return suspendCoroutine { continuation ->
            db.collection(FirestoreTable.TABLE_RETROS)
                .document(retroUuid)
                .collection(FirestoreCollection.COLLECTION_STATEMENTS)
                .add(item)
                .addOnSuccessListener {
                    Timber.d("Statement ${statementRemote.description} added to retro $retroUuid")
                    continuation.resume(Either.right(Unit))
                }
                .addOnFailureListener {
                    Timber.d(it, "Statement ${statementRemote.description} could not be added to retro $retroUuid")
                    continuation.resume(Either.left(Failure.CreateStatementError))
                }
        }
    }

    suspend fun removeStatement(retroUuid: String, statementUuid: String): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        return suspendCoroutine { continuation ->
            db.collection(FirestoreTable.TABLE_RETROS)
                .document(retroUuid)
                .collection(FirestoreCollection.COLLECTION_STATEMENTS)
                .document(statementUuid)
                .delete()
                .addOnSuccessListener {
                    Timber.d("Statement $statementUuid removed from retro $retroUuid")
                    continuation.resume(Either.right(Unit))
                }
                .addOnFailureListener {
                    Timber.e(it, "Statement $statementUuid could not be removed from retro $retroUuid")
                    continuation.resume(Either.left(Failure.parse(it)))
                }
        }
    }

    suspend fun joinRetro(userEmail: String, retroUuid: String): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        val userRef = db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)

        val retroRef = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)

        return suspendCoroutine { continuation ->
            Tasks.whenAllComplete(
                retroRef.update(FirestoreField.RETRO_USERS, FieldValue.arrayUnion(userRef)),
                userRef.update(FirestoreField.USER_RETROS, FieldValue.arrayUnion(retroRef))
            ).addOnSuccessListener {
                Timber.d("User $userEmail joined retro $retroUuid")
                continuation.resume(Either.right(Unit))
            }.addOnFailureListener {
                Timber.e(it, "User $userEmail could not join retro $retroUuid")
                continuation.resume(Either.left(Failure.parse(it)))
            }
        }
    }

    suspend fun createUser(
        email: String,
        firstName: String,
        lastName: String,
        photoUrl: String
    ): Either<Failure, Unit> {
        if (connectionManager.getNetworkStatus() == NetworkStatus.OFFLINE)
            return Either.left(Failure.UnavailableNetwork)

        val data = hashMapOf(
            FirestoreField.USER_FIRST_NAME to firstName,
            FirestoreField.USER_LAST_NAME to lastName,
            FirestoreField.USER_PHOTO_URL to photoUrl
        )

        return suspendCoroutine { continuation ->
            db.collection(FirestoreTable.TABLE_USERS)
                .document(email)
                .set(data, SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Either.right(Unit))
                }
                .addOnFailureListener {
                    continuation.resume(Either.left(Failure.parse(it)))
                }
        }
    }


}