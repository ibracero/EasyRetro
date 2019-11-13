package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.mapper.RetroRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.UserRemoteToDomainMapper
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.domain.Repository
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*

class RepositoryImpl(
    val localDataStore: LocalDataStore,
    val remoteDataStore: RemoteDataStore,
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

    override fun createRetro(title: String): LiveData<Retro> {
        val retroLiveData = MutableLiveData<Retro>()
        scope.launch {
            val retroRemote = remoteDataStore.createRetro(retroTitle = title)
            retroLiveData.postValue(retroRemoteToDomainMapper.map(retroRemote))
        }
        return retroLiveData
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

    override fun addStatement(retroUuid: String, description: String, statementType: StatementType) {
        scope.launch {
            remoteDataStore.addStatementToBoard(
                retroUuid = retroUuid,
                statementRemote = StatementRemote(
                    userEmail = "yo@yo.com",
                    description = description,
                    statementType = statementType.toString().toLowerCase(Locale.getDefault())
                )
            )
        }
    }

    private fun startObservingStatements(retroUuid: String) {
        remoteDataStore.observeStatements(retroUuid) {
            scope.launch {
                localDataStore.saveStatements(it.map(statementRemoteToDomainMapper::map))
            }
        }
    }

    private fun startObservingUserRetros() {
        remoteDataStore.observeUserRetros {
            scope.launch {
                localDataStore.saveRetros(it.map(retroRemoteToDomainMapper::map))
            }
        }
    }

    private fun startObservingUser() {
        remoteDataStore.observeUser {
            scope.launch {
                if (!it.email.isNullOrEmpty()) localDataStore.saveUser(userRemoteToDomainMapper.map(it))
            }
        }
    }

    override fun removeItem() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dispose() {
        scope.cancel()
    }
}