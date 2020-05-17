package com.easyretro.data

import arrow.core.Either
import com.easyretro.common.CoroutineDispatcherProvider
import com.easyretro.data.local.LocalDataStore
import com.easyretro.data.local.StatementDb
import com.easyretro.data.local.mapper.StatementDbToDomainMapper
import com.easyretro.data.remote.AuthDataStore
import com.easyretro.data.remote.RemoteDataStore
import com.easyretro.data.remote.firestore.StatementRemote
import com.easyretro.data.remote.mapper.StatementRemoteToDbMapper
import com.easyretro.domain.BoardRepository
import com.easyretro.domain.model.*
import com.easyretro.domain.model.StatementType.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

@Suppress("EXPERIMENTAL_API_USAGE")
class BoardRepositoryImpl(
    private val localDataStore: LocalDataStore,
    private val remoteDataStore: RemoteDataStore,
    private val authDataStore: AuthDataStore,
    private val statementRemoteToDbMapper: StatementRemoteToDbMapper,
    private val statementDbToDomainMapper: StatementDbToDomainMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : BoardRepository {

    private val userEmail: String
        get() = authDataStore.getCurrentUserEmail()

    override suspend fun getStatements(retroUuid: String, statementType: StatementType): Flow<List<Statement>> {
        val localStatements = getLocalStatements(statementType, retroUuid)
        return combine(localDataStore.observeRetro(retroUuid), localStatements) { retroDb, statementsDb ->
            if (retroDb == null || !retroDb.isProtected) statementsDb
            else statementsDb.map { it.copy(removable = false) }
        }.map {
            it.map(statementDbToDomainMapper::map)
        }.flowOn(dispatchers.io())
    }

    override suspend fun addStatement(
        retroUuid: String,
        description: String,
        type: StatementType
    ): Either<Failure, Unit> {
        return withContext(dispatchers.io()) {
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

    override suspend fun removeStatement(retroUuid: String, statementUuid: String): Either<Failure, Unit> =
        withContext(dispatchers.io()) {
            remoteDataStore.removeStatement(retroUuid = retroUuid, statementUuid = statementUuid)
        }

    override suspend fun startObservingStatements(retroUuid: String): Flow<Either<Failure, Unit>> {
        Timber.d("Start observing statements for $retroUuid")
        return remoteDataStore.observeStatements(userEmail, retroUuid)
            .map { either ->
                either.map { statements ->
                    if (statements.isNotEmpty())
                        localDataStore.saveStatements(statements.map(statementRemoteToDbMapper::map))
                }
            }.flowOn(dispatchers.io())
    }

    private fun getLocalStatements(statementType: StatementType, retroUuid: String): Flow<List<StatementDb>> {
        return when (statementType) {
            POSITIVE -> localDataStore.observePositiveStatements(retroUuid)
            NEGATIVE -> localDataStore.observeNegativeStatements(retroUuid)
            ACTION_POINT -> localDataStore.observeActionPoints(retroUuid)
        }
    }
}