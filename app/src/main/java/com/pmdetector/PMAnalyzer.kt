package com.pmdetector

class PMAnalyzer {
    private val pmRegex = "(FI-|SRO-)\\d{2,}-\\d{3}[A-Z]?".toRegex()
    
    fun extractPMNumbers(text: String): List<String> {
        return pmRegex.findAll(text)
            .map { it.value }
            .toList()
    }
}