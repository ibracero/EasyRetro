package com.ibracero.retrum.data.remote.cloudstore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_ACTION_POINTS
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_WENT_BADLY
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.BOARD_WENT_WELL
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.FIELD_ITEM_DESCRIPTION
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.FIELD_USER_ID
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.TABLE_RETROS
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.DatabaseInfo.TABLE_USERS
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import timber.log.Timber
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class FirebaseDataStore {

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
    private var currentRetroId: String = "EZBGkeRIaYnftxblLXFu"


    suspend fun getLatestOrCreateRetro() {
        suspendCoroutine { continuation: Continuation<Unit> ->
            db.collection(TABLE_RETROS)
                .document(currentRetroId)
                .get()
                .addOnSuccessListener {
                    currentRetroId = it.id
                    Timber.d("Retro added with ID: $currentRetroId")
                    continuation.resume(Unit)
                }
                .addOnFailureListener {
                    Timber.e(it, "Retro could not be added due to an exception")
                    continuation.resumeWithException(it)
                }
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
                .document(currentRetroId)
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