package com.easyretro.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import arrow.core.Either
import com.google.firebase.auth.FirebaseAuth
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.Statement
import com.easyretro.data.mapper.StatementRemoteToDomainMapper
import com.easyretro.data.mapper.UserRemoteToDomainMapper
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.domain.Failure
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.StatementType
import com.easyretro.domain.StatementType.*
import kotlinx.coroutines.*
import java.util.*

class BoardRepositoryImpl(
    val localDataStore: LocalDataStore,
    val remoteDataStore: RemoteDataStore,
    val statementRemoteToDomainMapper: StatementRemoteToDomainMapper,
    val userRemoteToDomainMapper: UserRemoteToDomainMapper,
    dispatchers: CoroutineDispatcherProvider
) : BoardRepository {

    private val userEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    override fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>> {
        return when (statementType) {
            POSITIVE -> localDataStore.getPositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.getNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.getActionPoints(retroUuid)
        }
    }

    override fun addStatement(
        retroUuid: String,
        description: String,
        statementType: StatementType
    ): LiveData<Either<Failure, Unit>> {
        val statementLiveData = MutableLiveData<Either<Failure, Unit>>()
        scope.launch {
            val eitherResult = remoteDataStore.addStatementToBoard(
                retroUuid = retroUuid,
                statementRemote = StatementRemote(
                    userEmail = userEmail,
                    description = description,
                    statementType = statementType.toString().toLowerCase(Locale.getDefault())
                )
            )
            statementLiveData.postValue(eitherResult)
        }
        return statementLiveData
    }

    override fun startObservingStatements(retroUuid: String) {
        remoteDataStore.observeStatements(userEmail, retroUuid) {
            scope.launch {
                it.map { statements ->
                    localDataStore.saveStatements(statements.map(statementRemoteToDomainMapper::map))
                }
            }
        }
    }

    override fun startObservingRetroUsers(retroUuid: String) {
        remoteDataStore.observeRetroUsers(retroUuid) {
            scope.launch {
                it.map { users ->
                    localDataStore.updateRetroUsers(retroUuid, users.map(userRemoteToDomainMapper::map))
                }
            }
        }
    }

    override fun removeStatement(statement: Statement) {
        remoteDataStore.removeStatement(retroUuid = statement.retroUuid, statementUuid = statement.uuid)
    }

    override fun stopObservingStatements() {
        remoteDataStore.stopObservingStatements()
    }

    override fun stopObservingRetroUsers() {
        remoteDataStore.stopObservingRetroUsers()
    }

    override fun dispose() {
        scope.cancel()
    }
}