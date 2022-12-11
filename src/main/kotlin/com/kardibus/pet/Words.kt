package com.kardibus.pet

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Words {
    @Id
    val id:Int = 0
    val word:String = ""


    override fun toString(): String {
        return "Words(id=$id, word='$word')"
    }
}