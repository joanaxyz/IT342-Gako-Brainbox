package edu.cit.gako.brainbox.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.R
import edu.cit.gako.brainbox.ui.theme.White

@Composable
internal fun LogoMark(size: Int) {
    Surface(
        modifier = Modifier.size(size.dp),
        shape = RoundedCornerShape((size * 0.3f).dp),
        color = White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, White.copy(alpha = 0.16f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "BrainBox",
                modifier = Modifier.size((size * 0.55f).dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

