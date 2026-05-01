package edu.cit.gako.brainbox.architecture

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MobileArchitectureBoundaryTest {
    private val sourceRoot = resolveSourceRoot()

    @Test
    fun platformDoesNotImportAppOrFeatures() {
        val violations = kotlinFiles(sourceRoot.resolve("edu/cit/gako/brainbox/platform"))
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (line.startsWith("import edu.cit.gako.brainbox.app") ||
                        line.startsWith("import edu.cit.gako.brainbox.features")
                    ) {
                        "${file.relativeTo(sourceRoot)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }

        assertTrue("platform must stay infrastructure-only:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun sharedDoesNotImportAppFeaturesOrNetwork() {
        val violations = kotlinFiles(sourceRoot.resolve("edu/cit/gako/brainbox/shared"))
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (line.startsWith("import edu.cit.gako.brainbox.app") ||
                        line.startsWith("import edu.cit.gako.brainbox.features") ||
                        line.startsWith("import edu.cit.gako.brainbox.platform.network")
                    ) {
                        "${file.relativeTo(sourceRoot)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }

        assertTrue("shared must stay generic:\n${violations.joinToString("\n")}", violations.isEmpty())
    }

    @Test
    fun networkDtosDoNotLiveInPlatformModels() {
        val oldModelsDir = sourceRoot.resolve("edu/cit/gako/brainbox/platform/network/models")

        assertFalse("feature DTOs must not return to platform.network.models", oldModelsDir.exists())
    }

    @Test
    fun homeShellDoesNotLiveInAppPackage() {
        val oldHomeDir = sourceRoot.resolve("edu/cit/gako/brainbox/app/home")

        assertFalse(
            "app owns bootstrap/state/orchestration; authenticated home UI belongs in features.home",
            oldHomeDir.exists()
        )
    }

    @Test
    fun homeTabSurfacesLiveUnderHomeFeature() {
        val expectedHomeFiles = listOf(
            "HomeScene.kt",
            "data/HomeRepository.kt",
            "dashboard/DashboardScreen.kt",
            "library/LibraryScreen.kt",
            "flashcards/FlashcardsScreen.kt",
            "flashcards/FlashcardStudyScreen.kt",
            "flashcards/data/FlashcardRepository.kt",
            "flashcards/data/FlashcardApiService.kt",
            "quizzes/QuizzesScreen.kt",
            "quizzes/QuizStudyScreen.kt",
            "quizzes/data/QuizRepository.kt",
            "quizzes/data/QuizApiService.kt",
            "playlists/PlaylistsScreen.kt",
            "playlists/data/PlaylistRepository.kt",
            "playlists/data/PlaylistApiService.kt",
            "playlists/data/PlaylistOptimisticUpdates.kt",
            "profile/ProfileScreen.kt"
        ).map { relativePath ->
            sourceRoot.resolve("edu/cit/gako/brainbox/features/home/$relativePath")
        }
        val missing = expectedHomeFiles.filterNot { it.exists() }

        assertTrue(
            "authenticated home shell and tabs must stay grouped under features.home:\n" +
                missing.joinToString("\n"),
            missing.isEmpty()
        )

        val oldTabLocations = listOf(
            sourceRoot.resolve("edu/cit/gako/brainbox/features/dashboard"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/library"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/notebook/LibraryScreen.kt"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/notebook/library"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/flashcard"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/quiz"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/playlist"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/profile"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/flashcard/FlashcardsScreen.kt"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/quiz/QuizzesScreen.kt"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/playlist/PlaylistsScreen.kt"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/playlist/components"),
            sourceRoot.resolve("edu/cit/gako/brainbox/features/profile/ProfileScreen.kt")
        )
        val existing = oldTabLocations.filter { it.exists() }

        assertTrue(
            "home tab UI must not drift back into top-level feature roots or notebook:\n" +
                existing.joinToString("\n"),
            existing.isEmpty()
        )
    }

    @Test
    fun homeTabUiUsesHomeOwnedAdapters() {
        val violations = kotlinFiles(sourceRoot.resolve("edu/cit/gako/brainbox/features/home"))
            .filterNot { it.inPathSegment("data") }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (line.contains("BrainBoxAppGraph")) {
                        "${file.relativeTo(sourceRoot)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }

        assertTrue(
            "home tab UI should receive home-owned data adapters from app composition:\n" +
                violations.joinToString("\n"),
            violations.isEmpty()
        )
    }

    @Test
    fun legacyNativeNotebookEditorIsGone() {
        val legacyFiles = listOf(
            "NotebookAiChatComponents.kt",
            "NotebookAiPane.kt",
            "NotebookChromeComponents.kt",
            "NotebookEditorViewModel.kt",
            "NotebookMobileEditorChrome.kt",
            "NotebookNativeEditorView.kt",
            "NotebookPlaybackViewModel.kt",
            "NotebookReviewComponents.kt",
            "NotebookReviewPane.kt",
            "NotebookUiState.kt",
            "NativeNotebookEditorScreen.kt"
        ).map { fileName ->
            sourceRoot.resolve("edu/cit/gako/brainbox/features/notebook/$fileName")
        }
        val existing = legacyFiles.filter { it.exists() }

        assertTrue(
            "mobile uses the embedded web editor/review surface; do not resurrect native notebook editor code:\n" +
                existing.joinToString("\n"),
            existing.isEmpty()
        )
    }

    @Test
    fun playbackDoesNotDumpFilesAtFeatureRoot() {
        val playbackRoot = sourceRoot.resolve("edu/cit/gako/brainbox/features/playback")
        val looseFiles = playbackRoot.listFiles()
            ?.filter { it.isFile && it.extension == "kt" }
            .orEmpty()

        assertTrue(
            "playback files must live in audio, data, model, tts, or ui slices:\n${looseFiles.joinToString("\n")}",
            looseFiles.isEmpty()
        )
    }

    @Test
    fun featureUiDoesNotImportSiblingRepositoriesOrServices() {
        val violations = kotlinFiles(sourceRoot.resolve("edu/cit/gako/brainbox/features"))
            .filterNot { it.inPathSegment("data") }
            .flatMap { file ->
                val owningFeature = file.relativeTo(sourceRoot.resolve("edu/cit/gako/brainbox/features"))
                    .invariantSeparatorsPath
                    .substringBefore("/")
                file.readLines().mapIndexedNotNull { index, line ->
                    val importedFeature = Regex("""^import edu\.cit\.gako\.brainbox\.features\.([^.]+)\.(data\.(?!dto)|.+Repository|.+ApiService)""")
                        .find(line)
                        ?.groupValues
                        ?.getOrNull(1)
                    if (importedFeature != null && importedFeature != owningFeature) {
                        "${file.relativeTo(sourceRoot)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }

        assertTrue(
            "feature UI must not grab sibling repositories/services directly:\n${violations.joinToString("\n")}",
            violations.isEmpty()
        )
    }

    private fun kotlinFiles(root: File): List<File> {
        if (!root.exists()) {
            return emptyList()
        }
        return root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
    }

    private fun File.inPathSegment(segment: String): Boolean =
        invariantSeparatorsPath.split("/").contains(segment)

    private fun resolveSourceRoot(): File {
        val userDir = File(System.getProperty("user.dir").orEmpty())
        val candidates = listOf(
            userDir.resolve("src/main/java"),
            userDir.resolve("app/src/main/java"),
            userDir.parentFile?.resolve("app/src/main/java")
        ).filterNotNull()

        return candidates.firstOrNull { it.exists() }
            ?: error("Could not locate Android source root from ${userDir.absolutePath}")
    }
}
