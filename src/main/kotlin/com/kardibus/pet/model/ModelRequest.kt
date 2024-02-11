package com.kardibus.pet.model

data class ModelRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<Message>
)

data class CompletionOptions(
    val stream: Boolean,
    val temperature: Double,
    val maxTokens: String
)

data class Message(
    val role: String,
    val text: String?
)