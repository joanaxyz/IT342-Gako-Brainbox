package edu.cit.gako.brainbox.features.home.library.data

import edu.cit.gako.brainbox.features.notebook.data.NotebookMutationResult
import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail

class LibraryRepository(
    private val notebooks: NotebookRepository
) {
    suspend fun getCategories(): List<CategoryDetail> =
        notebooks.getCategories()

    suspend fun createCategory(name: String): CategoryDetail =
        notebooks.createCategory(name)

    suspend fun deleteCategory(categoryId: Long, deleteNotebooks: Boolean) {
        notebooks.deleteCategory(categoryId, deleteNotebooks)
    }

    suspend fun updateNotebookCategory(
        notebookUuid: String,
        categoryId: Long?
    ): LibraryNotebookMutationResult =
        notebooks.updateNotebook(notebookUuid, categoryId = categoryId).toLibraryResult()
}

sealed interface LibraryNotebookMutationResult {
    data class Success(val notebook: NotebookDetail?) : LibraryNotebookMutationResult
    data class Conflict(
        val message: String,
        val latestNotebook: NotebookDetail?
    ) : LibraryNotebookMutationResult
    data class Failure(val message: String) : LibraryNotebookMutationResult
}

private fun NotebookMutationResult.toLibraryResult(): LibraryNotebookMutationResult =
    when (this) {
        is NotebookMutationResult.Success -> LibraryNotebookMutationResult.Success(notebook)
        is NotebookMutationResult.Conflict -> LibraryNotebookMutationResult.Conflict(message, latestNotebook)
        is NotebookMutationResult.Failure -> LibraryNotebookMutationResult.Failure(message)
    }
