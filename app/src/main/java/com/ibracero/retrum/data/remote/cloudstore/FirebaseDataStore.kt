package com.ibracero.retrum.data.remote.cloudstore

import com.google.firebase.firestore.FirebaseFirestore
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_ACTION_POINTS
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_WENT_BADLY
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_WENT_WELL
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.FIELD_ITEM_DESCRIPTION
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.FIELD_USER_ID
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.TABLE_RETROS
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseDataStore {

    companion object {
        const val RETRO_UUID = "EZBGkeRIaYnftxblLXFu"
    }

    private object DatabaseInfo {
        const val TABLE_USERS = "users"
        const val TABLE_RETROS = "retros"

        const val BOARD_WENT_WELL = "went_well"
        const val BOARD_WENT_BADLY = "went_badly"
        const val BOARD_ACTION_POINTS = "action_points"

        const val FIELD_USER_ID = "user_id"
        const val FIELD_ITEM_DESCRIPTION = "description"
    }

    private val db = FirebaseFirestore.getInstance()

    suspend fun loadRetro(): RetroResponse {
        return suspendCoroutine { continuation ->
            var uuid = RETRO_UUID
            var title: String? = ""
            val positivePoints: MutableList<StatementResponse> = mutableListOf()
            val negativePoints: MutableList<StatementResponse> = mutableListOf()
            val actionPoints: MutableList<StatementResponse> = mutableListOf()

            val retroRef = db.collection(TABLE_RETROS)
                .document(RETRO_UUID)

            retroRef
                .get()
                .addOnSuccessListener {
                    title = it.getString("title").orEmpty()
                }

            retroRef.collection("positive_points")
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        positivePoints.add(
                            StatementResponse(
                                uuid = doc.id,
                                userEmail = doc.getString("user_email").orEmpty(),
                                description = doc.getString("description").orEmpty()
                            )
                        )
                    }
                }

            retroRef.collection("negative_points")
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        negativePoints.add(
                            StatementResponse(
                                uuid = doc.id,
                                userEmail = doc.getString("user_email").orEmpty(),
                                description = doc.getString("description").orEmpty()
                            )
                        )
                    }
                }

            retroRef.collection("action_points")
                .get()
                .addOnSuccessListener {
                    for (doc in it) {
                        actionPoints.add(
                            StatementResponse(
                                uuid = doc.id,
                                userEmail = doc.getString("user_email").orEmpty(),
                                description = doc.getString("description").orEmpty()
                            )
                        )
                    }
                }

            continuation.resume(
                RetroResponse(

                )
            )
        }
    }

    suspend fun addStatementToBoard(statementType: StatementType, description: String) {
        val item = hashMapOf(
            FIELD_USER_ID to "imanol",
            FIELD_ITEM_DESCRIPTION to description
        )

        when (statementType) {
            POSITIVE -> addItemToBoard(BOARD_WENT_WELL, item)
            NEGATIVE -> addItemToBoard(BOARD_WENT_BADLY, item)
            ACTION_POINT -> addItemToBoard(BOARD_ACTION_POINTS, item)
        }
    }

    private fun createUser() {
        // email, first_name, last_name
    }

    private suspend fun addItemToBoard(board: String, item: HashMap<String, String>) {
        suspendCoroutine { continuation: Continuation<Unit> ->
            db.collection(TABLE_RETROS)
                .document(RETRO_UUID)
                .collection(board)
                .add(item).addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }
    }
}