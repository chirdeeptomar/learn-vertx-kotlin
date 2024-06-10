package com.traffix.progress.services

import io.vertx.core.impl.logging.LoggerFactory

interface AuditService {
    fun audit()
}

class Auditor : AuditService {
    private val logger = LoggerFactory.getLogger(Auditor::class.java)

    override fun audit() {
        logger.info("Creating an audit entry")
    }
}