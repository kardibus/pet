package com.kardibus.pet.model

data class ModelRequestSber (
     val model: String,
     val temperature :Double,
     val top_p :Double,
     val n :Int,
     val max_tokens :Int,
     val repetition_penalty :Double,
     val stream:Boolean,
     val update_interval:Int,
     val messages: List<MessageDTO>
)

data class MessageDTO (
     val role: String,
     val content: String
)