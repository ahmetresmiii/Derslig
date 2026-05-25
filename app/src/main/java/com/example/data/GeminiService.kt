package com.example.data

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.viewmodel.AcademicContent

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") key: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    /**
     * Ask a question to the AI tutor coach. Supports local fallback if API key is invalid.
     */
    suspend fun askTutor(userQuestion: String, courseName: String?, gradeName: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Safety check if the API key is not configured or is placeholder
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return getLocalCoachResponse(userQuestion, courseName)
        }

        val courseContext = if (courseName != null) " ile ilgili olan $courseName dersi" else " eğitim"
        val systemInstructionText = """
            Sen Derslig platformunun resmi yapay zeka öğretmen asistanı "Derslig Yapay Zeka Hocası"sın.
            Karşındaki kişi bir K-12 öğrencisidir ve seninle $gradeName seviyesinde$courseContext hakkında konuşuyor.
            
            Kurallar:
            1. Yanıtlarını her zaman Türkçe, son derece samimi, teşvik edici, tatlı ve pedagojik olarak ver.
            2. Karmaşık kavramları ortaokul/ilkokul seviyesine indirgeyerek, benzetmeler kullanarak anlat.
            3. Yanıtında asla kaba kelimeler kullanma. Konuyu sevdirmeye çalış.
            4. Eğer öğrenci soru sorduysa, soruyu adım adım açıklayarak çöz ve sonunda öğrenciye küçük bir pekiştirme sorusu sor!
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = userQuestion)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
        )

        return try {
            val response = api.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Yapay zeka asistanımdan boş yanıt aldım. Lütfen daha sonra tekrar deneyebilir misin?"
        } catch (e: Exception) {
            // Log & fallback
            "Yapay zeka öğretmenine bağlanırken bir hata oluştu: ${e.localizedMessage}\n\nİşte sana yerel öğretmen yardımım:\n\n${getLocalCoachResponse(userQuestion, courseName)}"
        }
    }

    suspend fun generateAcademicContent(title: String, courseName: String, gradeName: String): AcademicContent? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return null
        }

        val prompt = """
            Milli Eğitim Bakanlığı (MEB) müfredatı ile %100 uyumlu olacak şekilde, $gradeName seviyesindeki $courseName dersinin "$title" konusu için ayrıntılı, zengin ve kaliteli eğitim materyalleri hazırla. Bu eğitim materyallerini aşağıda belirtilen JSON formatında TÜRKÇE olarak döndür:
            
            {
              "intro": "Detaylı ve zengin konu anlatımı. En az 3 uzun paragraf içermeli. Kavramlar, tanımlar, günlük hayat örnekleri ve kazanım hedeflerini içermeli.",
              "formulaSheet": "💡 FORMÜLLER VE KRİTİK KURALLAR MATRİSİ:\n\nKonunun tüm formülleri, alt kuralları ve ders notları detaylıca listelenmiş olmalı.",
              "questionBank": "📝 OKUL YAZILISI VE YAPRAK TEST SORULARI:\n\nKonuya ait 3 adet örnek çoktan seçmeli yazılı/LGS/YKS tarzı soru. Cevap şıkları A, B, C, D olarak mutlaka yer almalı.",
              "solutionKey": "🔑 DETAYLI SORU ÇÖZÜMLERİ VE ANALİZİ:\n\nSoru bankasındaki her 3 sorunun adım adım, pedagojik, açıklayıcı çözümleri.",
              "studyTactics": "🚀 SINAVLARDA BAŞARI SAĞLAYACAK ALTIN TAKTİKLER VE STRATEJİLER:\n\nTaktik 1, Taktik 2, Taktik 3 başlıkları halinde pratik ipuçları.",
              "zihinHaritasi": "🧠 ÖĞRENMEYİ KOLAYLAŞTIRICI ZİHİN HARİTASI:\n\nZihin haritası ağaç yapısı ve ana kavram ilişkileri.",
              "evdeDeney": "🧩 EVDE YAPILABİLECEK EĞLENCELİ DENEY/DRAMA/ETKİNLİK PLANI:\n\nÖğrencinin konuyu evde canlandırarak pekiştirmesi için deneyin adı, malzemeleri ve adım adım uygulanışı."
            }
            
            Önemli kurallar:
            1. Yanıtında sadece ve sadece geçerli bir JSON objesi döndür, kod blogu işaretleri (```json vb.) haricinde fazladan açıklama metni yazma.
            2. JSON syntax'ı kusursuz olmalı. Çift tırnaklar doğru kaçırılmalı (escaped).
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            )
        )

        return try {
            val response = api.generateContent(apiKey, request)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawText != null) {
                parseAcademicContentJson(rawText, title, courseName)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseAcademicContentJson(rawText: String, title: String, course: String): AcademicContent {
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring("```json".length)
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring("```".length)
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length - "```".length)
        }
        cleaned = cleaned.trim()

        // Fast path: try parsing using Moshi
        try {
            val jsonAdapter = moshi.adapter(Map::class.java)
            val map = jsonAdapter.fromJson(cleaned) as? Map<*, *>
            if (map != null) {
                return AcademicContent(
                    intro = (map["intro"] as? String) ?: "Kazanım ders anlatımı: $course $title konusu.",
                    formulaSheet = (map["formulaSheet"] as? String) ?: "💡 FORMÜLLER:\n\nKonuya dair önemli formüller ve kurallar broşürü.",
                    questionBank = (map["questionBank"] as? String) ?: "📝 YAPRAK TEST SORULARI:\n\n[Soru 1] Soru yazılıyor...",
                    solutionKey = (map["solutionKey"] as? String) ?: "🔑 ÇÖZÜM ANAHTARI:\n\nDetaylı çözümler yakında.",
                    studyTactics = (map["studyTactics"] as? String) ?: "🚀 TAKTİKLER:\n\nHızlı soru çözme taktikleri.",
                    zihinHaritasi = (map["zihinHaritasi"] as? String) ?: "🧠 ZİHİN HARİTASI:\n\nZihin haritası sarmal dizilimi.",
                    evdeDeney = (map["evdeDeney"] as? String) ?: "🧩 EVDE ETKİNLİK:\n\nPratik canlandırma etkinliği."
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Resilient fallback path: Custom Tag / Substring parser
        val intro = extractField(cleaned, "intro") ?: "Kazanım ders anlatımı: $course $title konusu."
        val formulaSheet = extractField(cleaned, "formulaSheet") ?: "💡 FORMÜLLER:\n\nKonuya dair önemli formüller ve kurallar broşürü."
        val questionBank = extractField(cleaned, "questionBank") ?: "📝 YAPRAK TEST SORULARI:\n\n[Soru 1] Soru yazılıyor..."
        val solutionKey = extractField(cleaned, "solutionKey") ?: "🔑 ÇÖZÜM ANAHTARI:\n\nDetaylı çözümler yakında."
        val studyTactics = extractField(cleaned, "studyTactics") ?: "🚀 TAKTİKLER:\n\nHızlı soru çözme taktikleri."
        val zihinHaritasi = extractField(cleaned, "zihinHaritasi") ?: "🧠 ZİHİN HARİTASI:\n\nZihin haritası sarmal dizilimi."
        val evdeDeney = extractField(cleaned, "evdeDeney") ?: "🧩 EVDE ETKİNLİK:\n\nPratik canlandırma etkinliği."

        return AcademicContent(intro, formulaSheet, questionBank, solutionKey, studyTactics, zihinHaritasi, evdeDeney)
    }

    private fun extractField(jsonStr: String, fieldName: String): String? {
        val search = "\"$fieldName\""
        val index = jsonStr.indexOf(search)
        if (index == -1) return null
        val startIndex = jsonStr.indexOf(":", index)
        if (startIndex == -1) return null
        val startQuote = jsonStr.indexOf("\"", startIndex)
        if (startQuote == -1) return null
        var endQuote = -1
        var i = startQuote + 1
        while (i < jsonStr.length) {
            if (jsonStr[i] == '\"' && jsonStr[i - 1] != '\\') {
                endQuote = i
                break
            }
            i++
        }
        if (endQuote == -1) return null
        val rawVal = jsonStr.substring(startQuote + 1, endQuote)
        return rawVal
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }

    private fun getLocalCoachResponse(question: String, courseName: String?): String {
        val normalizedQuestion = question.lowercase()
        val defaultTutorPitch = "\n\n💡 *Not: Yapay zekanın canlı cevaplar üretebilmesi için lütfen 'Secrets' panelinden kendi GEMINI_API_KEY değerini gir!*"
        
        return when {
            normalizedQuestion.contains("ebob") || normalizedQuestion.contains("en büyük ortak bölen") -> {
                "Merhaba Şampiyon! 👋 EBOB konusunu mu merak ettin? EBOB, 'En Büyük Ortak Bölen' demektir. Örneğin 12 ve 18 sayılarını düşünelim: " +
                        "Her ikisini de tam bölen en büyük sayı 6'dır! Sorularda eğer büyük bir bütünü eşit parçalara bölüyorsak her zaman EBOB kullanırız. " +
                        "Örn: Bidonlardaki yağları küçük şişelere eşit doldurmak gibi. Anlaması oldukça kolay, değil mi? 🚀 $defaultTutorPitch"
            }
            normalizedQuestion.contains("ekok") || normalizedQuestion.contains("en küçük ortak kat") -> {
                "Harika bir soru! 🌟 EKOK, yani 'En Küçük Ortak Kat', iki sayının birleştiği en küçük ortak noktadır. " +
                        "Örneğin 4 günde bir ve 6 günde bir nöbet tutan iki hemşireyi düşünelim; günleri sayarsak en yakın ortak günleri 12'dir. " +
                        "Yani küçük parçalardan büyük bir bütün elde ediyorsak EKOK kullanırız! $defaultTutorPitch"
            }
            normalizedQuestion.contains("mevsim") || normalizedQuestion.contains("iklim") -> {
                "Selam! 🌍 Mevsimlerin oluşumu en heyecan verici konulardan biridir! Bunun 2 temel sebebi var: " +
                        "Dünya'nın Güneş etrafında dolanması ve 23° 27'lik eksen eğikliği. Eğik açıyla ışık alan yerlerde kış, dik açıyla alan yerlerde yaz mevsimi başlar! " +
                        "Merak ettiğin başka bir detay var mı? $defaultTutorPitch"
            }
            normalizedQuestion.contains("merhaba") || normalizedQuestion.contains("selam") -> {
                "Merhaba genç şampiyon! ben senin Derslig Yapay Zeka Öğretmeninim. Derslerin, ödevlerin veya zorlandığın sorular hakkında bana dilediğini sorabilirsin. Sana severek yardımcı olacağım! 🎓✨ $defaultTutorPitch"
            }
            else -> {
                "Harika bir soru! 🎓 Derslig Yapay Zeka Öğretmenin olarak, öğrenme azmini çok takdir ediyorum. " +
                        "Bu konuyu daha derinlemesine öğrenmek için lütfen Ders Kartlarımızı incele ve eğlenceli testlerimizi çözmeyi unutma! " +
                        "Derslig Ligi'nde puanları toplayıp zirveye çıkman için her zaman yanındayım! 💪 $defaultTutorPitch"
            }
        }
    }
}
