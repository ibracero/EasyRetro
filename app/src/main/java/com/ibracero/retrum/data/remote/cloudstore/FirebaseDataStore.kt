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
    var currentRetroId: String? = null


    fun createRetro(callback: () -> Unit) {
        db.collection(TABLE_RETROS)
            .document()
            .get()
            .addOnSuccessListener {
                currentRetroId = it.id
                Log.d("FILTER", "new Retro added $currentRetroId")
                callback()
            }
    }

    private fun createUser() {
        // email, first_name, last_name
        db.collection(TABLE_USERS)
    }

    private fun addWentWellPoint(userId: String, description: String) {
        val item = hashMapOf(
            FIELD_USER_ID to userId,
            FIELD_ITEM_DESCRIPTION to description
        )

        addItemToBoard(BOARD_WENT_WELL, item)
    }

    private fun addWentBadlyPoint(userId: String, description: String) {
        val item = hashMapOf(
            FIELD_USER_ID to userId,
            FIELD_ITEM_DESCRIPTION to description
        )

        addItemToBoard(BOARD_WENT_BADLY, item)
    }

    private fun addActionPoint(userId: String, description: String) {
        val item = hashMapOf(
            FIELD_USER_ID to userId,
            FIELD_ITEM_DESCRIPTION to description
        )

        addItemToBoard(BOARD_ACTION_POINTS, item)
    }

    private fun addItemToBoard(board: String, item: HashMap<String, String>) {
        db.collection(TABLE_RETROS)
            .document(currentRetroId.orEmpty())
            .collection(board)
            .add(item)
    }
}