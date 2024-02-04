package com.kardibus.pet.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "words")
open class Words {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id:Int = 0
    open var word:String = ""

    override fun toString(): String {
        return "Words(id=$id, word='$word')"
    }
}