package edu.cit.gako.brainbox.features.home.library

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.features.home.library.data.LibraryNotebookMutationResult
import edu.cit.gako.brainbox.features.home.library.data.LibraryRepository
import edu.cit.gako.brainbox.features.home.library.data.replaceOptimisticCategory
import edu.cit.gako.brainbox.features.home.library.data.withNotebookCategory
import edu.cit.gako.brainbox.features.home.library.data.withOptimisticCategory
import edu.cit.gako.brainbox.features.home.library.data.withoutCategory
import edu.cit.gako.brainbox.features.home.library.data.withoutCategoryById
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.BrandedSearchField
import edu.cit.gako.brainbox.shared.ui.EmptyStateCard
import edu.cit.gako.brainbox.shared.study.NotebookCard
import edu.cit.gako.brainbox.shared.study.StudyNotebookCardModel
import edu.cit.gako.brainbox.shared.ui.OfflinePackActionButton
import edu.cit.gako.brainbox.shared.ui.SimpleHomePage
import edu.cit.gako.brainbox.shared.ui.SyncNoticeBanner
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Border
import edu.cit.gako.brainbox.shared.ui.theme.Cream2
import edu.cit.gako.brainbox.shared.ui.theme.ErrorRed
import edu.cit.gako.brainbox.shared.ui.theme.Ink
import edu.cit.gako.brainbox.shared.ui.theme.Ink2
import edu.cit.gako.brainbox.shared.ui.theme.Ink3
import edu.cit.gako.brainbox.shared.ui.theme.White
import kotlinx.coroutines.launch

internal const val ALL_CATEGORIES = "all"
internal const val UNCATEGORIZED = "uncategorized"
internal const val SORT_UPDATED_AT = "updatedAt"
internal const val SORT_TITLE = "title"
internal const val SORT_WORDS = "words"
internal const val SORT_CATEGORY_NAME = "name"
internal const val SORT_CATEGORY_COUNT = "count"

internal data class CategoryDeleteTarget(
    val category: CategoryDetail,
    val notebookCount: Int
)

@Composable
internal fun LibraryScreen(
    repository: LibraryRepository,
    notebooks: List<NotebookSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onOpenNotebook: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val offlineNotebookUuids by repository.observeActiveOfflineNotebookUuids().collectAsState(initial = emptySet())

    var localNotebooks by remember(notebooks) { mutableStateOf(notebooks) }
    var localCategories by remember(notebooks) { mutableStateOf(categoriesFromNotebooks(notebooks)) }
    var selectedCategoryId by rememberSaveable { mutableStateOf(ALL_CATEGORIES) }
    var notebookQuery by rememberSaveable { mutableStateOf("") }
    var categoryQuery by rememberSaveable { mutableStateOf("") }
    var notebookSortBy by rememberSaveable { mutableStateOf(SORT_UPDATED_AT) }
    var notebookSortAsc by rememberSaveable { mutableStateOf(false) }
    var categorySortBy by rememberSaveable { mutableStateOf(SORT_CATEGORY_NAME) }
    var categorySortAsc by rememberSaveable { mutableStateOf(true) }
    var message by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryLoadMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var activeOfflineNotebookUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var movingNotebookUuid by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryBusy by rememberSaveable { mutableStateOf(false) }
    var temporaryCategoryId by rememberSaveable { mutableStateOf(-1L) }
    var showCreateCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var moveNotebookTarget by remember { mutableStateOf<NotebookSummary?>(null) }
    var pendingNotebookForCreatedCategory by remember { mutableStateOf<NotebookSummary?>(null) }
    var deleteCategoryTarget by remember { mutableStateOf<CategoryDeleteTarget?>(null) }

    LaunchedEffect(repository) {
        runCatching { repository.getCategories() }
            .onSuccess {
                localCategories = mergeCategories(it, localNotebooks)
                categoryLoadMessage = null
            }
            .onFailure {
                localCategories = mergeCategories(localCategories, localNotebooks)
                categoryLoadMessage = "Live categories are unavailable, so this view is using notebook metadata for now."
            }
    }

    val categories = remember(localCategories, localNotebooks) {
        mergeCategories(localCategories, localNotebooks)
    }

    LaunchedEffect(categories, selectedCategoryId) {
        if (
            selectedCategoryId != ALL_CATEGORIES &&
            selectedCategoryId != UNCATEGORIZED &&
            categories.none { it.id.toString() == selectedCategoryId }
        ) {
            selectedCategoryId = ALL_CATEGORIES
        }
    }

    val categoryCounts = remember(localNotebooks) {
        localNotebooks
            .mapNotNull { it.categoryId }
            .groupingBy { it }
            .eachCount()
    }
    val uncategorizedCount = remember(localNotebooks) {
        localNotebooks.count { it.categoryId == null }
    }

    val filteredCategories = remember(
        categories,
        categoryCounts,
        categoryQuery,
        categorySortBy,
        categorySortAsc
    ) {
        val query = categoryQuery.trim()
        val filtered = if (query.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(query, ignoreCase = true) }
        }

        when (categorySortBy) {
            SORT_CATEGORY_COUNT -> filtered.sortedWith(
                compareBy<CategoryDetail> { categoryCounts[it.id] ?: 0 }
                    .thenBy { it.name.lowercase() }
            )
            else -> filtered.sortedBy { it.name.lowercase() }
        }.let { sorted ->
            if (categorySortAsc) sorted else sorted.asReversed()
        }
    }

    val selectedCategory = remember(categories, selectedCategoryId) {
        categories.firstOrNull { it.id.toString() == selectedCategoryId }
    }

    val displayNotebooks = remember(
        localNotebooks,
        notebookQuery,
        notebookSortBy,
        notebookSortAsc,
        selectedCategoryId
    ) {
        val query = notebookQuery.trim()
        val filtered = localNotebooks.filter { notebook ->
            val matchesQuery = query.isBlank() ||
                notebook.title.contains(query, ignoreCase = true) ||
                notebook.categoryName.orEmpty().contains(query, ignoreCase = true)
            val matchesCategory = when (selectedCategoryId) {
                ALL_CATEGORIES -> true
                UNCATEGORIZED -> notebook.categoryId == null
                else -> notebook.categoryId?.toString() == selectedCategoryId
            }
            matchesQuery && matchesCategory
        }

        val sorted = when (notebookSortBy) {
            SORT_TITLE -> filtered.sortedBy { it.title.lowercase() }
            SORT_WORDS -> filtered.sortedBy { it.wordCount ?: 0 }
            else -> filtered.sortedBy { it.updatedAt.orEmpty() }
        }

        if (notebookSortAsc) sorted else sorted.asReversed()
    }

    val selectedCategoryLabel = when (selectedCategoryId) {
        ALL_CATEGORIES -> "All notebooks"
        UNCATEGORIZED -> "Uncategorized"
        else -> selectedCategory?.name ?: "Library"
    }
    val selectedCategoryCount = when (selectedCategoryId) {
        ALL_CATEGORIES -> localNotebooks.size
        UNCATEGORIZED -> uncategorizedCount
        else -> selectedCategoryId.toLongOrNull()?.let { categoryCounts[it] ?: 0 } ?: 0
    }

    fun mergeNotebook(updated: NotebookDetail, fallback: NotebookSummary? = null) {
        localNotebooks = localNotebooks.map { notebook ->
            if (notebook.uuid == updated.uuid) updated.toSummary(fallback ?: notebook) else notebook
        }
    }

    fun applyNotebookCategory(
        notebook: NotebookSummary,
        category: CategoryDetail?,
        rollbackNotebooks: List<NotebookSummary> = localNotebooks
    ) {
        val apiCategoryId = category?.id ?: -1L
        localNotebooks = localNotebooks.withNotebookCategory(notebook.uuid, category)
        movingNotebookUuid = notebook.uuid
        moveNotebookTarget = null

        scope.launch {
            when (val result = repository.updateNotebookCategory(notebook.uuid, apiCategoryId)) {
                is LibraryNotebookMutationResult.Success -> {
                    val updated = result.notebook
                    if (updated != null) {
                        mergeNotebook(updated, notebook)
                    }
                    message = "\"${notebook.title}\" moved to ${category?.name ?: "Uncategorized"}."
                }
                is LibraryNotebookMutationResult.Conflict -> {
                    localNotebooks = rollbackNotebooks
                    result.latestNotebook?.let { mergeNotebook(it, notebook) }
                    message = result.message
                }
                is LibraryNotebookMutationResult.Failure -> {
                    localNotebooks = rollbackNotebooks
                    message = result.message
                }
            }
            movingNotebookUuid = null
        }
    }

    fun createCategory(name: String) {
        val temporaryCategory = CategoryDetail(id = temporaryCategoryId, name = name)
        val previousCategories = localCategories
        val previousNotebooks = localNotebooks
        val previousSelectedCategoryId = selectedCategoryId
        val pendingNotebook = pendingNotebookForCreatedCategory
        temporaryCategoryId -= 1
        localCategories = localCategories.withOptimisticCategory(temporaryCategory)
        if (pendingNotebook != null) {
            localNotebooks = localNotebooks.withNotebookCategory(pendingNotebook.uuid, temporaryCategory)
            movingNotebookUuid = pendingNotebook.uuid
            moveNotebookTarget = null
        } else {
            selectedCategoryId = temporaryCategory.id.toString()
        }
        categoryBusy = true

        scope.launch {
            runCatching { repository.createCategory(name) }
                .onSuccess { created ->
                    localCategories = mergeCategories(
                        localCategories.replaceOptimisticCategory(temporaryCategory.id, created),
                        localNotebooks
                    )
                    pendingNotebookForCreatedCategory = null
                    if (pendingNotebook != null) {
                        message = "Category \"${created.name}\" created."
                        applyNotebookCategory(pendingNotebook, created, previousNotebooks)
                    } else {
                        selectedCategoryId = created.id.toString()
                        message = "Category \"${created.name}\" created."
                    }
                }
                .onFailure {
                    localCategories = previousCategories
                    localNotebooks = previousNotebooks
                    selectedCategoryId = previousSelectedCategoryId
                    pendingNotebookForCreatedCategory = null
                    movingNotebookUuid = null
                    message = "We couldn't create that category."
                }
            categoryBusy = false
        }
    }

    fun deleteCategory(target: CategoryDeleteTarget, deleteNotebooks: Boolean) {
        val previousCategories = localCategories
        val previousNotebooks = localNotebooks
        val affectedNotebooks = localNotebooks.filter { it.categoryId == target.category.id }
        localCategories = localCategories.withoutCategoryById(target.category.id)
        localNotebooks = localNotebooks.withoutCategory(target.category.id, deleteNotebooks)
        if (selectedCategoryId == target.category.id.toString()) {
            selectedCategoryId = ALL_CATEGORIES
        }
        deleteCategoryTarget = null
        categoryBusy = true

        scope.launch {
            runCatching { repository.deleteCategory(target.category.id, deleteNotebooks) }
                .onSuccess {
                    if (deleteNotebooks) {
                        affectedNotebooks.forEach { notebook ->
                            repository.removeOfflinePackAssets(notebook.uuid)
                        }
                    }
                    val movedCopy = if (!deleteNotebooks && affectedNotebooks.isNotEmpty()) {
                        " ${formatCount(affectedNotebooks.size, "notebook")} moved to Uncategorized."
                    } else {
                        ""
                    }
                    message = "Category \"${target.category.name}\" deleted.$movedCopy"
                }
                .onFailure {
                    localCategories = previousCategories
                    localNotebooks = previousNotebooks
                    message = "We couldn't delete that category."
                }
            categoryBusy = false
        }
    }

    SimpleHomePage(
        title = "Library",
        description = "Create categories, sort notebooks into them, and keep everything easy to find.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        message?.let {
            SyncNoticeBanner(it, "Library changes sync with your web workspace.")
            Spacer(modifier = Modifier.height(16.dp))
        }
        categoryLoadMessage?.let {
            SyncNoticeBanner(it, "Category actions need a live connection.")
            Spacer(modifier = Modifier.height(16.dp))
        }

        CategoryManagerPanel(
            categories = filteredCategories,
            totalCategoryCount = categories.size,
            notebookCount = localNotebooks.size,
            uncategorizedCount = uncategorizedCount,
            categoryCounts = categoryCounts,
            selectedCategoryId = selectedCategoryId,
            query = categoryQuery,
            sortBy = categorySortBy,
            sortAsc = categorySortAsc,
            onQueryChange = { categoryQuery = it },
            onSelectCategory = { selectedCategoryId = it },
            onCreateCategory = { showCreateCategoryDialog = true },
            onDeleteCategory = { category ->
                deleteCategoryTarget = CategoryDeleteTarget(
                    category = category,
                    notebookCount = categoryCounts[category.id] ?: 0
                )
            },
            onSortName = {
                if (categorySortBy == SORT_CATEGORY_NAME) {
                    categorySortAsc = !categorySortAsc
                } else {
                    categorySortBy = SORT_CATEGORY_NAME
                    categorySortAsc = true
                }
            },
            onSortCount = {
                if (categorySortBy == SORT_CATEGORY_COUNT) {
                    categorySortAsc = !categorySortAsc
                } else {
                    categorySortBy = SORT_CATEGORY_COUNT
                    categorySortAsc = false
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LibraryNotebookToolbar(
            selectedCategoryLabel = selectedCategoryLabel,
            selectedCategoryCount = selectedCategoryCount,
            query = notebookQuery,
            sortBy = notebookSortBy,
            sortAsc = notebookSortAsc,
            onQueryChange = { notebookQuery = it },
            onSort = { nextSort ->
                if (notebookSortBy == nextSort) {
                    notebookSortAsc = !notebookSortAsc
                } else {
                    notebookSortBy = nextSort
                    notebookSortAsc = nextSort == SORT_TITLE
                }
            },
            onShowAll = { selectedCategoryId = ALL_CATEGORIES }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (displayNotebooks.isEmpty()) {
            EmptyStateCard(
                title = if (notebookQuery.isBlank()) "No notebooks here yet" else "No matching notebooks",
                body = when {
                    notebookQuery.isNotBlank() -> "Try a different notebook title or category keyword."
                    selectedCategoryId == UNCATEGORIZED -> "Everything is already sorted, or there are no uncategorized notebooks."
                    selectedCategoryId != ALL_CATEGORIES -> "This category is empty right now."
                    else -> "When notebooks arrive from the web, you'll see them listed here."
                }
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayNotebooks.forEach { notebook ->
                    val isOffline = notebook.uuid in offlineNotebookUuids
                    val isWorking = activeOfflineNotebookUuid == notebook.uuid
                    val isMoving = movingNotebookUuid == notebook.uuid
                    NotebookCard(
                        notebook = notebook.toStudyNotebookCardModel(),
                        action = if (isMoving) "Updating category" else "Open editor",
                        footer = {
                            LibraryNotebookFooter(
                                notebook = notebook,
                                isOffline = isOffline,
                                isWorking = isWorking,
                                isMoving = isMoving,
                                onMove = { moveNotebookTarget = notebook },
                                onOfflineClick = {
                                    scope.launch {
                                        activeOfflineNotebookUuid = notebook.uuid
                                        if (isOffline) {
                                            repository.removeOfflinePackAssets(notebook.uuid)
                                            message = "\"${notebook.title}\" was removed from offline storage."
                                        } else {
                                            repository.downloadOfflinePack(notebook.uuid).onSuccess { result ->
                                                val details = buildList {
                                                    if (result.quizCount > 0) add("${result.quizCount} quizzes")
                                                    if (result.flashcardCount > 0) add("${result.flashcardCount} flashcards")
                                                }
                                                message = when {
                                                    details.isEmpty() && result.audioPrepared ->
                                                        "\"${result.notebookTitle}\" is now available offline with audio ready."
                                                    details.isEmpty() ->
                                                        "\"${result.notebookTitle}\" is now available offline, but audio could not be prepared yet."
                                                    result.audioPrepared ->
                                                        "\"${result.notebookTitle}\" is now available offline with ${details.joinToString(" and ")} and audio ready."
                                                    else ->
                                                        "\"${result.notebookTitle}\" is now available offline with ${details.joinToString(" and ")}, but audio could not be prepared yet."
                                                }
                                            }.onFailure {
                                                message = "We couldn't download that notebook for offline use right now."
                                            }
                                        }
                                        activeOfflineNotebookUuid = null
                                    }
                                }
                            )
                        }
                    ) {
                        onOpenNotebook(notebook.uuid)
                    }
                }
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            isBusy = categoryBusy,
            onDismiss = {
                showCreateCategoryDialog = false
                pendingNotebookForCreatedCategory = null
            },
            onCreate = { name ->
                showCreateCategoryDialog = false
                createCategory(name)
            }
        )
    }

    moveNotebookTarget?.let { notebook ->
        MoveNotebookDialog(
            notebook = notebook,
            categories = categories.sortedBy { it.name.lowercase() },
            selectedCategoryId = notebook.categoryId,
            isMoving = movingNotebookUuid == notebook.uuid,
            onDismiss = { moveNotebookTarget = null },
            onCreateCategory = {
                pendingNotebookForCreatedCategory = notebook
                moveNotebookTarget = null
                showCreateCategoryDialog = true
            },
            onSelectCategory = { category -> applyNotebookCategory(notebook, category) }
        )
    }

    deleteCategoryTarget?.let { target ->
        DeleteCategoryDialog(
            target = target,
            isBusy = categoryBusy,
            onDismiss = { deleteCategoryTarget = null },
            onConfirm = { deleteNotebooks -> deleteCategory(target, deleteNotebooks) }
        )
    }
}
