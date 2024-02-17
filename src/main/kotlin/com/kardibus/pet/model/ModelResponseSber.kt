package com.kardibus.pet.model

data class ModelResponseSber(
    val choices: List<ChoiceDTO>,
    val created: Long,
    val model: String,
    val `object`: String,
    val usage: UsageDTO?,
)

data class ChoiceDTO(
    val message: MessageDTO,
    val index: Int,
    val finish_reason: String
)

data class UsageDTO(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int,
    val system_tokens: Int
)