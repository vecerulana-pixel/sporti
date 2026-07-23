package com.sporti.core.data.remote

import android.text.Html
import com.sporti.core.data.local.EventEntity
import com.sporti.core.data.local.NewsEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SportsRemoteDataSource @Inject constructor(
    private val api: SportsApi,
) {
    private val leagueIds = listOf("4328", "4335", "4331", "4332")

    suspend fun fetchEvents(): List<EventEntity> = coroutineScope {
        leagueIds.map { id -> async { api.upcomingEvents(id).events.orEmpty() } }
            .awaitAll()
            .flatten()
            .mapNotNull { it.toEntity() }
            .distinctBy(EventEntity::id)
    }
}

@Singleton
class NewsRemoteDataSource @Inject constructor(
    private val client: OkHttpClient,
) {
    suspend fun fetchNews(): List<NewsEntity> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://feeds.bbci.co.uk/sport/rss.xml")
            .header("User-Agent", "Sporti/1.0 Android RSS reader")
            .build()
        val xml = client.newCall(request).execute().use { response ->
            check(response.isSuccessful) { "BBC RSS returned ${response.code}" }
            response.body?.string().orEmpty()
        }
        parseRss(xml)
    }

    private fun parseRss(xml: String): List<NewsEntity> {
        val parser = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }.newPullParser()
        parser.setInput(StringReader(xml))
        val articles = mutableListOf<NewsEntity>()
        var insideItem = false
        var title = ""
        var summary = ""
        var link = ""
        var published = 0L
        var imageUrl: String? = null

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name.lowercase()) {
                    "item" -> {
                        insideItem = true
                        title = ""
                        summary = ""
                        link = ""
                        published = 0L
                        imageUrl = null
                    }
                    "title" -> if (insideItem) title = parser.nextText().cleanHtml()
                    "description" -> if (insideItem) summary = parser.nextText().cleanHtml()
                    "link" -> if (insideItem) link = parser.nextText().trim()
                    "pubdate" -> if (insideItem) {
                        published = runCatching {
                            ZonedDateTime.parse(parser.nextText(), DateTimeFormatter.RFC_1123_DATE_TIME)
                                .toInstant().toEpochMilli()
                        }.getOrDefault(0L)
                    }
                    "media:thumbnail", "media:content", "enclosure" -> if (insideItem && imageUrl == null) {
                        imageUrl = parser.getAttributeValue(null, "url")
                    }
                }
            } else if (event == XmlPullParser.END_TAG && parser.name.equals("item", true)) {
                insideItem = false
                if (title.isNotBlank() && link.isNotBlank()) {
                    articles += NewsEntity(
                        id = link.hashCode().toString(),
                        title = title,
                        summary = summary,
                        publishedAtMillis = published,
                        url = link,
                        imageUrl = imageUrl,
                        source = "BBC Sport",
                    )
                }
            }
            event = parser.next()
        }
        return articles.take(40)
    }
}

private fun EventDto.toEntity(): EventEntity? {
    val safeId = id ?: return null
    val start = runCatching {
        LocalDateTime.parse(timestamp).toInstant(ZoneOffset.UTC).toEpochMilli()
    }.getOrDefault(0L)
    val normalizedStatus = when {
        status.orEmpty().contains("finish", ignoreCase = true) -> "FINISHED"
        status.orEmpty().contains("progress", ignoreCase = true) ||
            status.orEmpty().contains("live", ignoreCase = true) -> "LIVE"
        else -> "UPCOMING"
    }
    return EventEntity(
        id = safeId,
        league = league.orEmpty(),
        sport = sport.orEmpty(),
        homeTeam = homeTeam.orEmpty(),
        awayTeam = awayTeam.orEmpty(),
        homeScore = homeScore?.toIntOrNull(),
        awayScore = awayScore?.toIntOrNull(),
        startTimeMillis = start,
        status = normalizedStatus,
        venue = venue?.takeIf(String::isNotBlank),
        imageUrl = thumbnail ?: poster,
    )
}

@Suppress("DEPRECATION")
private fun String.cleanHtml(): String = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    .toString()
    .replace(Regex("\\s+"), " ")
    .trim()
