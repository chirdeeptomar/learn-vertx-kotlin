package com.traffix.progress.services

import io.vertx.core.impl.logging.LoggerFactory
import jakarta.inject.Singleton

interface AuditService {
    fun audit()
}

@Singleton
class Auditor : AuditService {
    private val logger = LoggerFactory.getLogger(Auditor::class.java)

    override fun audit() {
        logger.info("Creating an audit entry")
    }
}