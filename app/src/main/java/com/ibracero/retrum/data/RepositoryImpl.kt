package com.ibracero.retrum.data

import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType

class RepositoryImpl(val firebaseDataStore: FirebaseDataStore) : Repository {

    override suspend fun openRetro() {
        firebaseDataStore.getLatestOrCreateRetro()
    }

    override fun createUser() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStatements(statementType: StatementType) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addStatement(statementType: StatementType, description: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeItem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}