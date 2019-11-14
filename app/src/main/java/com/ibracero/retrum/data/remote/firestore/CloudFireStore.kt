package com.ibracero.retrum.data.remote.firestore

object CloudFireStore {

    object FirestoreTable {
        const val TABLE_USERS = "users"
        const val TABLE_RETROS = "retros"
    }

    object FirestoreCollection {
        const val COLLECTION_RETROS = "retros"
        const val COLLECTION_STATEMENTS = "statements"
    }

    object FirestoreField {
        const val RETRO_TITLE = "title"
        const val RETRO_CREATED = "created"
        const val USER_EMAIL = "user_email"
        const val USER_FIRST_NAME = "first_name"
        const val USER_LAST_NAME = "last_name"
        const val STATEMENT_TYPE = "type"
        const val STATEMENT_DESCRIPTION = "description"
        const val STATEMENT_CREATED = "created"
    }
}