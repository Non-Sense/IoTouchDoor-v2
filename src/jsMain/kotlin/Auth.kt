import react.*
import react.router.Navigate


data class AuthUser(
    var refreshToken: String
)

typealias AuthUserState = StateInstance<AuthUser?>

val AuthUserContext = createContext<AuthUserState>()

private val redirectToLogin = FC<Props> {
    Navigate {
        to = "/login"
    }
}

val PrivateElement = FC<PropsWithChildren> { props ->
    AuthUserContext.Consumer {
        this.children = {
            val authUser by it
            val isAuthenticated = authUser != null
            if(isAuthenticated)
                props.children
            else
                redirectToLogin.create()
        }
    }
}