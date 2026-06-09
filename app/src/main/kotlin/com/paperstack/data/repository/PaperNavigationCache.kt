package com.paperstack.data.repository

import com.paperstack.domain.model.Paper
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaperNavigationCache @Inject constructor() {
    private val cache = ConcurrentHashMap<String, Paper>()

    fun put(paper: Paper) {
        cache[paper.id] = paper
    }

    fun get(id: String): Paper? = cache[id]
}
