package livecricket.livecrickettv.cricketstreaming.Network

import livecricket.livecrickettv.cricketstreaming.BuildConfig
import livecricket.livecrickettv.cricketstreaming.Database.*
import livecricket.livecrickettv.cricketstreaming.Models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val apiService: ApiService,
    private val appDao: AppDao
) {

    suspend fun fetchAndSaveConfig(onResult: (Boolean, String?) -> Unit): Boolean {
        try {
            val response = apiService.getAppConfig("Bearer ${BuildConfig.API_TOKEN}")
            if (response.data.isNotEmpty()) {
                val appData = response.data[0]

                val appEntity = AppEntity(
                    id = appData.id,
                    packageName = appData.packageName,
                    appName = appData.appName,
                    appType = appData.appType,
                    category = appData.category,
                    currentVersion = appData.currentVersion,
                    updateRequired = appData.updateRequired,
                    isActive = appData.isActive,
                    newPackageName = appData.newPackageName,
                    licenseKey = appData.licenseKey,
                    productId = appData.productId
                )

                val adEntities = appData.ads?.map { ad ->
                    AdEntity(
                        id = ad.id,
                        platform = ad.platform,
                        adUnitId = ad.adUnitId,
                        adPlacement = ad.adPlacement,
                        priority = ad.priority,
                        isActive = ad.isActive,
                        frequencyCap = ad.frequencyCap,
                        appId = appData.id
                    )
                } ?: emptyList()

                val streamingEntities = mutableListOf<StreamingEntity>()
                val tournamentEntities = mutableListOf<TournamentEntity>()
                val eventEntities = mutableListOf<EventEntity>()
                val highlightEntities = mutableListOf<HighlightEntity>()
                val linkEntities = mutableListOf<LinkEntity>()

                appData.streaming?.forEach { streamingWrapper ->
                    streamingWrapper.streamingId?.let { streaming ->
                        streamingEntities.add(
                            StreamingEntity(
                                id = streaming.id,
                                status = streaming.status,
                                showCricketHighlights = streaming.showCricketHighlights,
                                showFootballHighlights = streaming.showFootballHighlights,
                                showOtherSportsHighlights = streaming.showOtherSportsHighlights,
                                newAppOutsideUrl = streaming.newAppOutsideUrl,
                                forceNewAppOutsideUrl = streaming.forceNewAppOutsideUrl,
                                appSportType = streaming.appSportType,
                                bannerAds = streaming.bannerAds,
                                onPauseAd = streaming.onPauseAd,
                                showOtherSports = streaming.showOtherSports,
                                liveCricket = streaming.liveCricket,
                                liveFootball = streaming.liveFootball,
                                liveOtherSport = streaming.liveOtherSport,
                                splashImageLink = streaming.splashImageLink,
                                otherSports = streaming.otherSports,
                                appId = appData.id
                            )
                        )

                        streaming.tournaments?.forEach { tournamentWrapper ->
                            tournamentWrapper.tournamentsId?.let { tournament ->
                                tournamentEntities.add(
                                    TournamentEntity(
                                        id = tournament.id,
                                        status = tournament.status,
                                        name = tournament.name,
                                        thumbUrl = tournament.thumbUrl,
                                        isVisible = tournament.isVisible,
                                        startTime = tournament.startTime,
                                        endTime = tournament.endTime,
                                        description = tournament.description,
                                        sportType = tournament.sportType,
                                        streamingId = streaming.id
                                    )
                                )

                                tournament.events?.forEach { eventWrapper ->
                                    eventWrapper.eventsId?.let { event ->
                                        eventEntities.add(
                                            EventEntity(
                                                id = event.id,
                                                eventName = event.eventName,
                                                eventSlug = event.eventSlug,
                                                eventThumbUrl = event.eventThumbUrl,
                                                eventUrl = event.eventUrl,
                                                isHighlight = event.isHighlight,
                                                isVisible = event.isVisible,
                                                startTime = event.startTime,
                                                endTime = event.endTime,
                                                description = event.description,
                                                teamAName = event.teamAName,
                                                teamAImage = event.teamAImage,
                                                teamBName = event.teamBName,
                                                teamBUrl = event.teamBUrl,
                                                tournamentId = tournament.id
                                            )
                                        )

                                        event.highlights?.forEach { highlightWrapper ->
                                            highlightWrapper.highlightsId?.let { highlight ->
                                                highlightEntities.add(
                                                    HighlightEntity(
                                                        id = highlight.id,
                                                        title = highlight.title,
                                                        videoLink = highlight.videoLink,
                                                        thumbnailLink = highlight.thumbnailLink,
                                                        durationSeconds = highlight.durationSeconds,
                                                        viewCount = highlight.viewCount,
                                                        isVisible = highlight.isVisible,
                                                        eventId = event.id
                                                    )
                                                )
                                            }
                                        }

                                        event.links?.forEach { linkWrapper ->
                                            linkWrapper.linksId?.let { link ->
                                                linkEntities.add(
                                                    LinkEntity(
                                                        id = link.id,
                                                        linkName = link.linkName,
                                                        linkUrl = link.linkUrl,
                                                        linkType = link.linkType,
                                                        mpdLink = link.mpdLink,
                                                        mpdKey = link.mpdKey,
                                                        linkImage = link.linkImage,
                                                        isVisible = link.isVisible,
                                                        priority = link.priority,
                                                        eventId = event.id
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                appDao.clearAndInsert(
                    appEntity,
                    adEntities,
                    streamingEntities,
                    tournamentEntities,
                    eventEntities,
                    highlightEntities,
                    linkEntities
                )
                onResult(true, null)
                return true
            } else {
                onResult(false, "No data available from server")
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, e.message ?: "Server connection error")
            return false
        }
    }

    suspend fun getAllAds(): List<AdEntity> {
        return appDao.getAllAds()
    }
}
