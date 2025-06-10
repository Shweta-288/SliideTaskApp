package me.shwetagoyal.sliidesampleapp.data

import me.shwetagoyal.sliidesampleapp.BuildConfig
import me.shwetagoyal.sliidesampleapp.network.TokenProvider
import javax.inject.Inject

class StaticTokenProvider @Inject constructor() : TokenProvider {
    override fun getToken(): String = BuildConfig.BEARER_TOKEN
}