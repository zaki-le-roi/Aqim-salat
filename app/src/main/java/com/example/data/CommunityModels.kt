package com.example.data

data class CommunityPost(
    val id: Int,
    val authorName: String,
    val authorCountry: String,
    val content: String,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class CommunityPoll(
    val id: Int,
    val questionAr: String,
    val questionEn: String,
    val optionsAr: List<String>,
    val optionsEn: List<String>,
    val votes: List<Int>,
    val totalVotes: Int,
    val votedOptionIndex: Int? = null
)

data class FajrDayRecord(
    val dayNameAr: String,
    val dayNameEn: String,
    val dateString: String,
    val status: String
)
