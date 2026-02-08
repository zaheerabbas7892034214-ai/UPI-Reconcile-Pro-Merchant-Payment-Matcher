package com.zaheer.upireconcilepro.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.upireconcilepro.data.model.CSVData
import com.zaheer.upireconcilepro.data.model.ColumnMapping
import com.zaheer.upireconcilepro.data.repository.EntitlementRepository
import com.zaheer.upireconcilepro.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadViewModel(
    application: Application,
    private val fileRepository: FileRepository,
    private val entitlementRepository: EntitlementRepository
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "UploadViewModel"
    }

    sealed class UploadUiState {
        object Initial : UploadUiState()
        object Loading : UploadUiState()
        data class FileSelected(
            val fileName: String,
            val fileSize: Long,
            val fileType: FileType
        ) : UploadUiState()
        data class FileParsed(
            val csvData: CSVData,
            val columnMapping: ColumnMapping?,
            val confidence: Float,
            val fileType: FileType
        ) : UploadUiState()
        data class BothFilesReady(
            val invoiceData: ParsedFile,
            val paymentData: ParsedFile,
            val canProceed: Boolean
        ) : UploadUiState()
        data class Error(val message: String) : UploadUiState()
        data class LimitReached(val message: String) : UploadUiState()
    }

    enum class FileType {
        INVOICE, PAYMENT
    }

    data class ParsedFile(
        val csvData: CSVData,
        val columnMapping: ColumnMapping?,
        val confidence: Float,
        val fileName: String
    )

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Initial)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    private var invoiceFile: ParsedFile? = null
    private var paymentFile: ParsedFile? = null

    fun selectFile(uri: Uri, fileType: FileType) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading

            try {
                val isPro = entitlementRepository.isProActive()
                
                if (!isPro) {
                    val hasInvoice = invoiceFile != null
                    val hasPayment = paymentFile != null
                    
                    if ((fileType == FileType.INVOICE && hasInvoice) || 
                        (fileType == FileType.PAYMENT && hasPayment)) {
                        _uiState.value = UploadUiState.LimitReached(
                            "FREE tier allows 1 file each. Upgrade to PRO for unlimited files."
                        )
                        return@launch
                    }
                }

                when (val fileInfoResult = fileRepository.getFileInfo(uri)) {
                    is FileRepository.FileResult.Success -> {
                        val fileInfo = fileInfoResult.data
                        
                        when (val validateResult = fileRepository.validateCSVFile(uri)) {
                            is FileRepository.FileResult.Success -> {
                                _uiState.value = UploadUiState.FileSelected(
                                    fileName = fileInfo.name,
                                    fileSize = fileInfo.size,
                                    fileType = fileType
                                )
                                parseFile(uri, fileType, fileInfo.name)
                            }
                            is FileRepository.FileResult.Error -> {
                                _uiState.value = UploadUiState.Error(validateResult.message)
                            }
                        }
                    }
                    is FileRepository.FileResult.Error -> {
                        _uiState.value = UploadUiState.Error(fileInfoResult.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error selecting file", e)
                _uiState.value = UploadUiState.Error("Failed to select file: ${e.message}")
            }
        }
    }

    private fun parseFile(uri: Uri, fileType: FileType, fileName: String) {
        viewModelScope.launch {
            try {
                when (val result = fileRepository.parseWithColumnDetection(uri)) {
                    is FileRepository.FileResult.Success -> {
                        val parsedData = result.data
                        val parsedFile = ParsedFile(
                            csvData = parsedData.csvData,
                            columnMapping = parsedData.columnMapping,
                            confidence = parsedData.confidence,
                            fileName = fileName
                        )

                        when (fileType) {
                            FileType.INVOICE -> invoiceFile = parsedFile
                            FileType.PAYMENT -> paymentFile = parsedFile
                        }

                        checkBothFilesReady()

                        _uiState.value = UploadUiState.FileParsed(
                            csvData = parsedData.csvData,
                            columnMapping = parsedData.columnMapping,
                            confidence = parsedData.confidence,
                            fileType = fileType
                        )

                        Log.d(TAG, "File parsed: $fileName, confidence=${parsedData.confidence}")
                    }
                    is FileRepository.FileResult.Error -> {
                        _uiState.value = UploadUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing file", e)
                _uiState.value = UploadUiState.Error("Failed to parse file: ${e.message}")
            }
        }
    }

    private fun checkBothFilesReady() {
        val invoice = invoiceFile
        val payment = paymentFile

        if (invoice != null && payment != null) {
            val canProceed = (invoice.columnMapping != null && invoice.confidence > 0.5f) &&
                           (payment.columnMapping != null && payment.confidence > 0.5f)

            _uiState.value = UploadUiState.BothFilesReady(
                invoiceData = invoice,
                paymentData = payment,
                canProceed = canProceed
            )
        }
    }

    fun updateColumnMapping(fileType: FileType, mapping: ColumnMapping) {
        when (fileType) {
            FileType.INVOICE -> {
                invoiceFile = invoiceFile?.copy(columnMapping = mapping, confidence = 1.0f)
            }
            FileType.PAYMENT -> {
                paymentFile = paymentFile?.copy(columnMapping = mapping, confidence = 1.0f)
            }
        }
        checkBothFilesReady()
    }

    fun removeFile(fileType: FileType) {
        when (fileType) {
            FileType.INVOICE -> invoiceFile = null
            FileType.PAYMENT -> paymentFile = null
        }
        
        if (invoiceFile == null && paymentFile == null) {
            _uiState.value = UploadUiState.Initial
        } else {
            checkBothFilesReady()
        }
    }

    fun getInvoiceFile() = invoiceFile
    fun getPaymentFile() = paymentFile

    fun reset() {
        invoiceFile = null
        paymentFile = null
        _uiState.value = UploadUiState.Initial
    }
}
