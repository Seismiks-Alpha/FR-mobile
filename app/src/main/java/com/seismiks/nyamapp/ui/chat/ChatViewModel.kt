package com.seismiks.nyamapp.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seismiks.nyamapp.data.Result
import com.seismiks.nyamapp.data.local.Chat
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.ChatResponse
import com.seismiks.nyamapp.utils.ChatSenders
import kotlinx.coroutines.launch

class ChatViewModel(private val remoteRepository: RemoteRepository) : ViewModel() {

    private val _chatMessagesInternal = MutableLiveData<MutableList<Chat>>(mutableListOf())
    val chatMessages: MutableLiveData<MutableList<Chat>> get() = _chatMessagesInternal

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private var messageIdCounter = 0L

    private var currentRepoResponseLiveData: LiveData<Result<ChatResponse>>? = null
    private var currentRepoObserver: Observer<Result<ChatResponse>>? = null

    fun sendMessage(token: String, userId: String, questionText: String) {

        val userMessage = Chat(
            id = messageIdCounter++,
            message = questionText,
            sender = ChatSenders.USER,
            timestamp = System.currentTimeMillis()
        )
        var updatedChatList = _chatMessagesInternal.value.orEmpty().toMutableList()
        updatedChatList.add(userMessage)

        _isLoading.value = true
        _errorMessage.value = null

        val typingIndicatorMessage = Chat(
            id = messageIdCounter++,
            message = "Bot sedang mengetik...",
            sender = ChatSenders.BOT_TYPING_INDICATOR,
            timestamp = System.currentTimeMillis()
        )
        updatedChatList.add(typingIndicatorMessage)
        _chatMessagesInternal.value = ArrayList(updatedChatList)

        currentRepoResponseLiveData?.removeObserver(currentRepoObserver ?: return)

        viewModelScope.launch {
            val repoLiveData = remoteRepository.postQuestion(token, userId, questionText)
            currentRepoResponseLiveData = repoLiveData

            val observer = object : Observer<Result<ChatResponse>> {
                override fun onChanged(result: Result<ChatResponse>) {
                    var messagesAfterLoading = _chatMessagesInternal.value.orEmpty().toMutableList()

                    messagesAfterLoading.removeAll { it.sender == ChatSenders.BOT_TYPING_INDICATOR }

                    when (result) {
                        is Result.Loading -> {
                            return
                        }
                        is Result.Success -> {
                            val botReplyText = result.data.response
                            if (!botReplyText.isNullOrEmpty()) {
                                val botMessage = Chat(
                                    id = messageIdCounter++,
                                    message = botReplyText,
                                    sender = ChatSenders.BOT,
                                    timestamp = System.currentTimeMillis()
                                )
                                messagesAfterLoading.add(botMessage)
                            } else {
                                val emptyResponseMessage = Chat(
                                    id = messageIdCounter++,
                                    message = "Maaf, saya tidak menemukan jawaban untuk itu.",
                                    sender = ChatSenders.BOT,
                                    timestamp = System.currentTimeMillis()
                                )
                                messagesAfterLoading.add(emptyResponseMessage)
                            }
                            _isLoading.value = false
                        }
                        is Result.Error -> {
                            val errorBubbleMessage = Chat(
                                id = messageIdCounter++,
                                message = "Maaf, terjadi kesalahan: ${result.error}", // Pesan error sebagai chat
                                sender = ChatSenders.BOT, // Tampilkan seolah-olah dari Bot atau "Sistem"
                                timestamp = System.currentTimeMillis()
                            )
                            messagesAfterLoading.add(errorBubbleMessage)
                            Log.e("ChatViewModel", "ChatRepository Error: ${result.error}")
                            _isLoading.value = false
                        }
                        null -> {
                            val nullErrorBubbleMessage = Chat(
                                id = messageIdCounter++,
                                message = "Maaf, terjadi kesalahan internal. Coba lagi nanti.",
                                sender = ChatSenders.BOT,
                                timestamp = System.currentTimeMillis()
                            )
                            messagesAfterLoading.add(nullErrorBubbleMessage)
                            _isLoading.value = false
                        }
                    }

                    _chatMessagesInternal.value = ArrayList(messagesAfterLoading)

                    currentRepoResponseLiveData?.removeObserver(this)
                    if (currentRepoObserver == this) {
                        currentRepoResponseLiveData = null
                        currentRepoObserver = null
                    }
                }
            }
            currentRepoObserver = observer
            repoLiveData.observeForever(observer)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentRepoResponseLiveData?.removeObserver(currentRepoObserver ?: return)
        currentRepoResponseLiveData = null
        currentRepoObserver = null
    }
}