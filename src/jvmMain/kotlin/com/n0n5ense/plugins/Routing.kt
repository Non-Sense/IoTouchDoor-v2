package com.n0n5ense.plugins

import com.n0n5ense.door.DoorService
import com.n0n5ense.getPostData
import com.n0n5ense.index
import com.n0n5ense.isAdminRole
import com.n0n5ense.model.json.*
import com.n0n5ense.persistence.PhysicalLogService
import com.n0n5ense.persistence.TouchLogService
import com.n0n5ense.persistence.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import kotlinx.html.HTML
import java.io.File


private val reactPages = listOf(
    "/",
    "/login",
    "/logout",
    "/unk"
)

fun Application.configureRouting() {

    routing {
        reactPages.forEach {
            get(it) {
                call.respondHtml(HttpStatusCode.OK, HTML::index)
            }
        }

        static("/static") {
            staticRootFolder = File("./static")
            files(".")
            resource("keylocker2.js")
            resource("keylocker2.js.map")
        }

        route("/api") {

            get() {
                call.respondText("Hello World!?")
            }

            get("/testmodel") {
                call.respond(DoorStatus(true,false,true))
            }


            route("/user") {
                post<RegisterUser>("/register") {
                    registerUser(it)
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
                        doorLog()
                    }
                }

                route("/card") {
                    post {

                    }
                    get {

                    }
                    get("/log") {
                        cardLog()
                    }
                }

            }
        }

    }
}


private suspend fun PipelineContext<Unit, ApplicationCall>.registerUser(user: RegisterUser) {
//    val post = getPostData<RegisterUser>() ?: return
    if(UserService.get(user.id) != null) {
        call.respond(HttpStatusCode.Conflict)
        return
    }
    UserService.create(user)
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.login() {
    val post = getPostData<LoginUser>() ?: return
    if(UserService.checkPassword(post)) {
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
    val force = action.force?.let {
        if(!isAdminRole()) {
            call.respond(HttpStatusCode.Forbidden)
            return
        }
        it
    } ?: false
    when(action.action.lowercase()) {
        "lock" -> DoorService.lock(force)
        "unlock" -> DoorService.unlock()
        else -> {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
    }
    call.respond(HttpStatusCode.OK)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.doorStatus() {
    val status = DoorService.status()
    call.respond(HttpStatusCode.OK, DoorStatus(
        status != null,
        status?.isClose == true,
        status?.isLock == true
    ))
}

private suspend fun PipelineContext<Unit, ApplicationCall>.doorLog() {
    val w = call.parameters["w"]?.toIntOrNull()?.takeIf { it <= 500 } ?: 50
    val p = call.parameters["p"]?.toIntOrNull() ?: 0
    val result = PhysicalLogService.get(p, w)
        .map { DoorLog(it.id.value, it.action, it.time.toString()) }
    call.respond(HttpStatusCode.OK, result)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.cardLog() {
    val w = call.parameters["w"]?.toIntOrNull()?.takeIf { it <= 500 } ?: 50
    val p = call.parameters["p"]?.toIntOrNull() ?: 0
    val result = TouchLogService.get(p, w)
        .map { CardTouchLog(it.id.value, it.cardId, it.accept, it.time.toString()) }
    call.respond(HttpStatusCode.OK, result)
}