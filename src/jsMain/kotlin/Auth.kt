import com.n0n5ense.model.json.LoginUser
import com.n0n5ense.model.json.RefreshToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.*


data class AuthUser(
    var refreshToken: String
)

typealias AuthUserState = StateInstance<AuthUser?>

val AuthUserContext = createContext<AuthUserState>()

val AuthUserProvider = FC<PropsWithChildren> {
    val state = useState<AuthUser?>(null)
    val login: suspend ((LoginUser) -> Unit) = { user ->
        postJsonData<RefreshToken, LoginUser>("$serverAddress/api/user/auth", user)
            .onSuccess {
                state.component2()(AuthUser(it.refreshToken))
            }
    }
    val logout = {
        state.component2()(null)
    }

}

private fun attemptLogin(user: LoginUser, callback: ((Result<RefreshToken>) -> Unit)) {
    MainScope().launch {
        callback.invoke(postJsonData("$serverAddress/api/user/auth", user))
    }
}
