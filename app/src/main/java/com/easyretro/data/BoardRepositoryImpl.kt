package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.mapper.StatementDbToDomainMapper
import com.easyretro.data.local.mapper.UserDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.data.remote.mapper.StatementRemoteToDbMapper
import com.easyretro.data.remote.mapper.UserRemoteToDbMapper
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.model.Failure
import com.easyretro.domain.model.Statement
import com.easyretro.domain.model.StatementType
import com.easyretro.domain.model.StatementType.*
import com.easyretro.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

class BoardRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val authDataStore: AuthDataStore,
    private val statementRemoteToDbMapper: StatementRemoteToDbMapper,
    private val statementDbToDomainMapper: StatementDbToDomainMapper,
    private val userRemoteToDbMapper: UserRemoteToDbMapper,
    private val userDbToDomainMapper: UserDbToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : BoardRepository {

    private val userEmail: String
        get() = authDataStore.getCurrentUserEmail()

    override suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>> {
        val localStatements = when (statementType) {
            POSITIVE -> localDataStore.observePositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.observeNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.observeActionPoints(retroUuid)
        }
        return localStatements.map { it.map(statementDbToDomainMapper::map) }
            .flowOn(dispatchers.io())
    }

    override suspend fun addStatement(
        retroUuid: String,
        description: String,
        type: StatementType
    ): Either<Failure, Unit> {
        return withContext(dispatchers.io()) {
            Timber.d("Adding statement $description")
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
        withContext(dispatchers.io()) {
            Timber.d("Removing statement ${statement.description}")
            remoteDataStore.removeStatement(retroUuid = statement.retroUuid, statementUuid = statement.uuid)
        }

    override suspend fun startObservingStatements(retroUuid: String): Flow<Either<Failure, Unit>> {
        Timber.d("Start observing statements for $retroUuid")
        return remoteDataStore.observeStatements(userEmail, retroUuid)
            .map { either ->
                either.map { statements ->
                    localDataStore.saveStatements(statements.map(statementRemoteToDbMapper::map))
                }
            }.flowOn(dispatchers.io())
    }
}