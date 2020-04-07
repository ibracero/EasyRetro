package com.easyretro.data.remote

import arrow.core.Either
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreCollection
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreField
import com.easyretro.data.remote.firestore.CloudFireStore.FirestoreTable
import com.easyretro.data.remote.firestore.RetroRemote
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.data.remote.firestore.UserRemote
import com.easyretro.domain.Failure
import timber.log.Timber
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RemoteDataStore {

    private val db = FirebaseFirestore.getInstance()

    private var statementObserver: ListenerRegistration? = null
    private var retrosObserver: ListenerRegistration? = null
    private var usersObserver: ListenerRegistration? = null

    suspend fun createRetro(userEmail: String, retroTitle: String): Either<Failure, RetroRemote> {
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

        if (!retroCreated) return Either.left(Failure.CreateRetroError)

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
                    continuation.resume(Either.left(Failure.CreateRetroError))
                }
        }
    }

    suspend fun getUserRetros(userEmail: String): Either<Failure, List<RetroRemote>>{
        return suspendCoroutine { continuation ->
            db.collection(FirestoreTable.TABLE_USERS)
                .document(userEmail)
                .get()
                .addOnSuccessListener {snapshot ->
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
                            continuation.resume(Either.right(retros))
                        }
                }
        }
    }

    fun observeUserRetros(userEmail: String, onUpdate: (Either<Failure, List<RetroRemote>>) -> Unit) {
        retrosObserver?.remove()
        retrosObserver = db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)
            .addSnapshotListener { snapshot, _ ->
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
                        Timber.d("Retros update $retros")
                        onUpdate(Either.right(retros))
                    }
            }
    }

    fun observeRetroUsers(retroUuid: String, onUpdate: (Either<Failure, List<UserRemote>>) -> Unit) {
        usersObserver?.remove()
        usersObserver = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)
            .addSnapshotListener { snapshot, _ ->
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
                            onUpdate(Either.right(users))
                        }
                    }
            }
    }

    fun observeStatements(
        userEmail: String,
        retroUuid: String,
        onUpdate: (Either<Failure, List<StatementRemote>>) -> Unit
    ) {
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

                if (statements != null && !snapshot.metadata.hasPendingWrites()) {
                    Timber.d("Statements update $statements")
                    onUpdate(Either.right(statements.toList()))
                }
            }
    }

    suspend fun addStatementToBoard(retroUuid: String, statementRemote: StatementRemote): Either<Failure, Unit> {
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
                    continuation.resume(Either.right(Unit))
                }
                .addOnFailureListener {
                    continuation.resume(Either.left(Failure.CreateStatementError))
                }
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

    fun stopObservingAll() {
        stopObservingRetroUsers()
        stopObservingStatements()
        stopObservingRetroUsers()
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

    fun joinRetro(userEmail: String, retroUuid: String) {
        val userRef = db.collection(FirestoreTable.TABLE_USERS)
            .document(userEmail)

        val retroRef = db.collection(FirestoreTable.TABLE_RETROS)
            .document(retroUuid)

        retroRef.update(FirestoreField.RETRO_USERS, FieldValue.arrayUnion(userRef))
        userRef.update(FirestoreField.USER_RETROS, FieldValue.arrayUnion(retroRef))
    }
}