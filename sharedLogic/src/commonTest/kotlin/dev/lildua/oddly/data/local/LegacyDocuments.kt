package dev.lildua.oddly.data.local

/**
 * The saved document exactly as release version 1 wrote it (pretty-printed,
 * defaults encoded, no `swapsUsed` on the daily challenge).
 *
 * Never edit this: it is a recording of a shipped version, and the test that
 * reads it is what proves an upgrade keeps people's data. When the schema moves
 * to version 3, add `V2_DOCUMENT` next to it instead of touching this one.
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
