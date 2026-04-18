import java.io.File
import java.util.Properties
import org.gradle.api.GradleException
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("org.jetbrains.kotlin.kapt")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val isWindows = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)

fun existingExecutable(path: String?): String? {
    if (path.isNullOrBlank()) {
        return null
    }

    val executable = File(path)
    return executable.takeIf { it.isFile && it.exists() && it.canExecute() }?.absolutePath
}

fun findExecutableOnPath(vararg names: String): String? {
    val pathEntries = System.getenv("PATH")
        ?.split(File.pathSeparatorChar)
        ?.filter { it.isNotBlank() }
        .orEmpty()

    return pathEntries.firstNotNullOfOrNull { entry ->
        names.firstNotNullOfOrNull { name ->
            existingExecutable(File(entry, name).absolutePath)
        }
    }
}

fun resolveEmbeddedEditorNodeBinary(): String? {
    val configuredNode = existingExecutable(localProperties.getProperty("BRAINBOX_NODE_BIN"))
        ?: existingExecutable(System.getenv("BRAINBOX_NODE_BIN"))

    if (configuredNode != null) {
        return configuredNode
    }

    val pathResolvedNode = if (isWindows) {
        findExecutableOnPath("node.exe")
    } else {
        findExecutableOnPath("node")
    }

    if (pathResolvedNode != null) {
        return pathResolvedNode
    }

    val userHome = System.getProperty("user.home")
    val fallbackCandidates = if (isWindows) {
        listOf(
            System.getenv("LOCALAPPDATA")?.let { "$it\\Programs\\nodejs\\node.exe" },
            System.getenv("ProgramFiles")?.let { "$it\\nodejs\\node.exe" },
            System.getenv("ProgramFiles(x86)")?.let { "$it\\nodejs\\node.exe" },
            "$userHome\\.cache\\codex-runtimes\\codex-primary-runtime\\dependencies\\node\\bin\\node.exe"
        )
    } else {
        listOf(
            "/usr/local/bin/node",
            "/opt/homebrew/bin/node",
            "$userHome/.nvm/current/bin/node",
            "$userHome/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/bin/node"
        )
    }

    return fallbackCandidates.firstNotNullOfOrNull(::existingExecutable)
}

val webProjectDir = rootProject.file("../web")
val webSourceDir = webProjectDir.resolve("src")
val webPublicDir = webProjectDir.resolve("public")
val webDistDir = webProjectDir.resolve("dist")
val webViteCli = webProjectDir.resolve("node_modules/vite/bin/vite.js")
val embeddedEditorAssetsDir = layout.buildDirectory.dir("generated/embeddedEditorAssets")
val embeddedEditorNodeBin = resolveEmbeddedEditorNodeBinary()
val androidApiBaseUrl = localProperties.getProperty("BRAINBOX_API_BASE_URL", "http://10.0.2.2:8080/")

val buildEmbeddedEditorWeb by tasks.registering {
    inputs.files(
        fileTree(webSourceDir),
        fileTree(webPublicDir),
        webProjectDir.resolve("index.html"),
        webProjectDir.resolve("mobile-editor.html"),
        webProjectDir.resolve("package.json"),
        webProjectDir.resolve("package-lock.json"),
        webProjectDir.resolve("vite.config.js")
    ).withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.dir(webDistDir)

    doFirst {
        if (!webViteCli.exists()) {
            throw GradleException(
                "Embedded notebook editor dependencies are missing. Run `npm install` in ../web once."
            )
        }

        if (embeddedEditorNodeBin == null) {
            throw GradleException(
                "Could not find a Node executable for the embedded notebook editor build. Set BRAINBOX_NODE_BIN in mobile/local.properties or your environment."
            )
        }
    }

    doLast {
        exec {
            workingDir = webProjectDir
            commandLine(embeddedEditorNodeBin, webViteCli.absolutePath, "build")
        }
    }
}

val syncEmbeddedEditorAssets by tasks.registering(Sync::class) {
    dependsOn(buildEmbeddedEditorWeb)
    from(webDistDir)
    into(embeddedEditorAssetsDir)

    doFirst {
        if (!webDistDir.exists()) {
            throw GradleException(
                "Embedded notebook editor assets are missing after the web build step."
            )
        }
    }
}

android {
    namespace = "edu.cit.gako.brainbox"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.cit.gako.brainbox"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )
        buildConfigField(
            "String",
            "BRAINBOX_API_BASE_URL",
            "\"$androidApiBaseUrl\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            assets.srcDir(embeddedEditorAssetsDir)
        }
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.named("preBuild") {
    dependsOn(syncEmbeddedEditorAssets)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation("org.wordpress:aztec:v1.6.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
