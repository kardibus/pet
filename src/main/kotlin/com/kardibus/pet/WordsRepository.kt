package com.kardibus.pet

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordsRepository:JpaRepository<Words,Long>{

    @Query("select count(w.id) from Words w where w.word = :string limit 1", nativeQuery = true)
    fun findByWordOutInt(string: String):Int
}