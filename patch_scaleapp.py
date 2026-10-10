import re

with open("app/src/main/java/duxtami/gramly/app/ui/ScaleApp.kt", "r") as f:
    content = f.read()

imports = """
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
"""

# add imports
content = content.replace("import androidx.compose.material3.Text", "import androidx.compose.material3.Text" + imports)

# add tab row
target = """        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Connected button group as the Rate / Weight switcher."""

replacement = """        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Browser-like tabs for multiple sessions (customers)
            ScrollableTabRow(
                selectedTabIndex = vm.currentSessionIndex,
                edgePadding = 8.dp,
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.Transparent,
                divider = {}
            ) {
                vm.sessions.forEachIndexed { index, session ->
                    Tab(
                        selected = vm.currentSessionIndex == index,
                        onClick = { vm.switchSession(index) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Customer ${index + 1}")
                                if (vm.sessions.size > 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { vm.removeSession(index) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Close Tab", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    )
                }
                if (vm.sessions.size < 10) {
                    Tab(
                        selected = false,
                        onClick = { vm.addSession() },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Customer") }
                    )
                }
            }

            // Connected button group as the Rate / Weight switcher."""

content = content.replace(target, replacement)

with open("app/src/main/java/duxtami/gramly/app/ui/ScaleApp.kt", "w") as f:
    f.write(content)
