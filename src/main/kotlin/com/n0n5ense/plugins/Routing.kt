package com.n0n5ense.plugins

import com.n0n5ense.door.DoorService
import com.n0n5ense.model.*
import com.n0n5ense.persistence.PhysicalLogService
import com.n0n5ense.persistence.UserService
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.util.pipeline.*


fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api") {
            route("/user") {
                post("/register") {
                    registerUser()
                }

                post("/auth") {
                    login()
                }
            }

            post("/token") {
                getAccessToken()
            }

            authenticate {
                get("/test") {
                    call.respond("unkunkunk")
                }

                route("/door") {
                    post {
                        doorLock()
                    }
                    get {
                        doorStatus()
                    }

                    get("/log") {

                    }
                }

            }
        }

    }
}

private suspend inline fun <reified T : Any> PipelineContext<Unit, ApplicationCall>.getPostData(): T? {
    return kotlin.runCatching {
        call.receive<T>()
    }.onFailure {
        call.respond(HttpStatusCode.BadRequest)
    }.getOrNull()
}

private fun PipelineContext<Unit, ApplicationCall>.isAdminRole(): Boolean {
    return kotlin.runCatching {
        val id = call.principal<JWTPrincipal>()!!.payload.claims["id"]!!.asString()
        UserService.isAdminRole(id)
    }.getOrElse { false }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.registerUser() {
    val post = getPostData<RegisterUser>() ?: return
    if (UserService.get(post.id) != null) {
        call.respond(HttpStatusCode.Conflict)
        return
    }
    UserService.create(post)
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.login() {
    val post = getPostData<LoginUser>() ?: return
    if (UserService.checkPassword(post)) {
        call.respond(RefreshToken(Security.createRefreshToken(post)))
    } else {
        call.respond(HttpStatusCode.Forbidden)
    }
}

private suspend fun PipelineContext<Unit, ApplicationCall>.getAccessToken() {
    val post = getPostData<RefreshToken>() ?: return
    Security.validateRefreshToken(post)?.let {
        Security.createAccessToken(it)?.let { token ->
            call.respond(AccessToken(token))
            return
        }
    }
    call.respond(HttpStatusCode.Forbidden)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.doorLock() {
    val action = getPostData<DoorLockAction>() ?: return
    when(action.action.lowercase()){
        "lock" -> DoorService.lock()
        "unlock" -> DoorService.unlock()
        else -> {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
    }
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.doorStatus() {
    call.respond(HttpStatusCode.OK, DoorStatus.from(DoorService.status()))
}

private suspend fun PipelineContext<Unit, ApplicationCall>.doorLog() {
    val w = call.parameters["w"]?.toIntOrNull()?.takeIf { it<=500 }?:50
    val p = call.parameters["p"]?.toIntOrNull()?:0

    PhysicalLogService.get(p, w)
    call.respond(HttpStatusCode.OK, DoorStatus.from(DoorService.status()))
}