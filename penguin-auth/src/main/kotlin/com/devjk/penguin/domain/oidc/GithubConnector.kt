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
        TODO("Not yet implemented")
    }
}