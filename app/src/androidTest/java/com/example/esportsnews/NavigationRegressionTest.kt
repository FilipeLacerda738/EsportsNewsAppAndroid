package com.example.esportsnews

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.ViewModelStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.esportsnews.app.EsportsApp
import com.example.esportsnews.data.remote.ApiService
import com.example.esportsnews.data.remote.dto.AppVersion
import com.example.esportsnews.data.remote.dto.PaginatedMatchResponse
import com.example.esportsnews.data.repository.AppUpdatesRepository
import com.example.esportsnews.data.repository.MatchesRepository
import com.example.esportsnews.feature.matchdetail.MatchDetailViewModel
import com.example.esportsnews.feature.matches.MatchesViewModel
import com.example.esportsnews.feature.updates.AppUpdatesViewModel
import com.example.esportsnews.data.remote.dto.Match
import com.example.esportsnews.data.remote.dto.MatchDetail
import com.example.esportsnews.data.remote.dto.Team
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response
import java.lang.reflect.Proxy
import java.util.Locale

/** Exercises real navigation/routes with a synchronous fake API, never the Render backend. */
@RunWith(AndroidJUnit4::class)
class NavigationRegressionTest {
    @get:Rule val compose = createComposeRule()
    private val store = ViewModelStore()
    @After fun clearModels() { compose.runOnIdle { store.clear() } }

    @Test fun navigationRendersPortuguese() = smoke("pt-BR")
    @Test fun navigationRendersEnglish() = smoke("en")
    @Test fun navigationRendersSpanish() = smoke("es")

    private fun smoke(language: String) {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(base.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
        }
        val context = base.createConfigurationContext(configuration)
        val match = Match(1, 1, "CSGO", "not_started", 0, 0, null, null,
            team_a = Team(1, "Alpha", "ALP", null), team_b = Team(2, "Bravo", "BRV", null), league = null)
        val api = Proxy.newProxyInstance(ApiService::class.java.classLoader, arrayOf(ApiService::class.java)) { _, method, _ ->
            when (method.name) {
                "getMatches" -> Response.success(PaginatedMatchResponse(1, 1, 1, false, listOf(match)))
                "getMatchDetails" -> MatchDetail(1, "CSGO", "not_started", null, null, 0, 0, null,
                    league = null, teamA = null, teamB = null, games = null)
                "getAppVersion" -> Response.success(AppVersion(BuildConfig.VERSION_NAME, "https://example.com/app.apk", ""))
                else -> error("Unexpected API method: ${method.name}")
            }
        } as ApiService
        lateinit var matches: MatchesViewModel
        lateinit var details: MatchDetailViewModel
        lateinit var updates: AppUpdatesViewModel
        compose.runOnUiThread {
            matches = MatchesViewModel(MatchesRepository(api))
            details = MatchDetailViewModel(MatchesRepository(api))
            updates = AppUpdatesViewModel(AppUpdatesRepository(api))
            store.put("matches", matches); store.put("details", details); store.put("updates", updates)
        }
        compose.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration) {
                EsportsApp(matches, details, updates)
            }
        }
        compose.onNodeWithText("Alpha").performScrollTo().performClick()
        compose.onAllNodesWithText(context.getString(R.string.match_detail_title))[0].assertIsDisplayed()
        compose.onNodeWithContentDescription(context.getString(R.string.cd_back)).performClick()
        compose.onNodeWithContentDescription(context.getString(R.string.open_menu)).performScrollTo().performClick()
        compose.onNodeWithText(context.getString(R.string.settings_title)).performClick()
        compose.onNodeWithText(context.getString(R.string.settings_updates)).assertIsDisplayed()
        compose.onAllNodesWithText(context.getString(R.string.settings_language)).assertCountEquals(2)
        compose.onNodeWithContentDescription(context.getString(R.string.open_menu)).performClick()
        compose.onNodeWithText(context.getString(R.string.matches_section)).performClick()
        compose.onNodeWithText(context.getString(R.string.search_team)).assertIsDisplayed()
    }
}
