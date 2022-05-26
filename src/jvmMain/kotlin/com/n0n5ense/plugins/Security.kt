package com.n0n5ense.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.n0n5ense.model.json.LoginUser
import com.n0n5ense.model.json.RefreshToken
import com.n0n5ense.persistence.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

private lateinit var audience: String
private lateinit var issuer: String
private lateinit var algorithm: Algorithm

fun Application.configureSecurity(environment: ApplicationEnvironment) {

    authentication {
        jwt {
            audience = environment.config.property("jwt.audience").getString()
            algorithm = Algorithm.HMAC256(environment.config.property("jwt.secret").getString())
            issuer = environment.config.property("jwt.domain").getString()
            realm = environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(algorithm)
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if(credential.payload.audience.contains(audience)) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}

class Security {
    companion object {
        private val refreshTokenVerifier = JWT
            .require(algorithm)
            .withAudience("${audience}_ref")
            .withIssuer(issuer)
            .build()

        fun validateRefreshToken(token: RefreshToken): String? {
            val jwt = kotlin.runCatching {
                refreshTokenVerifier.verify(token.refreshToken)!!
            }.getOrElse { return null }
            return jwt.claims["id"]?.asString()
        }

        fun createRefreshToken(loginUser: LoginUser): String {
            return JWT.create()
                .withAudience("${audience}_ref")
                .withClaim("id", loginUser.id)
                .withIssuer(issuer)
                .sign(algorithm)
        }

        fun createAccessToken(userId: String): String? {
            val user = UserService.get(userId) ?: return null
            if(!user.enabled)
                return null
            return JWT.create()
                .withAudience(audience)
                .withExpiresAt(Date(Date().time + 60000))
                .withClaim("id", userId)
                .withIssuer(issuer)
                .sign(algorithm)
        }
    }
}
