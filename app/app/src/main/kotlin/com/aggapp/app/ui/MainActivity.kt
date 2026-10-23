package com.aggapp.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aggapp.app.R
import com.aggapp.app.ui.theme.AggColor
import com.aggapp.app.ui.theme.AggTheme
import com.aggapp.app.update.AppUpdateEffect
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AggTheme {
                AppUpdateEffect()
                val navController = rememberNavController()
                AggScaffold(navController)
            }
        }
    }
}

/**
 * @property route NavHost 路由（阶段三会改为 @Serializable 类型安全路由）
 * @property label 底部可见文案
 * @property labelCd 无障碍描述资源 id
 * @property icon 装饰性图标：语义由 label 承担，故图标 description 保持 null（Material 推荐做法）
 */
private data class TabItem(
    val route: String,
    val label: String,
    val labelCd: Int,
    val icon: @Composable () -> Unit,
)

@Composable
fun AggScaffold(navController: NavHostController) {
    val tabs = listOf(
        TabItem("idcode", stringResource(R.string.tab_idcode), R.string.cd_tab_idcode) {
            Icon(Icons.Filled.Home, null)
        },
        TabItem("express", stringResource(R.string.tab_express), R.string.cd_tab_express) {
            Icon(Icons.Filled.LocalShipping, null)
        },
        TabItem("mine", stringResource(R.string.tab_mine), R.string.cd_tab_mine) {
            Icon(Icons.Filled.Person, null)
        },
    )
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: "express"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AggBottomBar(
                tabs = tabs,
                current = current,
                onSelect = { route ->
                    if (current != route) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) { pad ->
        NavHost(
            navController = navController,
            startDestination = "express",
            modifier = Modifier.padding(pad),
            // 快速淡入淡出，替换默认约 700ms 的转场（默认转场叠加重组会明显卡顿）
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(120)) },
            popEnterTransition = { fadeIn(tween(180)) },
            popExitTransition = { fadeOut(tween(120)) },
        ) {
            composable("idcode") { IdCodeScreen(hiltViewModel<MainViewModel>()) }
            composable("express") { ExpressScreen() }
            composable("mine") { MineScreen() }
        }
    }
}

/**
 * 底部导航（2026-10 重构）：从 Material 的胶囊指示器改为「单据分栏」——
 * 每栏一个高可见度信号色短横 + 墨色图标，选中态不靠色块，而靠这条短横与字重。
 * 平铺、无阴影，与页面上的纸面卡片同属一套语言。
 */
@Composable
private fun AggBottomBar(
    tabs: List<TabItem>,
    current: String,
    onSelect: (String) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            tabs.forEach { tab ->
                val selected = current == tab.route
                val contentColor = if (selected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                val tabCd = stringResource(tab.labelCd)
                Column(
                    Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { onSelect(tab.route) },
                        )
                        .semantics { contentDescription = tabCd }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 选中标记：3dp 信号色短横。未选中时保留同尺寸透明占位，防止切页时图标上下跳动
                    Box(
                        Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(if (selected) AggColor.signal else Color.Transparent),
                    )
                    Spacer(Modifier.height(6.dp))
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        Box(Modifier.size(22.dp)) { tab.icon() }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tab.label,
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}
