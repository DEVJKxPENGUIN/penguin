package com.devjk.penguin.domain.oidc

import com.devjk.penguin.external.GithubApiHelper
import org.springframework.stereotype.Component

@Component
class GithubConnector(
    private val githubApiHelper: GithubApiHelper
) : Connector {

    override fun getProviderLink(state: String): String {
        return githubApiHelper.getGithubLoginUrl(state)
    }

    override fun getProviderUserInfo(code: String): ProviderUserInfo? {
        return githubApiHelper.getAccessToken(code)?.let {
            return githubApiHelper.getUser(it.accessToken)?.let { user ->
                return ProviderUserInfo(user.id, user.email)
            }
        }

    }
}