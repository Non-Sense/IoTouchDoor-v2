package com.n0n5ense

import com.n0n5ense.persistence.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.getPostData(): T? {
    return kotlin.runCatching {
        call.receive<T>()
    }.onFailure {
        call.respond(HttpStatusCode.BadRequest)
    }.getOrNull()
}

fun PipelineContext<Unit, ApplicationCall>.isAdminRole(): Boolean {
    return kotlin.runCatching {
        val id = call.principal<JWTPrincipal>()!!.payload.claims["id"]!!.asString()
        UserService.isAdminRole(id)
    }.getOrElse { false }
}
