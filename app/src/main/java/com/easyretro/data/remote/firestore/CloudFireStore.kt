package com.easyretro.data.remote.firestore

object CloudFireStore {

    object FirestoreTable {
        const val TABLE_USERS = "users"
        const val TABLE_RETROS = "retros"
    }

    object FirestoreCollection {
        const val COLLECTION_STATEMENTS = "statements"
        const val COLLECTION_USERS = "users"
    }

    object FirestoreField {
        const val RETRO_TITLE = "title"
        const val RETRO_CREATED = "created"
        const val RETRO_USERS = "users"
        const val RETRO_OWNER_EMAIL = "owner_email"
        const val RETRO_LOCKED = "locked"
        const val USER_FIRST_NAME = "first_name"
        const val USER_LAST_NAME = "last_name"
        const val USER_PHOTO_URL = "photo_url"
        const val USER_RETROS = "retros"
        const val STATEMENT_TYPE = "type"
        const val STATEMENT_AUTHOR = "author"
        const val STATEMENT_DESCRIPTION = "description"
        const val STATEMENT_CREATED = "created"
    }
}