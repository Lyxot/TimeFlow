plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.build.config).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.hot.reload).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.ktorfit).apply(false)
}
