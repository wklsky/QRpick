package com.aggapp.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aggapp.app.R
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
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val tabCd = stringResource(tab.labelCd)
                    NavigationBarItem(
                        selected = current == tab.route,
                        onClick = {
                            if (current != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = tab.icon,
                        label = { Text(tab.label) },
                        // 图标为纯装饰，统一由 contentDescription 播报「XX页」，
                        // 比直接读可见 label 更能让 TalkBack 用户理解这是导航入口
                        modifier = Modifier.semantics { contentDescription = tabCd },
                    )
                }
            }
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
