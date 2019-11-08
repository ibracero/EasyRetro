package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.mapper.RetroMapper
import com.ibracero.retrum.data.mapper.StatementMapper
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.Companion.RETRO_UUID
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RepositoryImpl(
    val localDataStore: LocalDataStore,
    val firebaseDataStore: FirebaseDataStore,
    val retroMapper: RetroMapper,
    val statementMapper: StatementMapper,
    dispatchers: CoroutineDispatcherProvider
) : Repository {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    override fun loadRetro() {
        scope.launch {
            val retroResponse = firebaseDataStore.loadRetro()
            localDataStore.createOrUpdateRetro(retroMapper.map(retroResponse))
            localDataStore.createOrUpdateStatements(
                retroResponse.positivePoints
                    .plus(retroResponse.negativePoints)
                    .plus(retroResponse.actionPoints)
                    .map(statementMapper::map)
            )
        }
    }

    override fun getStatements(statementType: StatementType): LiveData<List<Statement>> {
        return when (statementType) {
            POSITIVE -> localDataStore.getPositiveStatements(RETRO_UUID)
            NEGATIVE -> localDataStore.getNegativeStatements(RETRO_UUID)
            ACTION_POINT -> localDataStore.getActionPoints(RETRO_UUID)
        }
    }

    override fun createUser() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addStatement(statementType: StatementType, description: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeItem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}