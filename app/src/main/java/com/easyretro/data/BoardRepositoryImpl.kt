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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*

class BoardRepositoryImpl(
    val localDataStore: LocalDataStore,
    val remoteDataStore: RemoteDataStore,
    val statementRemoteToDomainMapper: StatementRemoteToDomainMapper,
    val userRemoteToDomainMapper: UserRemoteToDomainMapper,
    val dispatchers: CoroutineDispatcherProvider
) : BoardRepository {

    private val userEmail: String
        get() = FirebaseAuth.getInstance().currentUser?.email.orEmpty()

    private val job = Job()
    private val coroutineContext = job + dispatchers.io
    private val scope = CoroutineScope(coroutineContext)

    override suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>> {
        return when (statementType) {
            POSITIVE -> localDataStore.getPositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.getNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.getActionPoints(retroUuid)
        }.flowOn(dispatchers.io)
    }

    override suspend fun addStatement(
        retroUuid: String,
        description: String,
        statementType: StatementType
    ): Either<Failure, Unit> {
        return withContext(dispatchers.io) {
            remoteDataStore.addStatementToBoard(
                retroUuid = retroUuid,
                statementRemote = StatementRemote(
                    userEmail = userEmail,
                    description = description,
                    statementType = statementType.toString().toLowerCase(Locale.getDefault())
                )
            )
        }
    }

    override suspend fun removeStatement(statement: Statement): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            remoteDataStore.removeStatement(retroUuid = statement.retroUuid, statementUuid = statement.uuid)
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