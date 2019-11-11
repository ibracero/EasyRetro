package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.Companion.RETRO_UUID
import com.ibracero.retrum.data.remote.cloudstore.StatementRemote
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.random.Random

class RepositoryImpl(
    val localDataStore: LocalDataStore,
    val firebaseDataStore: FirebaseDataStore,
    val retroRemoteToDomainMapper: RetroRemoteToDomainMapper,
    val statementRemoteToDomainMapper: StatementRemoteToDomainMapper,
    dispatchers: CoroutineDispatcherProvider
) : Repository {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    init {
        firebaseDataStore.observeStatements {
            scope.launch {
                localDataStore.save(statement = statementRemoteToDomainMapper.map(it))
            }
        }

        firebaseDataStore.observeUserRetros {
            scope.launch {
                localDataStore.save(retroRemoteToDomainMapper.map(it))
            }
        }
    }

    override fun getRetros(): LiveData<List<Retro>> = localDataStore.getRetros()

    override fun loadRetro() {
        scope.launch {
            val retroResponse = firebaseDataStore.loadRetro()
//            val statementsResponse = firebaseDataStore.loadStatements()
            localDataStore.save(retro = retroRemoteToDomainMapper.map(retroResponse))
//            localDataStore.save(statements = statementsResponse.map(statementRemoteToDomainMapper::map))
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

    override fun addStatement(statement: Statement?) {
        firebaseDataStore.addStatementToBoard(
            StatementRemote(
                userEmail = "yo@yo.com",
                description = Random.nextInt(100).toString(),
                statementType = POSITIVE.toString()
            )
        )
    }

    override fun removeItem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}