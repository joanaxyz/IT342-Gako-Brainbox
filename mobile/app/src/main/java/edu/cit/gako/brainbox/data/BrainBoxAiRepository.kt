package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.models.AiConfigListResponse
import edu.cit.gako.brainbox.network.models.AiConfigRequest
import edu.cit.gako.brainbox.network.models.AiConfigResponse
import edu.cit.gako.brainbox.network.models.AiQueryRequest
import edu.cit.gako.brainbox.network.models.AiQueryResponse

internal class BrainBoxAiRepository(
    private val apiService: ApiService
) {
    suspend fun query(request: AiQueryRequest): AiQueryResponse {
        return apiService.queryAiEnvelope(request)
            .requireData("We couldn't reach notebook AI right now.")
    }

    suspend fun getConfig(): AiConfigResponse {
        return apiService.getAiConfigEnvelope()
            .requireData("We couldn't load your active AI configuration.")
    }

    suspend fun listConfigs(): AiConfigListResponse {
        return apiService.listAiConfigsEnvelope()
            .requireData("We couldn't load your AI configurations.")
    }

    suspend fun saveConfig(request: AiConfigRequest): AiConfigResponse {
        return apiService.saveAiConfigEnvelope(request)
            .requireData("We couldn't save that AI configuration.")
    }

    suspend fun selectConfig(configId: Long): AiConfigResponse {
        return apiService.selectAiConfigEnvelope(configId)
            .requireData("We couldn't switch AI configurations.")
    }

    suspend fun deleteConfig(configId: Long) {
        apiService.deleteAiConfigEnvelope(configId)
            .requireSuccess("We couldn't delete that AI configuration.")
    }
}
