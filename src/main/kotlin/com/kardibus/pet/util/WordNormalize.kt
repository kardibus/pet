package com.kardibus.pet.util

import org.springframework.stereotype.Service

@Service
class WordNormalize {

    private var mutableMap:MutableMap<String,List<String>> = HashMap()

    private fun addMap(){
        mutableMap.put("а", listOf("а", "a", "@"))

    }
}