package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.home.SimpleHomePage
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.shared.BrandedSearchField
import edu.cit.gako.brainbox.shared.EmptyStateCard
import edu.cit.gako.brainbox.shared.NotebookCard

@Composable
internal fun LibraryScreen(
    notebooks: List<NotebookSummary>,
    syncNotice: String?,
    syncedAtLabel: String?,
    contentPadding: PaddingValues,
    onFeatureRequest: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = notebooks.filter {
        query.isBlank() ||
            it.title.contains(other = query, ignoreCase = true) ||
            (it.categoryName ?: "").contains(other = query, ignoreCase = true)
    }

    SimpleHomePage(
        title = "Library",
        description = "Browse notebooks with the same calm surfaces and warm hierarchy as the web.",
        syncNotice = syncNotice,
        syncedAtLabel = syncedAtLabel,
        contentPadding = contentPadding
    ) {
        BrandedSearchField(query, { query = it }, "Search notebooks")
        Spacer(modifier = Modifier.height(16.dp))
        if (filtered.isEmpty()) {
            EmptyStateCard("Nothing in the library yet", "When notebooks arrive from the web, you'll see them listed here.")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filtered.forEach { notebook ->
                    NotebookCard(notebook, "Open editor") {
                        onFeatureRequest("The mobile notebook editor is the next phase of the build.")
                    }
                }
            }
        }
    }
}


