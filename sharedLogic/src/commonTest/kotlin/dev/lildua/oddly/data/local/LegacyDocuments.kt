package dev.lildua.oddly.data.local

/**
 * The saved document exactly as release version 1 wrote it (pretty-printed,
 * defaults encoded, no `swapsUsed` on the daily challenge).
 *
 * Never edit this: it is a recording of a shipped version, and the test that
 * reads it is what proves an upgrade keeps people's data. The same goes for
 * [V2_DOCUMENT] below.
 */
internal const val V1_DOCUMENT: String = """{
    "version": 1,
    "profile": {
        "id": "local-user",
        "displayName": "1% Human",
        "createdAt": "2026-08-20",
        "interests": [
            "HEALTH",
            "FINANCE"
        ],
        "level": 3,
        "xpInLevel": 40
    },
    "settings": {
        "themeMode": "LIGHT",
        "reminderEnabled": false,
        "reminderTime": "21:15",
        "language": "ENGLISH",
        "soundEnabled": true,
        "hapticsEnabled": true
    },
    "completions": [
        {
            "id": "completion_health_walk_500m_20702",
            "challengeId": "health_walk_500m",
            "date": "2026-09-06",
            "completedAt": "08:30",
            "xpEarned": 20,
            "humanityPercent": 5
        },
        {
            "id": "completion_health_water_2l_20703",
            "challengeId": "health_water_2l",
            "date": "2026-09-07",
            "completedAt": "19:45",
            "xpEarned": 30,
            "humanityPercent": 5
        },
        {
            "id": "completion_health_stretch_3min_20704",
            "challengeId": "health_stretch_3min",
            "date": "2026-09-08",
            "completedAt": "07:05",
            "xpEarned": 25,
            "humanityPercent": 5,
            "note": null
        }
    ],
    "dailyChallenge": {
        "date": "2026-09-09",
        "challengeId": "health_stairs",
        "status": "PENDING"
    },
    "skippedCount": 5,
    "hasCompletedOnboarding": true
}"""

/**
 * The saved document exactly as release version 2 wrote it, produced by
 * `OddlyRepository.save` at commit `b275512` (not written by hand): it has
 * `swapsUsed` on the daily challenge but no `favoriteQuoteIds`.
 *
 * Never edit this. When the schema moves to version 4, add `V3_DOCUMENT` next to it.
 */
internal const val V2_DOCUMENT: String = """{
    "version": 2,
    "profile": {
        "id": "local-user",
        "displayName": "1% Human",
        "createdAt": "2026-08-30",
        "interests": [
            "SELF_GROWTH",
            "CREATIVITY"
        ],
        "level": 4,
        "xpInLevel": 15
    },
    "settings": {
        "themeMode": "SYSTEM",
        "reminderEnabled": true,
        "reminderTime": "07:30",
        "language": "VIETNAMESE",
        "soundEnabled": true,
        "hapticsEnabled": true
    },
    "completions": [
        {
            "id": "completion_create_doodle_20702",
            "challengeId": "create_doodle",
            "date": "2026-09-06",
            "completedAt": "08:10",
            "xpEarned": 20,
            "humanityPercent": 5,
            "note": "Drew a cat"
        },
        {
            "id": "completion_create_photo_odd_20703",
            "challengeId": "create_photo_odd",
            "date": "2026-09-07",
            "completedAt": "12:05",
            "xpEarned": 25,
            "humanityPercent": 5,
            "note": null
        },
        {
            "id": "completion_health_stairs_20704",
            "challengeId": "health_stairs",
            "date": "2026-09-08",
            "completedAt": "18:40",
            "xpEarned": 30,
            "humanityPercent": 5,
            "note": "Took the stairs"
        }
    ],
    "dailyChallenge": {
        "date": "2026-09-09",
        "challengeId": "create_new_playlist",
        "status": "PENDING",
        "swapsUsed": 2
    },
    "skippedCount": 4,
    "hasCompletedOnboarding": true
}"""
