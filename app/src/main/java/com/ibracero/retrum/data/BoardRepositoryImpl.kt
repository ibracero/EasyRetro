package com.ibracero.retrum.data

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ibracero.retrum.common.CoroutineDispatcherProvider
import com.ibracero.retrum.data.local.LocalDataStore
import com.ibracero.retrum.data.local.Retro
import com.ibracero.retrum.data.local.Statement
import com.ibracero.retrum.data.mapper.StatementRemoteToDomainMapper
import com.ibracero.retrum.data.mapper.UserRemoteToDomainMapper
import com.ibracero.retrum.data.remote.RemoteDataStore
import com.ibracero.retrum.data.remote.firestore.StatementRemote
import com.ibracero.retrum.domain.BoardRepository
import com.ibracero.retrum.domain.StatementType
import com.ibracero.retrum.domain.StatementType.*
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

    override fun getRetroInfo(retroUuid: String): LiveData<Retro> = localDataStore.getRetroInfo(retroUuid)

    override fun getStatements(retroUuid: String, statementType: StatementType): LiveData<List<Statement>> {
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
                    userEmail = userEmail,
                    description = description,
                    statementType = statementType.toString().toLowerCase(Locale.getDefault())
                )
            )
        }
    }

    override fun startObservingStatements(retroUuid: String) {
        remoteDataStore.observeStatements(userEmail, retroUuid) {
            scope.launch {
                localDataStore.saveStatements(it.map(statementRemoteToDomainMapper::map))
            }
        }
    }

    override fun startObservingRetroUsers(retroUuid: String) {
        remoteDataStore.observeRetroUsers(retroUuid) {
            scope.launch {
                localDataStore.updateRetroUsers(retroUuid, it)
            }
        }
    }

    override fun stopObservingStatements() {
        remoteDataStore.stopObservingStatements()
    }

    override fun removeStatement(statement: Statement) {
        remoteDataStore.removeStatement(retroUuid = statement.retroUuid, statementUuid = statement.uuid)
    }

    override fun dispose() {
        scope.cancel()
    }
}