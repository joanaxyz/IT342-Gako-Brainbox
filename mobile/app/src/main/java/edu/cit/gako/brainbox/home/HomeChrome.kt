package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.shared.BottomNavIconButton
import edu.cit.gako.brainbox.shared.LogoMark
import edu.cit.gako.brainbox.ui.theme.Border
import edu.cit.gako.brainbox.ui.theme.Cream
import edu.cit.gako.brainbox.ui.theme.Ink
import edu.cit.gako.brainbox.ui.theme.Ink3

@Composable
internal fun HomeTopBar(currentTab: HomeTab) {
    Surface(color = Cream) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LogoMark(size = 42)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("BrainBox", style = MaterialTheme.typography.labelMedium, color = Ink3)
                        Text(currentTab.label, style = MaterialTheme.typography.titleLarge, color = Ink)
                    }
                }
            }
            HorizontalDivider(color = Border)
        }
    }
}

@Composable
internal fun HomeBottomBar(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit
) {
    Surface(
        modifier = Modifier.navigationBarsPadding(),
        color = Cream.copy(alpha = 0.98f),
        shadowElevation = 10.dp
    ) {
        Column {
            HorizontalDivider(color = Border)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeTab.values().forEach { tab ->
                    BottomNavIconButton(
                        tab = tab,
                        selected = tab == currentTab,
                        onClick = { onTabSelected(tab) }
                    )
                }
            }
        }
    }
}


