package com.zaheer.upireconcilepro.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.zaheer.upireconcilepro.data.model.CSVData
import com.zaheer.upireconcilepro.data.model.ColumnMapping
import com.zaheer.upireconcilepro.util.CSVParser
import com.zaheer.upireconcilepro.util.ColumnDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class FileRepository(
    private val context: Context
) {
    companion object {
        private const val TAG = "FileRepository"
    }

    sealed class FileResult<out T> {
        data class Success<T>(val data: T) : FileResult<T>()
        data class Error(val message: String) : FileResult<Nothing>()
    }

    data class ParsedFileData(
        val csvData: CSVData,
        val columnMapping: ColumnMapping?,
        val confidence: Float
    )

    suspend fun parseCSVFile(
        uri: Uri,
        delimiter: Char = ',',
        encoding: Charset = Charsets.UTF_8
    ): FileResult<CSVData> {
        return withContext(Dispatchers.IO) {
            try {
                val config = CSVParser.ParseConfig(
                    delimiter = delimiter,
                    encoding = encoding,
                    skipEmptyRows = true,
                    trimWhitespace = true
                )

                when (val result = CSVParser.parseCSV(context, uri, config)) {
                    is CSVParser.ParseResult.Success -> {
                        if (CSVParser.validateCSVStructure(result.data)) {
                            Log.d(TAG, "CSV parsed successfully: ${result.data.headers.size} columns, ${result.data.rows.size} rows")
                            FileResult.Success(result.data)
                        } else {
                            Log.e(TAG, "CSV structure validation failed")
                            FileResult.Error("Invalid CSV structure: inconsistent column count")
                        }
                    }
                    is CSVParser.ParseResult.Error -> {
                        Log.e(TAG, "CSV parsing failed: ${result.message}")
                        FileResult.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing CSV file", e)
                FileResult.Error("Failed to parse file: ${e.message}")
            }
        }
    }

    suspend fun parseCSVFileWithAutoDetect(uri: Uri): FileResult<CSVData> {
        return withContext(Dispatchers.IO) {
            try {
                when (val result = CSVParser.parseCSVWithAutoDetectDelimiter(context, uri)) {
                    is CSVParser.ParseResult.Success -> {
                        Log.d(TAG, "CSV parsed with auto-detect: ${result.data.headers.size} columns")
                        FileResult.Success(result.data)
                    }
                    is CSVParser.ParseResult.Error -> {
                        Log.e(TAG, "CSV auto-detect parsing failed: ${result.message}")
                        FileResult.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error auto-detecting CSV", e)
                FileResult.Error("Failed to parse file: ${e.message}")
            }
        }
    }

    suspend fun parseWithColumnDetection(uri: Uri): FileResult<ParsedFileData> {
        return withContext(Dispatchers.IO) {
            try {
                val csvResult = parseCSVFileWithAutoDetect(uri)

                when (csvResult) {
                    is FileResult.Success -> {
                        val csvData = csvResult.data
                        val columnMapping = ColumnDetector.detectColumns(csvData)
                        
                        val confidence = if (columnMapping != null) {
                            ColumnDetector.getConfidenceScore(csvData, columnMapping)
                        } else {
                            0f
                        }

                        Log.d(TAG, "Column detection: mapping=${columnMapping != null}, confidence=$confidence")

                        FileResult.Success(
                            ParsedFileData(
                                csvData = csvData,
                                columnMapping = columnMapping,
                                confidence = confidence
                            )
                        )
                    }
                    is FileResult.Error -> {
                        FileResult.Error(csvResult.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing with column detection", e)
                FileResult.Error("Failed to detect columns: ${e.message}")
            }
        }
    }

    suspend fun detectColumns(csvData: CSVData): FileResult<ColumnMapping> {
        return withContext(Dispatchers.IO) {
            try {
                val mapping = ColumnDetector.detectColumns(csvData)
                
                if (mapping != null) {
                    val confidence = ColumnDetector.getConfidenceScore(csvData, mapping)
                    Log.d(TAG, "Columns detected with confidence: $confidence")
                    FileResult.Success(mapping)
                } else {
                    Log.e(TAG, "Failed to detect required columns")
                    FileResult.Error("Could not automatically detect required columns. Please map manually.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error detecting columns", e)
                FileResult.Error("Column detection failed: ${e.message}")
            }
        }
    }

    suspend fun validateColumnMapping(
        csvData: CSVData,
        mapping: ColumnMapping
    ): FileResult<Float> {
        return withContext(Dispatchers.IO) {
            try {
                val headerCount = csvData.headers.size

                if (mapping.referenceIndex >= headerCount ||
                    mapping.amountIndex >= headerCount ||
                    mapping.dateIndex >= headerCount ||
                    (mapping.merchantIndex != null && mapping.merchantIndex >= headerCount)
                ) {
                    return@withContext FileResult.Error("Invalid column mapping: index out of bounds")
                }

                val confidence = ColumnDetector.getConfidenceScore(csvData, mapping)
                Log.d(TAG, "Column mapping validated with confidence: $confidence")
                
                FileResult.Success(confidence)
            } catch (e: Exception) {
                Log.e(TAG, "Error validating column mapping", e)
                FileResult.Error("Validation failed: ${e.message}")
            }
        }
    }

    suspend fun getFileInfo(uri: Uri): FileResult<FileInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)

                        val displayName = if (displayNameIndex != -1) {
                            it.getString(displayNameIndex)
                        } else {
                            "Unknown"
                        }

                        val size = if (sizeIndex != -1) {
                            it.getLong(sizeIndex)
                        } else {
                            -1L
                        }

                        Log.d(TAG, "File info: name=$displayName, size=$size")
                        return@withContext FileResult.Success(
                            FileInfo(name = displayName, size = size)
                        )
                    }
                }
                
                FileResult.Error("Could not retrieve file information")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file info", e)
                FileResult.Error("Failed to get file info: ${e.message}")
            }
        }
    }

    data class FileInfo(
        val name: String,
        val size: Long
    )

    suspend fun validateCSVFile(uri: Uri): FileResult<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val fileInfo = getFileInfo(uri)
                
                if (fileInfo is FileResult.Error) {
                    return@withContext FileResult.Error(fileInfo.message)
                }

                val info = (fileInfo as FileResult.Success).data

                if (info.size > 50 * 1024 * 1024) {
                    return@withContext FileResult.Error("File too large (max 50MB)")
                }

                if (!info.name.endsWith(".csv", ignoreCase = true)) {
                    return@withContext FileResult.Error("File must be a CSV file")
                }

                FileResult.Success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error validating CSV file", e)
                FileResult.Error("Validation failed: ${e.message}")
            }
        }
    }
}
