import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.seismiks.nyamapp.data.remote.RemoteRepository
import com.seismiks.nyamapp.data.remote.response.HistoryAllResponse
import com.seismiks.nyamapp.data.remote.response.HistoryTodayResponse
import com.seismiks.nyamapp.data.Result

class HistoryViewModel(private val repository: RemoteRepository) : ViewModel() {

    private var todayHistoryResult: LiveData<Result<HistoryTodayResponse>>? = null
    private var allHistoryResult: LiveData<Result<HistoryAllResponse>>? = null

    fun getHistoryToday(token: String): LiveData<Result<HistoryTodayResponse>> {
        val currentResult = todayHistoryResult
        if (currentResult != null) {
            return currentResult
        }

        val newResult = repository.getHistoryTodayAndYesterday(token)
        todayHistoryResult = newResult
        return newResult
    }

    fun getHistoryAll(token: String): LiveData<Result<HistoryAllResponse>> {
        val currentResult = allHistoryResult
        if (currentResult != null) {
            return currentResult
        }

        val newResult = repository.getHistoryAll(token)
        allHistoryResult = newResult
        return newResult
    }

    fun deleteHistoryAll(token: String) = repository.deleteAllHistory(token)

}