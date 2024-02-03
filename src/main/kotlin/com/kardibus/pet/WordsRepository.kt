package com.kardibus.pet

import com.kardibus.pet.model.Words
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WordsRepository : JpaRepository<Words, Int> {

    @Query("select count(w.id) from Words w where w.word = :string limit 1", nativeQuery = true)
    fun findByWordOutInt(string: String): Int

    @Query(
        "SELECT w.id,w.word FROM words w WHERE word_similarity(w.word, :string) > 0.7",
        nativeQuery = true
    )
    fun findByWordSimilarity(string: String): List<Words>
}