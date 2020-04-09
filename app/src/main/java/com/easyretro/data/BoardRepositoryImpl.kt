package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.Statement
import com.easyretro.data.mapper.StatementRemoteToDomainMapper
import com.easyretro.data.mapper.UserRemoteToDomainMapper
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.Failure
import com.easyretro.domain.StatementType
import com.easyretro.domain.StatementType.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
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

    override suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>> {
        return when (statementType) {
            POSITIVE -> localDataStore.getPositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.getNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.getActionPoints(retroUuid)
        }.flowOn(dispatchers.io)
    }

    override suspend fun addStatement(retroUuid: String, description: String, type: StatementType): Either<Failure, Unit> {
        return withContext(dispatchers.io) {
            Timber.d("Adding ${type.name} statement $description")
            remoteDataStore.addStatementToBoard(
                retroUuid = retroUuid,
                statementRemote = StatementRemote(
                    userEmail = userEmail,
                    description = description,
                    statementType = type.toString().toLowerCase(Locale.getDefault())
                )
            )
        }
    }

    override suspend fun removeStatement(statement: Statement): Either<Failure, Unit> =
        withContext(dispatchers.io) {
            Timber.d("Removing statement ${statement.description}")
            remoteDataStore.removeStatement(retroUuid = statement.retroUuid, statementUuid = statement.uuid)
        }

    override suspend fun startObservingStatements(retroUuid: String): Flow<Either<Failure, Unit>> {
        Timber.d("Start observing statements for $retroUuid")
        return remoteDataStore.observeStatements(userEmail, retroUuid)
            .map { either ->
                either.map { statements ->
                    Timber.d("Statement list update: ${statements.joinToString(",") { it.description }}")
                    localDataStore.saveStatements(statements.map(statementRemoteToDomainMapper::map))
                }
            }.flowOn(dispatchers.io)
    }

    override suspend fun startObservingRetroUsers(retroUuid: String): Flow<Either<Failure, Unit>> {
        Timber.d("Start observing users for $retroUuid")
        return remoteDataStore.observeRetroUsers(retroUuid)
            .map { either ->
                either.map { users ->
                    Timber.d("User list update: ${users.joinToString(",") { it.email.orEmpty() }}")
                    localDataStore.updateRetroUsers(retroUuid, users.map(userRemoteToDomainMapper::map))
                }
            }.flowOn(dispatchers.io)
    }
}