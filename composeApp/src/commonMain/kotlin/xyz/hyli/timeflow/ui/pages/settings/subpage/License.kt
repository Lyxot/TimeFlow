package xyz.hyli.timeflow.ui.pages.settings.subpage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import org.jetbrains.compose.resources.stringResource
import timeflow.composeapp.generated.resources.Res
import timeflow.composeapp.generated.resources.back
import xyz.hyli.timeflow.utils.currentPlatform
import xyz.hyli.timeflow.utils.getLibrariesState
import xyz.hyli.timeflow.utils.isDesktop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(navHostController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp)
            .then(
                if (currentPlatform().isDesktop())
                    Modifier.padding(top = 16.dp)
                else Modifier
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .then(
                    if (currentPlatform().isDesktop())
                        Modifier.padding(end = 16.dp)
                    else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    navHostController.popBackStack()
                },
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(Res.string.back)
                )
            }
        }
        val libraries by getLibrariesState()
        LibrariesContainer(libraries, Modifier.fillMaxSize())
    }
}
