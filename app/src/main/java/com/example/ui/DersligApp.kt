package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.DersligScreen
import com.example.viewmodel.DersligViewModel
import com.example.viewmodel.EducationalMaterial
import com.example.viewmodel.generateMaterialsForTopic
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ---------------------------------------------------------------------------------
// FROSTED GLASS THEME CONSTANTS & COMPONENT WRAPPERS
// ---------------------------------------------------------------------------------
val GlassBgWhite60 = Color(0x99FFFFFF)  // 60% White
val GlassBgWhite40 = Color(0x66FFFFFF)  // 40% White
val GlassBgWhite80 = Color(0xCCFFFFFF)  // 80% White
val GlassBorderWhite = Color(0xCCFFFFFF) // Highlight glass reflection
val GlassBorderSoft = Color(0x80FFFFFF)  // Moderate highlight
val GlassGradientBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x193B82F6), // blue-500/10 from HTML
        Color(0x0D8B5CF6), // purple-500/5 from HTML
        Color(0x19F97316)  // orange-500/10 from HTML
    )
)

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    border: BorderStroke = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.8f)),
    containerColor: Color = Color.White.copy(alpha = 0.6f),
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.clickable { onClick() }
            ) {
                content()
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    border: BorderStroke = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.8f)),
    containerColor: Color = Color.White.copy(alpha = 0.6f),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = shape,
        color = containerColor,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DersligApp(
    viewModel: DersligViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val stats by viewModel.userStatsState.collectAsStateWithLifecycle()
    val completedQuizzes by viewModel.completedQuizzesState.collectAsStateWithLifecycle()
    val shopItems by viewModel.shopItemsState.collectAsStateWithLifecycle()

    val currentCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()
    val currentTopic by viewModel.selectedTopic.collectAsStateWithLifecycle()
    val currentMaterial by viewModel.selectedMaterial.collectAsStateWithLifecycle()

    if (stats == null) {
        Box(
            modifier = modifier.fillMaxSize().background(DersligBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = DersligOrange)
        }
        return
    }

    if (!stats!!.onboardingCompleted) {
        OnboardingScreen(
            onSubmit = { name, grade ->
                viewModel.submitOnboarding(name, grade)
            }
        )
        return
    }

    var showGradeDialog by remember { mutableStateOf(false) }

    val activeGrade = stats?.selectedGrade ?: "8. Sınıf (LGS)"

    // Scaffold for dynamic tab bar and custom safe area Edge-To-Edge
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = "Logo",
                                tint = DersligOrange,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "derslig",
                                color = DersligNavy,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        // Sınıf / Grade choosing chip
                        Surface(
                            onClick = { showGradeDialog = true },
                            color = DersligOrangeLight,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, DersligOrange.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeGrade,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DersligOrange
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Sınıf Seç",
                                    tint = DersligOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GlassBgWhite80, // Frosted Glass White background
                    titleContentColor = DersligNavy
                ),
                modifier = Modifier.border(BorderStroke(1.dp, GlassBorderSoft)),
                actions = {
                    // Coin & XP badges
                    Row(
                        modifier = Modifier.padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins Badge
                        Surface(
                            color = Color(0xFFFFF9C4),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { viewModel.navigateTo(DersligScreen.STORE) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = "Altın",
                                    tint = DersligAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stats?.coins ?: 0}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17)
                                )
                            }
                        }

                        // Streak
                        Surface(
                            color = Color(0xFFFFCCBC),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Günlük Serin",
                                    tint = Color(0xFFFF3D00),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${stats?.streak ?: 1} Gün",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD84315)
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen == DersligScreen.HOME ||
                currentScreen == DersligScreen.COURSES ||
                currentScreen == DersligScreen.LEAGUE ||
                currentScreen == DersligScreen.STORE ||
                currentScreen == DersligScreen.PROFILE
            ) {
                NavigationBar(
                    containerColor = GlassBgWhite80,
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(BorderStroke(1.dp, GlassBorderSoft), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val homeSelected = currentScreen == DersligScreen.HOME
                    NavigationBarItem(
                        selected = homeSelected,
                        onClick = { viewModel.navigateTo(DersligScreen.HOME) },
                        icon = { Icon(imageVector = if (homeSelected) Icons.Filled.Home else Icons.Filled.Home, contentDescription = "Ana Sayfa") },
                        label = { Text("Ana Sayfa", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DersligOrange,
                            selectedTextColor = DersligOrange,
                            indicatorColor = DersligOrangeLight,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    val coursesSelected = currentScreen == DersligScreen.COURSES || currentScreen == DersligScreen.COURSE_DETAIL
                    NavigationBarItem(
                        selected = coursesSelected,
                        onClick = { viewModel.navigateTo(DersligScreen.COURSES) },
                        icon = { Icon(imageVector = Icons.Filled.Book, contentDescription = "Dersler") },
                        label = { Text("Dersler", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DersligOrange,
                            selectedTextColor = DersligOrange,
                            indicatorColor = DersligOrangeLight,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("nav_courses")
                    )

                    val leagueSelected = currentScreen == DersligScreen.LEAGUE
                    NavigationBarItem(
                        selected = leagueSelected,
                        onClick = { viewModel.navigateTo(DersligScreen.LEAGUE) },
                        icon = { Icon(imageVector = Icons.Filled.Leaderboard, contentDescription = "Lig Yarışı") },
                        label = { Text("Lig", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DersligOrange,
                            selectedTextColor = DersligOrange,
                            indicatorColor = DersligOrangeLight,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("nav_league")
                    )

                    val storeSelected = currentScreen == DersligScreen.STORE
                    NavigationBarItem(
                        selected = storeSelected,
                        onClick = { viewModel.navigateTo(DersligScreen.STORE) },
                        icon = { Icon(imageVector = Icons.Filled.ShoppingCart, contentDescription = "Mağaza") },
                        label = { Text("Mağaza", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DersligOrange,
                            selectedTextColor = DersligOrange,
                            indicatorColor = DersligOrangeLight,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("nav_store")
                    )

                    val profileSelected = currentScreen == DersligScreen.PROFILE
                    NavigationBarItem(
                        selected = profileSelected,
                        onClick = { viewModel.navigateTo(DersligScreen.PROFILE) },
                        icon = { Icon(imageVector = Icons.Filled.Person, contentDescription = "Profil") },
                        label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DersligOrange,
                            selectedTextColor = DersligOrange,
                            indicatorColor = DersligOrangeLight,
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.LightGray
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DersligBg)
                .background(GlassGradientBrush)
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                DersligScreen.HOME -> HomeScreen(viewModel, stats)
                DersligScreen.COURSES -> CoursesScreen(viewModel)
                DersligScreen.COURSE_DETAIL -> CourseDetailScreen(viewModel, currentCourse, completedQuizzes)
                DersligScreen.TOPIC_HUB -> TopicHubScreen(viewModel, currentTopic)
                DersligScreen.PDF_VIEWER -> PdfViewerScreen(viewModel, currentMaterial)
                DersligScreen.VIDEO_VIEWER -> VideoViewerScreen(viewModel, currentMaterial)
                DersligScreen.LITERATURE_SLIDES -> LectureSlidesScreen(viewModel, currentTopic)
                DersligScreen.ACTIVE_QUIZ -> ActiveQuizScreen(viewModel, currentTopic)
                DersligScreen.QUIZ_RESULT -> QuizResultScreen(viewModel)
                DersligScreen.LEAGUE -> LeagueScreen(viewModel, stats)
                DersligScreen.STORE -> StoreScreen(viewModel, stats, shopItems)
                DersligScreen.PROFILE -> ProfileScreen(viewModel, stats, completedQuizzes)
                DersligScreen.AI_TUTOR -> AiTutorScreen(viewModel)
            }
        }
    }

    // Modern Sınıf Seçme Dialogu (Grade Selector)
    if (showGradeDialog) {
        Dialog(onDismissRequest = { showGradeDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sınıfını Seç",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    val grades = listOf(
                        "5. Sınıf", "6. Sınıf", "7. Sınıf",
                        "8. Sınıf (LGS)", "9. Sınıf", "10. Sınıf",
                        "11. Sınıf", "12. Sınıf (YKS)"
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(grades) { pGrade ->
                            val isSelected = pGrade == activeGrade
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.changeUserGrade(pGrade)
                                        showGradeDialog = false
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) DersligOrangeLight else Color(0xFFF8FAFC),
                                border = if (isSelected) BorderStroke(1.5.dp, DersligOrange) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = if (isSelected) DersligOrange else Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = pGrade,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DersligOrange else DersligNavy
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { showGradeDialog = false }) {
                        Text("Kapat", color = DersligOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// ONBOARDING SCREEN
// ---------------------------------------------------------------------------------
@Composable
fun OnboardingScreen(
    onSubmit: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("5. Sınıf") }
    val grades = listOf(
        "5. Sınıf", "6. Sınıf", "7. Sınıf", "8. Sınıf (LGS)",
        "9. Sınıf", "10. Sınıf", "11. Sınıf", "12. Sınıf (YKS Hazırlık)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassGradientBrush),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = "Hoş Geldin Şampiyon! 🎓",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = DersligNavy,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
            )

            GlassCard(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(vertical = 12.dp),
                containerColor = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.5.dp, Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = DersligOrange,
                        modifier = Modifier.size(52.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Profilini Oluştur",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Derslig eğitim macerana başlamak için adını yaz ve sınıfını seç.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Adın Soyadın") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DersligOrange,
                            focusedLabelColor = DersligOrange,
                            cursorColor = DersligOrange
                        ),
                        placeholder = { Text("Örn: Ahmet Mesrur") },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Sınıf Seçimi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    ) {
                        items(grades.size) { index ->
                            val grade = grades[index]
                            val isSelected = selectedGrade == grade
                            Surface(
                                onClick = { selectedGrade = grade },
                                color = if (isSelected) DersligOrangeLight else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) DersligOrange else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("onboarding_grade_$index")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = grade,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DersligOrange else DersligNavy
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (name.trim().isNotEmpty()) {
                                onSubmit(name.trim(), selectedGrade)
                            }
                        },
                        enabled = name.trim().isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = DersligOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("onboarding_submit_button")
                    ) {
                        Text(
                            text = "Başla ve Öğren! 🚀",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "derslig • Sınıfının Şampiyonu Sensin!",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray.copy(alpha = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

// ---------------------------------------------------------------------------------
// 1. ANA SAYFA / HOME SCREEN
// ---------------------------------------------------------------------------------
@Composable
fun HomeScreen(viewModel: DersligViewModel, stats: UserStats?) {
    val activeGrade = stats?.selectedGrade ?: "8. Sınıf (LGS)"
    val scoreXp = stats?.xp ?: 0
    val leagueName = viewModel.getUserLeague(scoreXp)
    val competitors = viewModel.getLeagueCompetitors(scoreXp)
    val userRank = competitors.find { it.isCurrentUser }?.rank ?: 1

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.3f)), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Hoş Geldin! 👋",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Avatar border effect in name
                            Text(
                                text = stats?.username ?: "Süper Öğrenci",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        // Avatar view
                        UserAvatarView(
                            borderType = stats?.avatarBorder ?: "None",
                            colorHex = "#FF8C00",
                            size = 60
                        )
                    }

                    Divider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Derslig Lig Puanın", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("$scoreXp XP", color = DersligOrange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Aktif Lig Seviyen", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text(leagueName, color = Color(0xFF4FC3F7), fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // Live Action Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Course Study button
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(DersligScreen.COURSES) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Book,
                                contentDescription = null,
                                tint = DersligBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Derslere Çalış", color = DersligNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Özet oku ve test çöz", color = Color.Gray, fontSize = 11.sp)
                    }
                }

                // AI Tutor Study button
                GlassCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(DersligScreen.AI_TUTOR) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2F1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Face,
                                contentDescription = null,
                                tint = Color(0xFF00BFA5),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Yapay Zeka Hoca", color = DersligNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("7/24 Soru Sor & Öğren", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }

        // League Race status card
        item {
            GlassCard(
                onClick = { viewModel.navigateTo(DersligScreen.LEAGUE) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = DersligAmber,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haftalık Lig Sıralaman",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$leagueName - Sıran: #$userRank",
                            color = DersligNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Lige Git",
                        tint = Color.Gray
                    )
                }
            }
        }

        // Quick Lesson Carousel
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popüler Derslerin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    )
                    TextButton(onClick = { viewModel.navigateTo(DersligScreen.COURSES) }) {
                        Text("Tümünü Gör", color = DersligOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Show first 3 courses
                val popularCourses = remember(activeGrade) { CurriculumData.getCoursesForGrade(activeGrade) }
                popularCourses.take(3).forEach { course ->
                    Spacer(modifier = Modifier.height(6.dp))
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { viewModel.selectCourseAndNavigate(course) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(android.graphics.Color.parseColor(course.colorHex)).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconForName(course.iconName),
                                    contentDescription = null,
                                    tint = Color(android.graphics.Color.parseColor(course.colorHex)),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.name, fontWeight = FontWeight.Bold, color = DersligNavy, fontSize = 14.sp)
                                Text("${course.units.size} Ünite • Hazırlık Çalışması", fontSize = 11.sp, color = Color.Gray)
                            }
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 2. DERSLER SCREEN
// ---------------------------------------------------------------------------------
@Composable
fun CoursesScreen(viewModel: DersligViewModel) {
    val stats by viewModel.userStatsState.collectAsStateWithLifecycle()
    val activeGrade = stats?.selectedGrade ?: "8. Sınıf (LGS)"
    val gradeCourses = remember(activeGrade) {
        CurriculumData.getCoursesForGrade(activeGrade)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Eğitim Derslerin",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = DersligNavy,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Sınıfının kurallarına göre düzenlenmiş interaktif müfredat",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(gradeCourses) { course ->
                val courseColor = Color(android.graphics.Color.parseColor(course.colorHex))
                
                GlassCard(
                    onClick = { viewModel.selectCourseAndNavigate(course) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(courseColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForName(course.iconName),
                                contentDescription = null,
                                tint = courseColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DersligNavy
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${course.units.size} Ünite • ${course.units.flatMap { it.topics }.size} Konu Başlığı",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        
                        // Button and indicator
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DersligBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Konuları Gör",
                                tint = DersligNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 3. COURSE DETAY SCREEN
// ---------------------------------------------------------------------------------
@Composable
fun CourseDetailScreen(
    viewModel: DersligViewModel,
    course: AppCourse?,
    completedQuizzes: List<CompletedQuiz>
) {
    if (course == null) {
        viewModel.navigateTo(DersligScreen.COURSES)
        return
    }

    val courseColor = Color(android.graphics.Color.parseColor(course.colorHex))

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // App bar substitute
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(DersligScreen.COURSES) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = DersligNavy)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${course.name} Konu Müfredatı",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DersligNavy
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(course.units) { unit ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unit Header text
                    Text(
                        text = unit.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = DersligNavy,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )

                    // Topics Loop
                    unit.topics.forEach { topic ->
                        val isQuizCompleted = completedQuizzes.any { it.quizId == topic.id }
                        val finishedScoreRecord = completedQuizzes.find { it.quizId == topic.id }
                        
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topic.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = DersligNavy
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "5 Bilgi Özet Kartı • ${topic.questions.size} Soru",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    
                                    // Complete Check indicator
                                    if (isQuizCompleted) {
                                        Surface(
                                            color = Color(0xFFE8F5E9),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Tamamlandı",
                                                    tint = DersligGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "%${finishedScoreRecord?.scorePercent ?: 100}",
                                                    color = DersligGreen,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        }
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

                                // Fast Action: Start study cards
                                Button(
                                    onClick = { viewModel.selectTopicAndGoToHub(topic) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_topic_${topic.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = courseColor),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isQuizCompleted) "Materyalleri Gör (22+ Çalışma)" else "Çalışmaya Başla! (22+ Materyal)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// TOPIC HUB / EDUCATIONAL MATERIALS SELECTION SCREEN (22+ MATERIALS)
// ---------------------------------------------------------------------------------
@Composable
fun TopicHubScreen(viewModel: DersligViewModel, topic: TopicDetail?) {
    if (topic == null) {
        viewModel.navigateTo(DersligScreen.COURSES)
        return
    }

    val stats by viewModel.userStatsState.collectAsStateWithLifecycle()
    val materials by viewModel.topicMaterials.collectAsStateWithLifecycle()
    val isLoadingMaterials by viewModel.isLoadingMaterials.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().background(DersligBg)
    ) {
        // Topic Header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(DersligScreen.COURSE_DETAIL) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = DersligNavy)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = topic.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = DersligNavy
                    )
                    Text(
                        text = "Eğitim Materyalleri ve Çalışma Hub'ı",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Welcome banner of the Hub
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = DersligOrangeLight.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DersligOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Derslig Şampiyonlar Odası 🏆",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DersligNavy
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${topic.title} konusu için 22 adet özel lise/ortaokul düzeyi çalışma materyali hazırlandı. İstediğin materyalden başla!",
                            fontSize = 11.sp,
                            color = DersligNavy.copy(alpha = 0.8f),
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoadingMaterials) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = DersligOrange,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Yapay Zeka Öğretmeni Çalışıyor... 🤖",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DersligNavy,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Seçtiğiniz konu için MEB kazanımlarına %100 uyumlu konu el kitabı, pratik formül kartları, zihin haritası ve yaprak test soru bankasını sizin için üretiyoruz.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Text(
                    text = "Eğitim Materyalleri Listesi (${materials.size} Dosya)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = DersligNavy,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                // Dynamic Scrollable Grid/List of the materials
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(materials) { mat ->
                        val matColor = when (mat.type) {
                            "VIDEO" -> Color(0xFF1976D2)
                            "SLIDES" -> Color(0xFF388E3C)
                            "PDF" -> DersligOrange
                            else -> DersligNavy
                        }

                        GlassCard(
                            onClick = { viewModel.selectMaterial(mat) },
                            modifier = Modifier.fillMaxWidth().testTag("material_item_${mat.id}"),
                            containerColor = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Material Type Indicator icon
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(matColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getMaterialIcon(mat.iconName),
                                        contentDescription = null,
                                        tint = matColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mat.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DersligNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Small type tag chip
                                        Surface(
                                            color = matColor.copy(alpha = 0.08f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = mat.type,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = matColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mat.description,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Aç",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper mapper for Icons
fun getMaterialIcon(iconName: String): ImageVector {
    return when (iconName) {
        "play_circle" -> Icons.Default.PlayArrow
        "slideshow" -> Icons.Default.School
        "picture_as_pdf" -> Icons.Default.MenuBook
        else -> Icons.Default.MenuBook
    }
}

// ---------------------------------------------------------------------------------
// PDF VIEWER SCREEN (Dahili Gelişmiş PDF Okuyucu)
// ---------------------------------------------------------------------------------
@Composable
fun PdfViewerScreen(viewModel: DersligViewModel, material: EducationalMaterial?) {
    if (material == null) {
        viewModel.navigateTo(DersligScreen.TOPIC_HUB)
        return
    }

    var currentPage by remember { mutableStateOf(0) }
    val pageCount = material.contentPages.size.coerceAtLeast(1)
    var zoomScale by remember { mutableStateOf(100) }
    var isNightMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var showDownloadDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isNightMode) Color(0xFF1E293B) else DersligBg)
    ) {
        // App Bar & PDF controls
        Surface(
            color = if (isNightMode) Color(0xFF0F172A) else Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, if (isNightMode) Color(0xFF334155) else Color(0xFFE2E8F0))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(DersligScreen.TOPIC_HUB) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = if (isNightMode) Color.White else DersligNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = material.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) Color.White else DersligNavy,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Derslig Dahili PDF Görüntüleyici",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    // Download action with beautiful animation simulation
                    IconButton(onClick = {
                        showDownloadDialog = true
                        isDownloading = true
                        downloadProgress = 0f
                        scope.launch {
                            for (p in 1..10) {
                                kotlinx.coroutines.delay(120)
                                downloadProgress = p / 10f
                            }
                            isDownloading = false
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "İndir",
                            tint = DersligOrange
                        )
                    }

                    // Night mode toggle inside pdf
                    IconButton(onClick = { isNightMode = !isNightMode }) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Gece Modu",
                            tint = if (isNightMode) Color.White else DersligNavy
                        )
                    }
                }

                // Control panel (Zoom, Pages)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isNightMode) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Zoom
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (zoomScale > 75) zoomScale -= 25 },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Küçült",
                                tint = if (isNightMode) Color.White else DersligNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "%$zoomScale",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) Color.White else DersligNavy,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { if (zoomScale < 175) zoomScale += 25 },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Büyüt",
                                tint = if (isNightMode) Color.White else DersligNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Page Navigation
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (currentPage > 0) currentPage-- },
                            enabled = currentPage > 0,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Önceki Sayfa",
                                tint = if (currentPage > 0) (if (isNightMode) Color.White else DersligNavy) else Color.LightGray
                            )
                        }
                        Text(
                            text = "SAYFA ${currentPage + 1} / $pageCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isNightMode) Color.White else DersligNavy,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { if (currentPage < pageCount - 1) currentPage++ },
                            enabled = currentPage < pageCount - 1,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Sonraki Sayfa",
                                tint = if (currentPage < pageCount - 1) (if (isNightMode) Color.White else DersligNavy) else Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        // Simulating the actual download response dialog with progress bar
        if (showDownloadDialog) {
            Dialog(onDismissRequest = { if (!isDownloading) showDownloadDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isNightMode) Color(0xFF1E293B) else Color.White,
                    modifier = Modifier.padding(16.dp),
                    border = BorderStroke(1.dp, if (isNightMode) Color(0xFF475569) else Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                progress = downloadProgress,
                                color = DersligOrange,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "PDF İndiriliyor... %${(downloadProgress * 100).toInt()}",
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) Color.White else DersligNavy,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Çevrim dışı mod için eğitim materyali cihazına güvenli biçimde indiriliyor.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DersligGreen, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "PDF Başarıyla İndirildi! 📥",
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) Color.White else DersligNavy,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PDF dosyası cihazının İndirilenler (Downloads) klasörüne kaydedildi.\nArtık internetin olmasa bile Derslig kalitesiyle çalışabilirsin!",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showDownloadDialog = false },
                                colors = ButtonDefaults.buttonColors(containerColor = DersligGreen)
                            ) {
                                Text("Harika, Kapat!", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // PDF Main Content Paper Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val scaleFactor = zoomScale / 100f
            // Interactive simulated white sheet representing realistic paper PDF document
            Surface(
                color = if (isNightMode) Color(0xFF0F172A) else Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (isNightMode) Color(0xFF475569) else Color(0xFFCBD5E1)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f / 1.414f) // standard A4 page layout aspect ratio
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((18 * scaleFactor).dp)
                ) {
                    // Header tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "derslig şampiyonluk materyali",
                            fontSize = (9 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) DersligOrangeLight else DersligOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "KOD-PDF-${material.id.substringAfterLast("_").uppercase()}",
                            fontSize = (8 * scaleFactor).sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height((12 * scaleFactor).dp))
                    Divider(color = if (isNightMode) Color(0xFF334155) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height((16 * scaleFactor).dp))

                    // PDF Content Text (rendered depending on pages list!)
                    val textContent = material.contentPages.getOrNull(currentPage)
                        ?: "SAYFA İÇERİĞİ MEVCUT DEĞİL"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        Text(
                            text = textContent,
                            fontSize = (13 * scaleFactor).sp,
                            color = if (isNightMode) Color.White.copy(alpha = 0.9f) else DersligNavy,
                            lineHeight = (20 * scaleFactor).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = if (isNightMode) Color(0xFF334155) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer page info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Okul Başarısı Derslig ile Cepte!",
                            fontSize = (8 * scaleFactor).sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Sayfa ${currentPage + 1}",
                            fontSize = (9 * scaleFactor).sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNightMode) Color.White else DersligNavy
                        )
                    }
                }
            }
        }

        // Quick page thumbnail strip at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isNightMode) Color(0xFF0F172A) else Color.White)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hızlı Sayfa Atla:  ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isNightMode) Color.White else DersligNavy
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pageCount) { index ->
                    val isPageSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isPageSelected) DersligOrange else (if (isNightMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                            )
                            .border(
                                BorderStroke(1.dp, if (isPageSelected) DersligOrange else Color(0xFFCBD5E1)),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { currentPage = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPageSelected) Color.White else (if (isNightMode) Color.White else DersligNavy)
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// VIDEO VIEWER SCREEN (Vimeo İnline Oynatıcı)
// ---------------------------------------------------------------------------------
@Composable
fun VideoViewerScreen(viewModel: DersligViewModel, material: EducationalMaterial?) {
    if (material == null) {
        viewModel.navigateTo(DersligScreen.TOPIC_HUB)
        return
    }

    var isVideoDone by remember { mutableStateOf(false) }
    var rewardEarned by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().background(DersligBg)
    ) {
        // App bar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(DersligScreen.TOPIC_HUB) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = DersligNavy)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = material.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    )
                    Text(
                        text = "İnteraktif Video Anlatımı",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Vimeo embed responsive player container using WebView
                Card(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, DersligOrange.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                }
                                loadUrl(material.videoUrl)
                            }
                        },
                        update = { webView ->
                            webView.loadUrl(material.videoUrl)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(DersligOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Derslig Eğitmeninden Notlar 👨‍🏫",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DersligNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Sevgili şampiyon, bu video anlatımında konunun can alıcı noktalarını, sınavlarda en çok çıkan soru tiplerini ve hızlı ezber taktiklerini anlatıyorum.\n\nVideoyu hiç bölmeden, pür dikkat dinle ve gerekirse önemli yerleri defterine not et! Bitirdiğinde aşağıdaki butona tıklayarak +75 XP Akıncısı ödülünü almayı unutma!",
                            fontSize = 12.sp,
                            color = DersligNavy.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Video Completion state simulation
                        if (!rewardEarned) {
                            Button(
                                onClick = {
                                    isVideoDone = true
                                    rewardEarned = true
                                    // Give rewards
                                    coroutineScope.launch {
                                        viewModel.repository.completeQuiz(
                                            quizId = material.id,
                                            correctCount = 1,
                                            wrongCount = 0,
                                            scorePercent = 100,
                                            xpEarned = 75,
                                            coinsEarned = 15
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DersligOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Videoyu Bitirdim & Ödülümü Al! 🏆",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DersligGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tebrikler! +75 XP ve +15 Altın Hesabına Eklendi 🎉",
                                        color = DersligGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Diğer Şampiyonların Yorumları 💬",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DersligNavy,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(3) { index ->
                val commentUser = when (index) {
                    0 -> "Ayşenur Demir" to "Bu video sayesinde eksiğimi 5 dakikada kapattım, anlatım mükemmel! 💎"
                    1 -> "Emirhan Yıldırım" to "Hemen sonrasındaki yaprak testi de full çektim şaka mı Derslig farkı! 🔥"
                    else -> "Buse Soylu" to "Derslerde en yüksek notu ben alacağım, lige bomba gibi dönüyorum!"
                }
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DersligOrangeLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = commentUser.first.take(1),
                                fontWeight = FontWeight.Bold,
                                color = DersligOrange,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = commentUser.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DersligNavy
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = commentUser.second,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 4. LECTURE SLIDES / STUDY CARDS SCREEN (Ders Kartları)
// ---------------------------------------------------------------------------------
@Composable
fun LectureSlidesScreen(viewModel: DersligViewModel, topic: TopicDetail?) {
    if (topic == null) {
        viewModel.navigateTo(DersligScreen.COURSES)
        return
    }

    val currentSlideIndex by viewModel.currentSlideIndex.collectAsStateWithLifecycle()
    val slides = remember(topic) { viewModel.getSlidesForSelectedTopic() }
    val totalSlides = slides.size

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Safe navigation header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(DersligScreen.COURSE_DETAIL) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = DersligNavy)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Konu Özet Kartları",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    )
                }

                // Progress Indicator
                Text(
                    text = "Sayfa ${currentSlideIndex + 1} / $totalSlides",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        // Active layout representing a high-quality study slide
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Lecture cards progress bar
            LinearProgressIndicator(
                progress = { (currentSlideIndex + 1).toFloat() / totalSlides.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                color = DersligOrange,
                trackColor = Color(0xFFE2E8F0)
            )

            // Inner study card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, DersligOrange.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Star decoration
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = DersligOrange,
                        modifier = Modifier
                            .size(44.dp)
                            .padding(bottom = 12.dp)
                    )

                    Text(
                        text = topic.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = DersligOrange,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = slides.getOrNull(currentSlideIndex) ?: "",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DersligNavy,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }

            // Slide commands row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Back button
                OutlinedButton(
                    onClick = { viewModel.prevSlide() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                    enabled = currentSlideIndex > 0
                ) {
                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Geri")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Geri")
                }

                // Next or Test button
                Button(
                    onClick = { viewModel.nextSlide() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lecture_next_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DersligOrange)
                ) {
                    val isLast = currentSlideIndex == totalSlides - 1
                    Text(text = if (isLast) "Pratik Yap!" else "İlerle")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isLast) Icons.Default.EmojiEvents else Icons.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 5. ACTIVE QUIZ SCREEN (Canlı Test Çözümü)
// ---------------------------------------------------------------------------------
@Composable
fun ActiveQuizScreen(viewModel: DersligViewModel, topic: TopicDetail?) {
    if (topic == null) {
        viewModel.navigateTo(DersligScreen.COURSES)
        return
    }

    val currentQIdx by viewModel.quizQuestionIndex.collectAsStateWithLifecycle()
    val totalQuestions = topic.questions.size
    val activeQuestion = topic.questions.getOrNull(currentQIdx) ?: return

    val selectedAnswer by viewModel.selectedAnswerIndex.collectAsStateWithLifecycle()
    val isChecked by viewModel.isQuestionChecked.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back toolbar
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateTo(DersligScreen.COURSE_DETAIL) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "İptal", tint = DersligNavy)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Konu Pekiştirme Testi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    )
                }

                Text(
                    text = "Soru ${currentQIdx + 1} / $totalQuestions",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live question card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "SORU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = DersligOrange,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeQuestion.questionText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DersligNavy,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Options layout
            items(activeQuestion.options.size) { index ->
                val optionText = activeQuestion.options[index]
                val isSelected = selectedAnswer == index
                
                // Color formatting depending on check status
                val isCorrectIndex = index == activeQuestion.correctAnswerIndex
                val isWrongIndexSelected = isSelected && !isCorrectIndex

                val optionBgColor = when {
                    isChecked && isCorrectIndex -> Color(0xFFE8F5E9)      // Right answer highlight: Green background
                    isChecked && isWrongIndexSelected -> Color(0xFFFFEBEE) // Selected wrong: Red background
                    isSelected -> DersligOrangeLight                       // Active un-checked select: Light orange
                    else -> Color.White
                }

                val optionBorderColor = when {
                    isChecked && isCorrectIndex -> DersligGreen
                    isChecked && isWrongIndexSelected -> Color.Red
                    isSelected -> DersligOrange
                    else -> Color(0xFFE2E8F0)
                }

                val optionTextColor = when {
                    isChecked && isCorrectIndex -> DersligGreen
                    isChecked && isWrongIndexSelected -> Color.Red
                    isSelected -> DersligOrange
                    else -> DersligNavy
                }

                val optionSign = ('A' + index).toString()

                Surface(
                    onClick = { viewModel.selectAnswer(index) },
                    shape = RoundedCornerShape(14.dp),
                    color = optionBgColor,
                    border = BorderStroke(if (isSelected || (isChecked && isCorrectIndex)) 2.dp else 1.dp, optionBorderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(14.dp))
                        .testTag("quiz_option_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Letter badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DersligOrange else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = optionSign,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = optionText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = optionTextColor,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isChecked && isCorrectIndex) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Doğru", tint = DersligGreen)
                        } else if (isChecked && isWrongIndexSelected) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Yanlış", tint = Color.Red)
                        }
                    }
                }
            }

            // Answer evaluation / explanation box
            if (isChecked) {
                item {
                    val wasCorrect = selectedAnswer == activeQuestion.correctAnswerIndex
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (wasCorrect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (wasCorrect) Icons.Filled.Stars else Icons.Filled.School,
                                    contentDescription = null,
                                    tint = if (wasCorrect) DersligGreen else DersligOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (wasCorrect) "Harika Çözüm! 👏 (+50 XP)" else "Soru Çözümü Açıklaması (+10 XP)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (wasCorrect) DersligGreen else DersligNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = activeQuestion.explanation,
                                fontSize = 12.sp,
                                color = DersligNavy,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Fixed submit / skip bottom bar
        Surface(
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                if (!isChecked) {
                    Button(
                        onClick = { viewModel.checkAnswer() },
                        enabled = selectedAnswer != -1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DersligOrange)
                    ) {
                        Text("Cevabı Gönder", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextQuestion() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_next_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DersligNavy)
                    ) {
                        val isLast = currentQIdx == totalQuestions - 1
                        Text(
                            text = if (isLast) "Test Sonuçlarını Gör" else "Sıradaki Soruya Geç",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 6. QUIZ RESULT SCREEN (Test Sonuç Ekranı)
// ---------------------------------------------------------------------------------
@Composable
fun QuizResultScreen(viewModel: DersligViewModel) {
    val results by viewModel.lastQuizResult.collectAsStateWithLifecycle()
    val correct = results?.first ?: 0
    val wrong = results?.second ?: 0
    val total = correct + wrong

    val xpEarned = (correct * 50) + (wrong * 10)
    val coinsEarned = correct * 10

    val successRate = if (total > 0) (correct.toFloat() / total * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large Victory Chest
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF3E0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = DersligAmber,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Tebrikler Şampiyon! 🎉",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = DersligNavy
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "Konu Çalışmasını Başarıyla Bitirdin!",
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // Stats grid
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TEST SONUÇ ÖZETİ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Doğru", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "$correct", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DersligGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Yanlış", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "$wrong", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Başarı", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "%$successRate", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DersligBlue)
                    }
                }

                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 16.dp))

                // Rewards Earned layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xFFFFECE0), RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stars, contentDescription = "XP", tint = DersligOrange, modifier = Modifier.size(18.dp))
                        Text("+$xpEarned XP", fontWeight = FontWeight.Black, color = DersligOrange, fontSize = 15.sp)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xFFFFF9C4), RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Stars, contentDescription = "Altın", tint = DersligAmber, modifier = Modifier.size(18.dp))
                        Text("+$coinsEarned Altın", fontWeight = FontWeight.Black, color = Color(0xFFF57F17), fontSize = 15.sp)
                    }
                }
            }
        }

        // CTA buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(DersligScreen.HOME) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("result_home_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DersligOrange)
            ) {
                Text("Ders Lig Zirvesini Gör", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.navigateTo(DersligScreen.COURSES) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, Color.LightGray)
            ) {
                Text("Diğer Konulara Geç", color = DersligNavy, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 7. LİG SCREEN (Gamified Leaderboard / Weekly Ligi)
// ---------------------------------------------------------------------------------
@Composable
fun LeagueScreen(viewModel: DersligViewModel, stats: UserStats?) {
    val scoreXp = stats?.xp ?: 0
    val leagueName = viewModel.getUserLeague(scoreXp)
    val leagueColorHex = viewModel.getLeagueColor(leagueName)
    val leagueColor = Color(android.graphics.Color.parseColor(leagueColorHex))

    val competitors = viewModel.getLeagueCompetitors(scoreXp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Lig information banner card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = leagueColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "AKTİF LİG GRUBUN",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = leagueName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Haftalık ligin bitmesine 3 gün 4 saat kaldı.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Leaderboard header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Haftalık Liderlik Sıralaması",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DersligNavy
            )
            Text(
                text = "${competitors.size} Katılımcı",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }

        // Leaderboard list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(competitors) { comp ->
                val isSelf = comp.isCurrentUser
                
                GlassSurface(
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (isSelf) DersligOrangeLight.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.55f),
                    border = if (isSelf) BorderStroke(1.5.dp, DersligOrange) else BorderStroke(1.2.dp, Color.White.copy(alpha = 0.8f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Indicator / Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when (comp.rank) {
                                        1 -> Color(0xFFFFF9C4) // Gold
                                        2 -> Color(0xFFECEFF1) // Silver
                                        3 -> Color(0xFFFFE0B2) // Bronze
                                        else -> Color.Transparent
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (comp.rank <= 3) {
                                val tintColor = when (comp.rank) {
                                    1 -> DersligAmber
                                    2 -> Color.Gray
                                    else -> Color(0xFFD84315)
                                }
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = tintColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${comp.rank}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Avatar
                        UserAvatarView(
                            borderType = comp.avatarBorder,
                            colorHex = comp.avatarColorHex,
                            size = 40
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Name & School
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = comp.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelf) DersligOrange else DersligNavy
                                )
                                if (isSelf) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = DersligOrange,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "SEN",
                                            color = Color.White,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = comp.school,
                                fontSize = 10.sp,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Points
                        Text(
                            text = "${comp.xp} XP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelf) DersligOrange else DersligNavy
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 8. MAĞAZA SCREEN (Derslig Store)
// ---------------------------------------------------------------------------------
@Composable
fun StoreScreen(viewModel: DersligViewModel, stats: UserStats?, items: List<ShopItem>) {
    var showPurchasedDialog by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Balance card header
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DersligNavy),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "DERSLİG ALTIN CÜZDANIN",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Altın",
                        tint = DersligAmber,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${stats?.coins ?: 0} Derslig Altını",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Text(
            text = "Eğitim Market Ürünleri",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = DersligNavy,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                GlassCard(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Icon representer
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (item.category == "BORDER") Color(0xFFFFF3E0) else Color(0xFFE3F2FD)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (item.category == "BORDER") Icons.Default.WorkspacePremium else Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = if (item.category == "BORDER") DersligOrange else DersligBlue,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Title
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DersligNavy,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Description
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 13.sp,
                            modifier = Modifier.padding(vertical = 4.dp).height(26.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        val hasEnufCoins = (stats?.coins ?: 0) >= item.cost

                        // Action buy button
                        if (item.isPurchased) {
                            Button(
                                onClick = {
                                    if (item.category == "BORDER") {
                                        viewModel.changeProfileBorder(item.title)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DersligGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (item.category == "BORDER") "Kuşan" else "Sahip Olundu",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.buyItem(item)
                                    showPurchasedDialog = item.title
                                },
                                enabled = hasEnufCoins,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasEnufCoins) DersligOrange else Color.LightGray
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("buy_item_${item.itemId}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Stars, contentDescription = null, modifier = Modifier.size(10.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${item.cost} Altın", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Purchase completion notice Dialog
    showPurchasedDialog?.let { title ->
        Dialog(onDismissRequest = { showPurchasedDialog = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DersligGreen, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tebrikler!", fontWeight = FontWeight.Black, fontSize = 20.sp, color = DersligNavy)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "'$title' başarıyla satın alındı ve profil envanterine eklendi!",
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { showPurchasedDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = DersligOrange)
                    ) {
                        Text("Kapat")
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 9. PROFIL & ÖZELLEŞTİRME SCREEN
// ---------------------------------------------------------------------------------
@Composable
fun ProfileScreen(
    viewModel: DersligViewModel,
    stats: UserStats?,
    completedQuizzes: List<CompletedQuiz>
) {
    val focusManager = LocalFocusManager.current
    var inputName by remember { mutableStateOf(stats?.username ?: "Süper Öğrenci") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Center Profile Avatar
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                UserAvatarView(
                    borderType = stats?.avatarBorder ?: "None",
                    colorHex = "#2196F3",
                    size = 90
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Editable Name input
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.changeUsername(inputName)
                        focusManager.clearFocus()
                    }),
                    modifier = Modifier
                        .widthIn(max = 240.dp)
                        .testTag("username_input"),
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = DersligNavy
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DersligOrange,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    placeholder = { Text("Lütfen adını yazın") }
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${stats?.selectedGrade ?: "8. Sınıf"} Öğrencisi",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Stats boxes row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassSurface(
                    modifier = Modifier.weight(1.0f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Toplam XP", fontSize = 11.sp, color = Color.Gray)
                        Text("${stats?.xp ?: 0}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DersligOrange)
                    }
                }

                GlassSurface(
                    modifier = Modifier.weight(1.0f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Tamamlanan Test", fontSize = 11.sp, color = Color.Gray)
                        Text("${completedQuizzes.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DersligBlue)
                    }
                }
            }
        }

        // Achievements Collection List
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Derslig Başarı Kupaların 🏆",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = DersligNavy,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Achievement cards
                val isTopicCompleted = completedQuizzes.isNotEmpty()
                val isBorderEquipped = stats?.avatarBorder != "None" && stats?.avatarBorder?.isEmpty() == false
                val isHighscoreEarned = completedQuizzes.any { it.scorePercent >= 80 }

                AchievementBadgeRow("İlk Adım", "Derslig'de ilk özetini bitirdin", isTopicCompleted)
                Spacer(modifier = Modifier.height(8.dp))
                AchievementBadgeRow("Kusursuz Çözcü", "Bir testten 80% ve üstü skor aldın", isHighscoreEarned)
                Spacer(modifier = Modifier.height(8.dp))
                AchievementBadgeRow("Asil Şampiyon", "Marketten özel bir profil çerçevesi kuşandın", isBorderEquipped)
            }
        }
    }
}

@Composable
fun AchievementBadgeRow(title: String, desc: String, isUnlocked: Boolean) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isUnlocked) Color.White else Color(0xFFE2E8F0).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (isUnlocked) Color(0xFFE2E8F0) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFFFFECE0) else Color.LightGray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (isUnlocked) DersligOrange else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) DersligNavy else Color.Gray
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = if (isUnlocked) Color.Gray else Color.LightGray
                )
            }
            
            if (isUnlocked) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Açıldı",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = DersligGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(text = "Kilitli", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// 10. YAPAY ZEKA DERSLİG HOCASI / TUTOR CHAT
// ---------------------------------------------------------------------------------
@Composable
fun AiTutorScreen(viewModel: DersligViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    var textInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Safe navigation header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(DersligScreen.HOME) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = DersligNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Derslig Yapay Zeka Hocası 🤖",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DersligNavy
                    )
                    Text(
                        text = "Saniyeler içinde soru çözümü ve konu anlatımı",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // Chat logs
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item {
                // Info Banner
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                ) {
                    Text(
                        text = "Sorunun metnini buraya yapıştırıp gönderebilirsin. Örn: 'EBOB(12, 18) nasıl çözülür?' Matematik ve tüm dersleri saniyeler içinde açıklar!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00796B),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            items(chatMessages) { msg ->
                val isTeacher = msg.sender == "TEACHER"
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isTeacher) Arrangement.Start else Arrangement.End
                ) {
                    if (isTeacher) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🏫", fontSize = 16.sp)
                        }
                    }
                    
                    Surface(
                        color = if (isTeacher) Color.White else DersligOrange,
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = if (isTeacher) 16.dp else 2.dp,
                            bottomStart = if (isTeacher) 2.dp else 16.dp
                        ),
                        border = if (isTeacher) BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                        modifier = Modifier.widthIn(max = 260.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isTeacher) DersligNavy else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👨‍🏫", fontSize = 16.sp)
                        }
                        
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = DersligOrange)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Öğretmeniniz düşünüyor...", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        // Quick query Suggestion block
        Surface(color = Color.White) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                val questions = listOf("EBOB Nedir?", "Mevsimler Nasıl Oluşur?", "Accepting ne demek?")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    questions.forEach { question ->
                        Surface(
                            onClick = { viewModel.sendQuestionToTutor(question) },
                            color = Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = question,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DersligNavy,
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Input Box Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tutor_input_field"),
                        placeholder = { Text("Sorunu buraya yaz...", fontSize = 12.sp) },
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DersligOrange,
                            unfocusedBorderColor = Color.LightGray
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            viewModel.sendQuestionToTutor(textInput)
                            textInput = ""
                            focusManager.clearFocus()
                        })
                    )
                    
                    IconButton(
                        onClick = {
                            viewModel.sendQuestionToTutor(textInput)
                            textInput = ""
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DersligOrange)
                            .testTag("tutor_send_button")
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Gönder", tint = Color.White)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------
// COMPOSABLE COMPONENT HELPERS
// ---------------------------------------------------------------------------------
@Composable
fun UserAvatarView(
    borderType: String,
    colorHex: String,
    size: Int
) {
    val ringColor = when (borderType) {
        "Altın Çerçeve" -> DersligAmber
        "Ejderha Elması" -> Color(0xFF7E57C2)
        "Alevli Lig Çerçevesi" -> Color(0xFFFF3D00)
        else -> Color.Transparent
    }

    val boxModifier = if (ringColor != Color.Transparent) {
        Modifier
            .size(size.dp)
            .border(3.dp, ringColor, CircleShape)
            .padding(4.dp)
    } else {
        Modifier.size(size.dp)
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(colorHex))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎓",
                fontSize = (size * 0.45).sp,
                textAlign = TextAlign.Center
            )
        }
        
        if (borderType == "Alevli Lig Çerçevesi") {
            // A small fire emblem decoration on side for cosmetic richness
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Red)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

fun getIconForName(iconName: String): ImageVector {
    return when (iconName) {
        "calculate" -> Icons.Default.Calculate
        "science" -> Icons.Default.Science
        "translate" -> Icons.Default.Translate
        "history" -> Icons.Default.History
        else -> Icons.Default.School
    }
}
