package com.llamatik.repository

import com.llamatik.repository.profile.Profiles
import com.llamatik.repository.user.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

private const val DEFAULT_MAX_POOL_SIZE = 3

object DatabaseFactory {
    fun init() {
        Database.connect(hikari())

        transaction {
            SchemaUtils.create(tables = arrayOf(Users, Profiles))
        }
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = System.getenv("JDBC_DRIVER")
        config.jdbcUrl = System.getenv("JDBC_DATABASE_URL")
        // Keep the old small default, but allow larger deployments to tune DB concurrency without rebuilding.
        config.maximumPoolSize = configuredMaxPoolSize()
        config.isAutoCommit = false
        config.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        val user = System.getenv("DB_USER")
        if (user != null) {
            config.username = user
        }
        val password = System.getenv("DB_PASSWORD")
        if (password != null) {
            config.password = password
        }
        config.validate()
        return HikariDataSource(config)
    }

    private fun configuredMaxPoolSize(): Int {
        return System.getenv("DB_MAX_POOL_SIZE")
            ?.toIntOrNull()
            ?.takeIf { it > 0 }
            ?: DEFAULT_MAX_POOL_SIZE
    }

    suspend fun <T> dbQuery(block: () -> T): T =
        withContext(Dispatchers.IO) {
            transaction { block() }
        }
}
