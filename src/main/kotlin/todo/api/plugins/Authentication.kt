package todo.api.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*


fun Application.configureAuthentication() {
    install(Authentication) {
       jwt {
            jwtConfig.configureKtorFeature(this)
       }
    }
}