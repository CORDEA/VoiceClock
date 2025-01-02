package jp.cordea.voiceclock.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.cordea.voiceclock.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(stringResource(R.string.settings_title))
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Item(stringResource(R.string.settings_oss_license)) {
                    context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                }
            }
        }
    }
}

@Composable
private fun Item(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable {
                onClick()
            }
    ) {
        Text(
            modifier = Modifier.padding(
                vertical = 24.dp,
                horizontal = 16.dp
            ),
            style = MaterialTheme.typography.titleMedium,
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            text = title
        )
    }
}
