package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.UserRemoteToDomainMapper
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore
import com.ibracero.retrum.data.remote.cloudstore.FirebaseDataStore.Companion.RETRO_UUID
import com.ibracero.retrum.data.remote.cloudstore.StatementRemote
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.random.Random

class RepositoryImpl(
    val localDataStore: LocalDataStore,
    val firebaseDataStore: FirebaseDataStore,
    val retroRemoteToDomainMapper: RetroRemoteToDomainMapper,
    val statementRemoteToDomainMapper: StatementRemoteToDomainMapper,
    val userRemoteToDomainMapper: UserRemoteToDomainMapper,
    dispatchers: CoroutineDispatcherProvider
) : Repository {

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    init {
        startObservingUserRetros()
    }

    override fun getRetros(): LiveData<List<Retro>> = localDataStore.getRetros()

    override fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>> {
        startObservingStatements(retroUuid)
        return when (statementType) {
            POSITIVE -> localDataStore.getPositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.getNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.getActionPoints(retroUuid)
        }
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


    private fun startObservingStatements(retroUuid: String) {
        firebaseDataStore.observeStatements(retroUuid) {
            scope.launch {
                localDataStore.saveStatements(it.map(statementRemoteToDomainMapper::map))
            }
        }
    }

    private fun startObservingUserRetros() {
        firebaseDataStore.observeUserRetros {
            scope.launch {
                localDataStore.saveRetros(it.map(retroRemoteToDomainMapper::map))
            }
        }
    }

    private fun startObservingUser() {
        firebaseDataStore.observeUser {
            scope.launch {
                if (!it.email.isNullOrEmpty()) localDataStore.saveUser(userRemoteToDomainMapper.map(it))
            }
        }
    }

    override fun dispose() {
        scope.cancel()
    }
}