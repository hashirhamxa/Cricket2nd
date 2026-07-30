package livecricket.livecrickettv.cricketstreaming.Database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val id: Int,
    val packageName: String?,
    val appName: String?,
    val appType: String?,
    val category: String?,
    val currentVersion: String?,
    val updateRequired: Boolean?,
    val isActive: Boolean?,
    val newPackageName: String?,
    val licenseKey: String?,
    val productId: String?
)

@Entity(tableName = "ads")
data class AdEntity(
    @PrimaryKey val id: Int,
    val platform: String?,
    val adUnitId: String?,
    val adPlacement: String?,
    val priority: Int?,
    val isActive: Boolean?,
    val frequencyCap: Int?,
    val appId: Int
)

@Entity(tableName = "streaming")
data class StreamingEntity(
    @PrimaryKey val id: Int,
    val status: String?,
    val showCricketHighlights: Boolean?,
    val showFootballHighlights: Boolean?,
    val showOtherSportsHighlights: Boolean?,
    val newAppOutsideUrl: String?,
    val forceNewAppOutsideUrl: Boolean?,
    val appSportType: String?,
    val bannerAds: Boolean?,
    val onPauseAd: Boolean?,
    val showOtherSports: Boolean?,
    val liveCricket: Boolean?,
    val liveFootball: Boolean?,
    val liveOtherSport: Boolean?,
    val splashImageLink: String?,
    val otherSports: String?,
    val appId: Int
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: Int,
    val status: String?,
    val name: String?,
    val thumbUrl: String?,
    val isVisible: Boolean?,
    val startTime: String?,
    val endTime: String?,
    val description: String?,
    val sportType: String?,
    val streamingId: Int
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: Int,
    val eventName: String?,
    val eventSlug: String?,
    val eventThumbUrl: String?,
    val eventUrl: String?,
    val isHighlight: Boolean?,
    val isVisible: Boolean?,
    val startTime: String?,
    val endTime: String?,
    val description: String?,
    val teamAName: String?,
    val teamAImage: String?,
    val teamBName: String?,
    val teamBUrl: String?,
    val tournamentId: Int
)

@Entity(tableName = "highlights")
data class HighlightEntity(
    @PrimaryKey val id: Int,
    val title: String?,
    val videoLink: String?,
    val thumbnailLink: String?,
    val durationSeconds: Int?,
    val viewCount: Int?,
    val isVisible: Boolean?,
    val eventId: Int
)

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey val id: Int,
    val linkName: String?,
    val linkUrl: String?,
    val linkType: String?,
    val mpdLink: String?,
    val mpdKey: String?,
    val linkImage: String?,
    val isVisible: Boolean?,
    val priority: Int?,
    val eventId: Int
)
