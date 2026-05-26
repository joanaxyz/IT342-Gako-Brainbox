package edu.cit.gako.brainbox.features.home.library.data

import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary

internal fun List<CategoryDetail>.withOptimisticCategory(category: CategoryDetail): List<CategoryDetail> =
    (filterNot { it.id == category.id } + category).sortedBy { it.name.lowercase() }

internal fun List<CategoryDetail>.replaceOptimisticCategory(
    temporaryId: Long,
    category: CategoryDetail
): List<CategoryDetail> =
    map { if (it.id == temporaryId) category else it }.sortedBy { it.name.lowercase() }

internal fun List<CategoryDetail>.withoutCategoryById(categoryId: Long): List<CategoryDetail> =
    filterNot { it.id == categoryId }

internal fun List<CategoryDetail>.withRenamedCategory(categoryId: Long, name: String): List<CategoryDetail> =
    map { category ->
        if (category.id == categoryId) {
            category.copy(name = name)
        } else {
            category
        }
    }.sortedBy { it.name.lowercase() }

internal fun List<NotebookSummary>.withNotebookCategory(
    notebookUuid: String,
    category: CategoryDetail?
): List<NotebookSummary> =
    map { notebook ->
        if (notebook.uuid == notebookUuid) {
            notebook.copy(categoryId = category?.id, categoryName = category?.name)
        } else {
            notebook
        }
    }

internal fun List<NotebookSummary>.withNotebookCategoryName(
    categoryId: Long,
    name: String
): List<NotebookSummary> =
    map { notebook ->
        if (notebook.categoryId == categoryId) {
            notebook.copy(categoryName = name)
        } else {
            notebook
        }
    }

internal fun List<NotebookSummary>.withoutCategory(
    categoryId: Long,
    deleteNotebooks: Boolean
): List<NotebookSummary> =
    if (deleteNotebooks) {
        filterNot { it.categoryId == categoryId }
    } else {
        map { notebook ->
            if (notebook.categoryId == categoryId) {
                notebook.copy(categoryId = null, categoryName = null)
            } else {
                notebook
            }
        }
    }
