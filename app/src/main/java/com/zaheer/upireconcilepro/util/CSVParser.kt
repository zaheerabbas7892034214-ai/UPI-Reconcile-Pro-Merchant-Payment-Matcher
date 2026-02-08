package com.zaheer.upireconcilepro.util

import android.content.Context
import android.net.Uri
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.zaheer.upireconcilepro.data.model.CSVData
import java.io.InputStream
import java.nio.charset.Charset

object CSVParser {
    
    data class ParseConfig(
        val delimiter: Char = ',',
        val encoding: Charset = Charsets.UTF_8,
        val skipEmptyRows: Boolean = true,
        val trimWhitespace: Boolean = true
    )
    
    sealed class ParseResult {
        data class Success(val data: CSVData) : ParseResult()
        data class Error(val message: String) : ParseResult()
    }
    
    fun parseCSV(
        context: Context,
        uri: Uri,
        config: ParseConfig = ParseConfig()
    ): ParseResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ParseResult.Error("Unable to open file")
            
            val result = parseInputStream(inputStream, config)
            inputStream.close()
            result
        } catch (e: Exception) {
            ParseResult.Error("Parse error: ${e.message ?: "Unknown error"}")
        }
    }
    
    private fun parseInputStream(
        inputStream: InputStream,
        config: ParseConfig
    ): ParseResult {
        try {
            val reader = csvReader {
                delimiter = config.delimiter
                charset = config.encoding.name()
                skipEmptyLine = config.skipEmptyRows
                quoteChar = '"'
                escapeChar = '\\'
            }
            
            val allRows = reader.readAll(inputStream)
            
            if (allRows.isEmpty()) {
                return ParseResult.Error("CSV file is empty")
            }
            
            val headers = if (config.trimWhitespace) {
                allRows[0].map { it.trim() }
            } else {
                allRows[0]
            }
            
            if (headers.isEmpty() || headers.all { it.isBlank() }) {
                return ParseResult.Error("No valid headers found")
            }
            
            val dataRows = allRows.drop(1).map { row ->
                if (config.trimWhitespace) {
                    row.map { it.trim() }
                } else {
                    row
                }
            }.filter { row ->
                row.isNotEmpty() && row.any { it.isNotBlank() }
            }
            
            val csvData = CSVData(
                headers = headers,
                rows = dataRows
            )
            
            return ParseResult.Success(csvData)
            
        } catch (e: Exception) {
            return ParseResult.Error("Failed to parse CSV: ${e.message ?: "Unknown error"}")
        }
    }
    
    fun parseCSVWithAutoDetectDelimiter(
        context: Context,
        uri: Uri,
        encoding: Charset = Charsets.UTF_8
    ): ParseResult {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: return ParseResult.Error("Unable to open file")
        
        return try {
            val firstLines = inputStream.bufferedReader(encoding).use { reader ->
                buildString {
                    repeat(3) {
                        val line = reader.readLine() ?: return@buildString
                        appendLine(line)
                    }
                }
            }
            
            val delimiter = detectDelimiter(firstLines)
            
            parseCSV(context, uri, ParseConfig(delimiter = delimiter, encoding = encoding))
            
        } catch (e: Exception) {
            ParseResult.Error("Auto-detect failed: ${e.message ?: "Unknown error"}")
        } finally {
            inputStream.close()
        }
    }
    
    private fun detectDelimiter(sampleText: String): Char {
        val delimiters = listOf(',', ';', '\t', '|')
        val counts = delimiters.associateWith { delimiter ->
            sampleText.lines().take(2).sumOf { line ->
                line.count { it == delimiter }
            }
        }
        
        return counts.maxByOrNull { it.value }?.key ?: ','
    }
    
    fun parseCSVWithEncodingDetection(
        context: Context,
        uri: Uri,
        delimiter: Char = ','
    ): ParseResult {
        val encodings = listOf(
            Charsets.UTF_8,
            Charset.forName("ISO-8859-1"),
            Charset.forName("Windows-1252"),
            Charsets.UTF_16
        )
        
        for (encoding in encodings) {
            val result = parseCSV(
                context,
                uri,
                ParseConfig(delimiter = delimiter, encoding = encoding)
            )
            
            if (result is ParseResult.Success) {
                return result
            }
        }
        
        return ParseResult.Error("Unable to parse with any supported encoding")
    }
    
    fun validateCSVStructure(csvData: CSVData): Boolean {
        if (csvData.headers.isEmpty()) return false
        if (csvData.rows.isEmpty()) return false
        
        val headerCount = csvData.headers.size
        return csvData.rows.all { row -> 
            row.size == headerCount || row.size < headerCount
        }
    }
}
