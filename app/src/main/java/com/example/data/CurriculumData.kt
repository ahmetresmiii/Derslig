package com.example.data

data class AppCourse(
    val id: String,
    val name: String,
    val iconName: String, // "calculate", "science", "translate", "history", "school"
    val colorHex: String,
    val units: List<LearningUnit>
)

data class LearningUnit(
    val id: String,
    val title: String,
    val topics: List<TopicDetail>
)

data class TopicDetail(
    val id: String,
    val title: String,
    val summaryCards: List<String>, // Slide cards containing educational summaries
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

object CurriculumData {

    // Global courses fallback list for backward compatibility
    val courses: List<AppCourse> by lazy {
        getCoursesForGrade("8. Sınıf (LGS)")
    }

    fun getCourseById(id: String): AppCourse? {
        // Search across all grade courses
        for (g in 5..12) {
            val list = getCoursesForGrade(if (g == 8) "8. Sınıf (LGS)" else if (g == 12) "12. Sınıf (YKS)" else "$g. Sınıf")
            val found = list.find { it.id == id || it.id.endsWith(id) }
            if (found != null) return found
        }
        return courses.firstOrNull()
    }

    fun getTopicById(topicId: String): Pair<AppCourse, TopicDetail>? {
        for (g in 5..12) {
            val gradeName = when (g) {
                8 -> "8. Sınıf (LGS)"
                12 -> "12. Sınıf (YKS)"
                else -> "$g. Sınıf"
            }
            val coursesList = getCoursesForGrade(gradeName)
            for (course in coursesList) {
                for (unit in course.units) {
                    for (topic in unit.topics) {
                        if (topic.id == topicId) {
                            return Pair(course, topic)
                        }
                    }
                }
            }
        }
        return null
    }

    fun normalizeGrade(raw: String): String {
        val lower = raw.lowercase()
        return when {
            lower.contains("12") || lower.contains("yks") -> "12. Sınıf (YKS Hazırlık)"
            lower.contains("11") -> "11. Sınıf"
            lower.contains("10") -> "10. Sınıf"
            lower.contains("9") -> "9. Sınıf"
            lower.contains("8") || lower.contains("lgs") -> "8. Sınıf (LGS)"
            lower.contains("7") -> "7. Sınıf"
            lower.contains("6") -> "6. Sınıf"
            else -> "5. Sınıf"
        }
    }

    fun getCoursesForGrade(grade: String): List<AppCourse> {
        val norm = normalizeGrade(grade)
        return when (norm) {
            "5. Sınıf" -> buildGrade5()
            "6. Sınıf" -> buildGrade6()
            "7. Sınıf" -> buildGrade7()
            "8. Sınıf (LGS)" -> buildGrade8()
            "9. Sınıf" -> buildGrade9()
            "10. Sınıf" -> buildGrade10()
            "11. Sınıf" -> buildGrade11()
            "12. Sınıf (YKS Hazırlık)" -> buildGrade12()
            else -> buildGrade5()
        }
    }

    // Programmatic generator to build rich TopicDetail objects dynamically with Turkish names
    private fun generateTopic(
        id: String,
        title: String,
        courseName: String,
        unitTitle: String
    ): TopicDetail {
        val slide1 = "📖 **$title** konusunun kapsamlı özetine hoş geldin şampiyon!\n\n" +
                "• Bu ünitede bu konunun tüm akademik detaylarını öğreneceğiz.\n" +
                "• **$courseName** dersi kapsamında yer alan bu konu, milli eğitim müfredatı ile %100 uyumludur.\n" +
                "• Düzenli tekrar, konu anlatımı özet kartları ve pekiştirme testleriyle tam öğrenme gerçekleştirebilirsin."

        val slide2 = "🚨 **Önemli Kural ve Formüller:**\n\n" +
                "• Konuya ait temel kavramları bilmek soruları %80 daha hızlı çözmeni sağlar.\n" +
                "• **$title** ile ilgili çalışırken mutlaka kağıt kalem kullanıp formülleri not et.\n" +
                "• Karşılaştığın her soru tipi LGS/YKS sınavlarında senin için büyük avantaj sağlayacaktır.\n\n" +
                "💡 *Örnek uygulama:* Konunun ana formüllerini odandaki çalışma masasına asabilirsin!"

        val slide3 = "🎯 **Özet & Başarı İpuçları:**\n\n" +
                "• **$title** konusunu bitirdikten sonra hemen pekiştirme testini çöz.\n" +
                "• Anlamadığın yerleri Derslig Yapay Zeka Öğretmenine (AI Tutor) sorabilirsin!\n" +
                "• Gümüş, Altın liglerde yükselmek için her gün en az 3 pekiştirme testi tamamlamayı unutma!"

        // Create 2 comprehensive multiple-choice questions
        val q1 = QuizQuestion(
            questionText = "$title konusuyla ilgili olarak aşağıdakilerden hangisi en doğru maddedir?",
            options = listOf(
                "Konuyu pekiştirmek için test çözmek gereksizdir.",
                "Bu konu MEB müfredatında yer alan ve sınavlarda çıkan mühim bir konudur.",
                "Sadece ezber yaparak en zor soruları çözebiliriz.",
                "Sadece son gün çalışarak tüm sınavlarda birinci olunur."
            ),
            correctAnswerIndex = 1,
            explanation = "Bu konu MEB müfredatı ile tamamen uyumludur ve pekiştirme testleriyle öğrenilmelidir."
        )

        val q2 = QuizQuestion(
            questionText = "Yukarıda öğrendiğimiz $title konusu kapsamında hedeflenen asıl kazanım nedir?",
            options = listOf(
                "Konunun temel mantığını kavrayarak yeni nesil soruları doğru analiz etmek",
                "Formülleri deftere hiç yazmadan ezberlemek",
                "Sadece dersi izleyip hiç soru çözmemek",
                "Testleri rastgele işaretleyerek bitirmek"
            ),
            correctAnswerIndex = 0,
            explanation = "Zihinde kalıcı olması için konunun temel mantığı kavranmalı ve yeni nesil soru çözümü yapılmalıdır."
        )

        return TopicDetail(
            id = id,
            title = title,
            summaryCards = listOf(slide1, slide2, slide3),
            questions = listOf(q1, q2)
        )
    }

    // ===================================
    // Grade 5 Curriculum
    // ===================================
    private fun buildGrade5(): List<AppCourse> {
        val turkceTopics = listOf(
            "Okuma, Yazma, Konuşma ve Dinleme",
            "Sözcükte Anlam (Gerçek, Yan, Mecaz, Terim)",
            "Cümlede Anlam (Neden-Sonuç, Amaç-Sonuç, Öznel-Nesnel)",
            "Parçada Anlam (Ana fikir, Yardımcı fikir)",
            "Ses Bilgisi (Ünsüz Benzeşmesi, Yumuşama, Ünlü Düşmesi)",
            "Yazım Kuralları ve Noktalama İşaretleri",
            "Metin Türleri (Hikaye, Fabl, Masal)"
        )
        val matTopics = listOf(
            "Doğal Sayılar ve İşlemler (Milyonlar)",
            "Kesirler, Ondalık Gösterim ve Yüzdeler",
            "Geometri (Temel Kavramlar, Üçgen ve Dörtgenler)",
            "Veri İşleme (Sütun Grafiği)",
            "Uzunluk, Zaman ve Alan Ölçme"
        )
        val fenTopics = listOf(
            "Güneş, Dünya ve Ay",
            "Canlılar Dünyası",
            "Kuvvetin Ölçülmesi ve Sürtünme",
            "Madde ve Değişim (Hal değişimi, Isı-Sıcaklık)",
            "Işığın Yayılması",
            "İnsan ve Çevre",
            "Elektrik Devre Elemanları"
        )
        val sosyalTopics = listOf(
            "Birey ve Toplum (Hak ve Sorumluluklar)",
            "Kültür ve Miras (Anadolu Uygarlıkları)",
            "İnsanlar, Yerler ve Çevreler (Harita Bilgisi, İklim)",
            "Bilim, Teknoloji ve Toplum",
            "Üretim, Dağıtım ve Tüketim",
            "Etkin Vatandaşlık",
            "Küresel Bağlantılar"
        )

        return listOf(
            AppCourse("5_tur", "Türkçe", "translate", "#1E88E5", listOf(LearningUnit("5_tur_u1", "Edebi Beceriler", turkceTopics.mapIndexed { idx, t -> generateTopic("5_tur_t_$idx", t, "Türkçe", "Edebi Beceriler") }))),
            AppCourse("5_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("5_mat_u1", "Sayılar ve Uzay", matTopics.mapIndexed { idx, t -> generateTopic("5_mat_t_$idx", t, "Matematik", "Sayılar ve Uzay") }))),
            AppCourse("5_fen", "Fen Bilimleri", "science", "#2E7D32", listOf(LearningUnit("5_fen_u1", "Gözlem ve Doğa", fenTopics.mapIndexed { idx, t -> generateTopic("5_fen_t_$idx", t, "Fen Bilimleri", "Gözlem ve Doğa") }))),
            AppCourse("5_sos", "Sosyal Bilgiler", "history", "#D84315", listOf(LearningUnit("5_sos_u1", "Yurttaşlık ve Tarih", sosyalTopics.mapIndexed { idx, t -> generateTopic("5_sos_t_$idx", t, "Sosyal Bilgiler", "Yurttaşlık ve Tarih") })))
        )
    }

    // ===================================
    // Grade 6 Curriculum
    // ===================================
    private fun buildGrade6(): List<AppCourse> {
        val turkceTopics = listOf(
            "Sözcükte Anlam (Eş/Zıt Anlam, Kök-Ek)",
            "Cümlede Anlam (Örtülü Anlam, Geçiş ve Bağlantı İfadeleri)",
            "Parçada Anlam",
            "İsimler, Sıfatlar, Zamirler",
            "Edat, Bağlaç, Ünlem",
            "Yazım ve Noktalama"
        )
        val matTopics = listOf(
            "Doğal Sayılarla İşlemler (Üslü Sayılar, İşlem Önceliği)",
            "Çarpanlar ve Katlar",
            "Tam Sayılar",
            "Kesirlerle İşlemler",
            "Oran",
            "Ondalık Gösterim",
            "Veri Analizi",
            "Açılar, Alan Ölçme, Çember, Geometrik Cisimler (Hacim)"
        )
        val fenTopics = listOf(
            "Güneş Sistemi ve Tutulmalar",
            "Vücudumuzdaki Sistemler (Destek ve Hareket, Sindirim, Dolaşım, Solunum, Boşaltım)",
            "Bileşke Kuvvet ve Sabit Süratli Hareket",
            "Maddenin Tanecikli Yapısı, Yoğunluk, Yakıtlar",
            "Ses ve Özellikleri",
            "Vücudumuzdaki Sistemler ve Sağlığı (Denetleyici ve Düzenleyici Sistemler)",
            "İletken ve Yalıtkan Maddeler"
        )
        val sosyalTopics = listOf(
            "Biz ve Değerlerimiz",
            "Tarihe Yolculuk (Orta Asya Türk Devletleri, İslamiyetin Doğuşu)",
            "Yeryüzünde Yaşam (Kıtalar, Okyanuslar, Türkiye’nin Konumu)",
            "Bilim ve Teknoloji Hayatımızda",
            "Üretiyorum, Tüketiyorum, Ekonomiye Katılıyorum",
            "Yönetime Katılıyorum",
            "Ülkeler Arası Köprüler"
        )

        return listOf(
            AppCourse("6_tur", "Türkçe", "translate", "#1E88E5", listOf(LearningUnit("6_tur_u1", "Sözcük ve Cümle", turkceTopics.mapIndexed { idx, t -> generateTopic("6_tur_t_$idx", t, "Türkçe", "Sözcük ve Cümle") }))),
            AppCourse("6_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("6_mat_u1", "İşlemler ve Oran", matTopics.mapIndexed { idx, t -> generateTopic("6_mat_t_$idx", t, "Matematik", "İşlemler ve Oran") }))),
            AppCourse("6_fen", "Fen Bilimleri", "science", "#2E7D32", listOf(LearningUnit("6_fen_u1", "Sistemler ve Uzay", fenTopics.mapIndexed { idx, t -> generateTopic("6_fen_t_$idx", t, "Fen Bilimleri", "Sistemler ve Uzay") }))),
            AppCourse("6_sos", "Sosyal Bilgiler", "history", "#D84315", listOf(LearningUnit("6_sos_u1", "Eski Tarih ve İnsan", sosyalTopics.mapIndexed { idx, t -> generateTopic("6_sos_t_$idx", t, "Sosyal Bilgiler", "Eski Tarih ve İnsan") })))
        )
    }

    // ===================================
    // Grade 7 Curriculum
    // ===================================
    private fun buildGrade7(): List<AppCourse> {
        val turkceTopics = listOf(
            "Sözcükte, Cümlede ve Parçada Anlam",
            "Fiiller (Haber/Dilek Kipleri, Anlam Kayması)",
            "Fiillerde Yapı (Basit, Türemiş, Birleşik)",
            "Zarflar (Belirteçler)",
            "Anlatım Bozuklukları",
            "Yazım ve Noktalama",
            "Söz Sanatları ve Metin Türleri"
        )
        val matTopics = listOf(
            "Tam Sayılarla İşlemler",
            "Rasyonel Sayılar ve İşlemler",
            "Eşitlik ve Denklem (Birinci Dereceden Bir Bilinmeyenli)",
            "Oran ve Orantı",
            "Yüzdeler",
            "Doğrular ve Açılar",
            "Çokgenler, Çember ve Daire",
            "Veri Analizi"
        )
        val fenTopics = listOf(
            "Güneş Sistemi ve Ötesi (Gök Cisimleri, Uzay Araştırmaları)",
            "Hücre ve Bölünmeler (Mitoz, Mayoz)",
            "Kuvvet ve Enerji (İş, Kinetik/Potansiyel Enerji)",
            "Saf Madde ve Karışımlar (Atomun Yapısı)",
            "Işığın Madde ile Etkileşimi (Aynalar, Mercekler)",
            "Canlılarda Üreme, Büyüme ve Gelişme",
            "Elektrik Devreleri (Seri ve Paralel Bağlama)"
        )
        val sosyalTopics = listOf(
            "Birey ve Toplum (İletişim)",
            "Türk Tarihinde Yolculuk (Osmanlı Devleti Kuruluş/Yükselme)",
            "Ülkemizde Nüfus",
            "Zaman İçinde Bilim",
            "Ekonomi ve Sosyal Hayat",
            "Yaşayan Demokrasi",
            "Küresel Bağlantılar"
        )

        return listOf(
            AppCourse("7_tur", "Türkçe", "translate", "#1E88E5", listOf(LearningUnit("7_tur_u1", "Anlam Bilgisi", turkceTopics.mapIndexed { idx, t -> generateTopic("7_tur_t_$idx", t, "Türkçe", "Anlam Bilgisi") }))),
            AppCourse("7_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("7_mat_u1", "Denklemler ve Orantı", matTopics.mapIndexed { idx, t -> generateTopic("7_mat_t_$idx", t, "Matematik", "Denklemler ve Orantı") }))),
            AppCourse("7_fen", "Fen Bilimleri", "science", "#2E7D32", listOf(LearningUnit("7_fen_u1", "Hücre ve Enerji", fenTopics.mapIndexed { idx, t -> generateTopic("7_fen_t_$idx", t, "Fen Bilimleri", "Hücre ve Enerji") }))),
            AppCourse("7_sos", "Sosyal Bilgiler", "history", "#D84315", listOf(LearningUnit("7_sos_u1", "Osmanlı ve Vatandaşlık", sosyalTopics.mapIndexed { idx, t -> generateTopic("7_sos_t_$idx", t, "Sosyal Bilgiler", "Osmanlı ve Vatandaşlık") })))
        )
    }

    // ===================================
    // Grade 8 Curriculum (LGS)
    // ===================================
    private fun buildGrade8(): List<AppCourse> {
        val turkceTopics = listOf(
            "Fiilimsiler (Eylemsiler)",
            "Cümlenin Ögeleri",
            "Sözcükte, Cümlede, Parçada Anlam",
            "Cümle Türleri",
            "Fiilde Çatı",
            "Anlatım Bozuklukları",
            "Yazım ve Noktalama",
            "Metin Türleri ve Söz Sanatları"
        )
        val matTopics = listOf(
            "Çarpanlar ve Katlar (EBOB-EKOK)",
            "Üslü İfadeler",
            "Kareköklü İfadeler",
            "Veri Analizi",
            "Olasılık",
            "Cebirsel İfadeler ve Özdeşlikler",
            "Doğrusal Denklemler ve Eşitsizlikler",
            "Üçgenler ve Dönüşüm Geometrisi",
            "Geometrik Cisimler (Silindir, Koni, Piramit)"
        )
        val fenTopics = listOf(
            "Mevsimler ve İklim",
            "DNA ve Genetik Kod",
            "Basınç (Katı, Sıvı, Gaz)",
            "Madde ve Endüstri (Periyodik Sistem, Kimyasal Tepkimeler, Asit-Baz)",
            "Basit Makineler",
            "Enerji Dönüşümleri ve Çevre Bilimi",
            "Elektrik Yükleri ve Elektrik Enerjisi"
        )
        val inkilapTopics = listOf(
            "Bir Kahraman Doğuyor (Atatürk’ün Hayatı)",
            "Milli Uyanış (I. Dünya Savaşı, Kurtuluş Savaşı Hazırlık)",
            "Ya İstiklal Ya Ölüm (Cepheler)",
            "Atatürkçülük ve Çağdaşlaşan Türkiye (İnkılaplar)",
            "Demokratikleşme Çabaları",
            "Atatürk Dönemi Türk Dış Politikası",
            "Atatürk'ün Ölümü ve Sonrası"
        )

        return listOf(
            AppCourse("8_tur", "Türkçe", "translate", "#1E88E5", listOf(LearningUnit("8_tur_u1", "LGS Türkçe Hazırlık", turkceTopics.mapIndexed { idx, t -> generateTopic("8_tur_t_$idx", t, "Türkçe", "LGS Türkçe Hazırlık") }))),
            AppCourse("8_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("8_mat_u1", "LGS Matematik Hazırlık", matTopics.mapIndexed { idx, t -> generateTopic("8_mat_t_$idx", t, "Matematik", "LGS Matematik Hazırlık") }))),
            AppCourse("8_fen", "Fen Bilimleri", "science", "#2E7D32", listOf(LearningUnit("8_fen_u1", "LGS Fen Bilimleri", fenTopics.mapIndexed { idx, t -> generateTopic("8_fen_t_$idx", t, "Fen Bilimleri", "LGS Fen Bilimleri") }))),
            AppCourse("8_ink", "T.C. İnkılap Tarihi", "history", "#D84315", listOf(LearningUnit("8_ink_u1", "LGS İnkılap Tarihi", inkilapTopics.mapIndexed { idx, t -> generateTopic("8_ink_t_$idx", t, "T.C. İnkılap Tarihi", "LGS İnkılap Tarihi") })))
        )
    }

    // ===================================
    // Grade 9 Curriculum
    // ===================================
    private fun buildGrade9(): List<AppCourse> {
        val edebiyatTopics = listOf(
            "Edebiyatın Tanımı ve Güzel Sanatlar",
            "Hikaye",
            "Şiir (Manzume, Şiir İnceleme)",
            "Masal ve Fabl",
            "Roman",
            "Tiyatro",
            "Biyografi, Otobiyografi, Mektup, Günlük"
        )
        val matTopics = listOf(
            "Mantık",
            "Kümeler",
            "Sayı Kümeleri (Bölünebilme, EBOB-EKOK)",
            "Denklem ve Eşitsizlikler",
            "Üslü ve Köklü İfadeler",
            "Oran ve Orantı",
            "Denklemler ve Eşitsizliklerle İlgili Uygulamalar (Problemler)",
            "Üçgenler (Eşlik, Benzerlik, Trigonometri Başlangıç, Alan)",
            "Veri Analizi"
        )
        val fizikTopics = listOf(
            "Fizik Bilimine Giriş",
            "Madde ve Özellikleri (Özkütle, Dayanıklılık, Yapışma-Tutma)",
            "Hareket ve Kuvvet",
            "Enerji (İş, Güç, Enerji Dönüşümü)",
            "Isı ve Sıcaklık",
            "Elektrostatik"
        )
        val kimyaTopics = listOf(
            "Kimya Bilimi",
            "Atom ve Periyodik Sistem",
            "Kimyasal Türler Arası Etkileşimler",
            "Maddenin Halleri",
            "Doğa ve Kimya"
        )
        val biyolojiTopics = listOf(
            "Yaşam Bilimi Biyoloji (Canlıların Ortak Özellikleri, Temel Bileşenler)",
            "Hücre",
            "Canlılar Dünyası (Sınıflandırma)"
        )

        return listOf(
            AppCourse("9_ede", "Edebiyat", "translate", "#1565C0", listOf(LearningUnit("9_ede_u1", "Edebiyat Dünyası", edebiyatTopics.mapIndexed { idx, t -> generateTopic("9_ede_t_$idx", t, "Edebiyat", "Edebiyat Dünyası") }))),
            AppCourse("9_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("9_mat_u1", "Cebir ve Mantık", matTopics.mapIndexed { idx, t -> generateTopic("9_mat_t_$idx", t, "Matematik", "Cebir ve Mantık") }))),
            AppCourse("9_fiz", "Fizik", "science", "#2E7D32", listOf(LearningUnit("9_fiz_u1", "Fiziğin Esasları", fizikTopics.mapIndexed { idx, t -> generateTopic("9_fiz_t_$idx", t, "Fizik", "Fiziğin Esasları") }))),
            AppCourse("9_kim", "Kimya", "school", "#00ACC1", listOf(LearningUnit("9_kim_u1", "Atom ve Doğa", kimyaTopics.mapIndexed { idx, t -> generateTopic("9_kim_t_$idx", t, "Kimya", "Atom ve Doğa") }))),
            AppCourse("9_biy", "Biyoloji", "forest", "#43A047", listOf(LearningUnit("9_biy_u1", "Canlılar ve Hücre", biyolojiTopics.mapIndexed { idx, t -> generateTopic("9_biy_t_$idx", t, "Biyoloji", "Canlılar ve Hücre") })))
        )
    }

    // ===================================
    // Grade 10 Curriculum
    // ===================================
    private fun buildGrade10(): List<AppCourse> {
        val edebiyatTopics = listOf(
            "Türk Edebiyatının Dönemleri",
            "Hikaye (Dede Korkut, Mesnevi, Halk Hikayesi)",
            "Şiir (İslamiyet Öncesi, Geçiş Dönemi, Halk ve Divan Şiiri)",
            "Destan ve Efsane",
            "Roman (Servet-i Fünun, Tanzimat)",
            "Tiyatro (Geleneksel Türk Tiyatrosu)",
            "Anı ve Haber Metni"
        )
        val matTopics = listOf(
            "Sayma ve Olasılık (Permütasyon, Kombinasyon, Binom)",
            "Fonksiyonlar",
            "Polinomlar",
            "İkinci Dereceden Denklemler",
            "Çokgenler ve Dörtgenler (Özel Dörtgenler)",
            "Uzay Geometri (Katı Cisimler)"
        )
        val fizikTopics = listOf(
            "Elektrik ve Manyetizma",
            "Basınç ve Kaldırma Kuvveti",
            "Dalgalar (Yay, Su, Ses, Deprem)",
            "Optik (Aydınlanma, Yansıma, Kırılma, Mercekler)"
        )
        val kimyaTopics = listOf(
            "Kimyanın Temel Kanunları ve Kimyasal Hesaplamalar",
            "Karışımlar",
            "Asitler, Bazlar ve Tuzlar",
            "Kimya Her Yerde"
        )
        val biyolojiTopics = listOf(
            "Hücre Bölünmeleri (Mitoz, Mayoz)",
            "Kalıtımın Temel İlkeleri",
            "Ekosistem Ekolojisi ve Güncel Çevre Sorunları"
        )

        return listOf(
            AppCourse("10_ede", "Edebiyat", "translate", "#1565C0", listOf(LearningUnit("10_ede_u1", "Tarihi Metinler", edebiyatTopics.mapIndexed { idx, t -> generateTopic("10_ede_t_$idx", t, "Edebiyat", "Tarihi Metinler") }))),
            AppCourse("10_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("10_mat_u1", "Olasılık ve Cebir", matTopics.mapIndexed { idx, t -> generateTopic("10_mat_t_$idx", t, "Matematik", "Olasılık ve Cebir") }))),
            AppCourse("10_fiz", "Fizik", "science", "#2E7D32", listOf(LearningUnit("10_fiz_u1", "Optik ve Dalga", fizikTopics.mapIndexed { idx, t -> generateTopic("10_fiz_t_$idx", t, "Fizik", "Optik ve Dalga") }))),
            AppCourse("10_kim", "Kimya", "school", "#00ACC1", listOf(LearningUnit("10_kim_u1", "Hesaplamalar", kimyaTopics.mapIndexed { idx, t -> generateTopic("10_kim_t_$idx", t, "Kimya", "Hesaplamalar") }))),
            AppCourse("10_biy", "Biyoloji", "forest", "#43A047", listOf(LearningUnit("10_biy_u1", "Kalıtım ve Çevre", biyolojiTopics.mapIndexed { idx, t -> generateTopic("10_biy_t_$idx", t, "Biyoloji", "Kalıtım ve Çevre") })))
        )
    }

    // ===================================
    // Grade 11 Curriculum
    // ===================================
    private fun buildGrade11(): List<AppCourse> {
        val edebiyatTopics = listOf(
            "Edebiyat ve Toplum İlişkisi",
            "Hikaye (Cumhuriyet Dönemi 1923-1960)",
            "Şiir (Tanzimat’tan Cumhuriyet’e Şiir)",
            "Makale, Sohbet, Fıkra",
            "Roman (Cumhuriyet Dönemi 1923-1950/1950-1980)",
            "Tiyatro",
            "Eleştiri ve Mülakat"
        )
        val matTopics = listOf(
            "Yönlü Açılar ve Trigonometri",
            "Analitik Geometri",
            "Fonksiyonlarda Uygulamalar",
            "Denklem ve Eşitsizlik Sistemleri",
            "Çember ve Daire",
            "Uzay Geometri (Katı Cisimler)",
            "Olasılık (Koşullu Olasılık)"
        )
        val fizikTopics = listOf(
            "Vektörler",
            "Bağıl Hareket",
            "Newton’ın Hareket Yasaları",
            "Bir ve İki Boyutta Sabit İvmeli Hareket",
            "Enerji ve Hareket",
            "İtme ve Çizgisel Momentum",
            "Tork ve Denge (Basit Makineler)",
            "Elektriksel Kuvvet ve Potansiyel",
            "Manyetizma ve Elektromanyetik İndüklenme"
        )
        val kimyaTopics = listOf(
            "Modern Atom Teorisi",
            "Gazlar",
            "Sıvı Çözeltiler ve Çözünürlük",
            "Kimyasal Tepkimelerde Enerji",
            "Kimyasal Tepkimelerde Hız ve Denge"
        )
        val biyolojiTopics = listOf(
            "İnsan Fizyolojisi (Denetleyici Sistemler, Duyular, Destek, Sindirim, Dolaşım, Bağışıklık, Solunum, Boşaltım, Üreme)",
            "Komünite ve Popülasyon Ekolojisi"
        )

        return listOf(
            AppCourse("11_ede", "Edebiyat", "translate", "#1565C0", listOf(LearningUnit("11_ede_u1", "Cumhuriyet Dönemi", edebiyatTopics.mapIndexed { idx, t -> generateTopic("11_ede_t_$idx", t, "Edebiyat", "Cumhuriyet Dönemi") }))),
            AppCourse("11_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("11_mat_u1", "Trigonometri", matTopics.mapIndexed { idx, t -> generateTopic("11_mat_t_$idx", t, "Matematik", "Trigonometri") }))),
            AppCourse("11_fiz", "Fizik", "science", "#2E7D32", listOf(LearningUnit("11_fiz_u1", "Kuvvet ve Alanlar", fizikTopics.mapIndexed { idx, t -> generateTopic("11_fiz_t_$idx", t, "Fizik", "Kuvvet ve Alanlar") }))),
            AppCourse("11_kim", "Kimya", "school", "#00ACC1", listOf(LearningUnit("11_kim_u1", "Tepkimeler ve Gaz", kimyaTopics.mapIndexed { idx, t -> generateTopic("11_kim_t_$idx", t, "Kimya", "Tepkimeler ve Gaz") }))),
            AppCourse("11_biy", "Biyoloji", "forest", "#43A047", listOf(LearningUnit("11_biy_u1", "Fizyoloji ve Canlı", biyolojiTopics.mapIndexed { idx, t -> generateTopic("11_biy_t_$idx", t, "Biyoloji", "Fizyoloji ve Canlı") })))
        )
    }

    // ===================================
    // Grade 12 Curriculum (YKS)
    // ===================================
    private fun buildGrade12(): List<AppCourse> {
        val edebiyatTopics = listOf(
            "Edebiyat ve Felsefe / Psikoloji",
            "Hikaye (Cumhuriyet Dönemi 1960 Sonrası)",
            "Şiir (Cumhuriyet Dönemi Saf/Toplumcu/Garip/İkinci Yeni vb.)",
            "Roman (1980 Sonrası Türk Romanı / Dünya Romanı)",
            "Tiyatro (Cumhuriyet Dönemi)",
            "Deneme, Söylev"
        )
        val matTopics = listOf(
            "Logaritmik Fonksiyonlar ve Diziler",
            "Trigonometri (Toplam-Fark, Yarım Açı)",
            "Türev (Limit ve Süreklilik, Türev Alma, Uygulamalar)",
            "İntegral (Belirsiz ve Belirli İntegral, Alan Hesabı)",
            "Analitik Geometri (Çemberin Analitiği)"
        )
        val fizikTopics = listOf(
            "Çembersel ve Harmonik Hareket",
            "Dalga Mekaniği (Kırınım, Girişim, Doppler)",
            "Atom Fiziğine Giriş ve Radyoaktivite",
            "Modern Fizik (Özel Görelilik, Fotoelektrik, Compton)",
            "Modern Fiziğin Teknolojideki Uygulamaları"
        )
        val kimyaTopics = listOf(
            "Kimya ve Elektrik (Redoks, Piller, Elektroliz)",
            "Karbon Kimyasına Giriş",
            "Organik Bileşikler (Hidrokarbonlar, Fonksiyonel Gruplar)",
            "Energy Kaynakları"
        )
        val biyolojiTopics = listOf(
            "Genden Proteine (Protein Sentezi)",
            "Canlılarda Enerji Dönüşümleri (Respirasyon)",
            "Bitki Biyolojisi",
            "Canlılar ve Çevre"
        )

        return listOf(
            AppCourse("12_ede", "Edebiyat", "translate", "#1565C0", listOf(LearningUnit("12_ede_u1", "YKS Edebiyat Kampı", edebiyatTopics.mapIndexed { idx, t -> generateTopic("12_ede_t_$idx", t, "Edebiyat", "YKS Edebiyat Kampı") }))),
            AppCourse("12_mat", "Matematik", "calculate", "#FF3D00", listOf(LearningUnit("12_mat_u1", "YKS Mat Analiz", matTopics.mapIndexed { idx, t -> generateTopic("12_mat_t_$idx", t, "Matematik", "YKS Mat Analiz") }))),
            AppCourse("12_fiz", "Fizik", "science", "#2E7D32", listOf(LearningUnit("12_fiz_u1", "Modern Fizik", fizikTopics.mapIndexed { idx, t -> generateTopic("12_fiz_t_$idx", t, "Fizik", "Modern Fizik") }))),
            AppCourse("12_kim", "Kimya", "school", "#00ACC1", listOf(LearningUnit("12_kim_u1", "Organik Kimya", kimyaTopics.mapIndexed { idx, t -> generateTopic("12_kim_t_$idx", t, "Kimya", "Organik Kimya") }))),
            AppCourse("12_biy", "Biyoloji", "forest", "#43A047", listOf(LearningUnit("12_biy_u1", "Fotosentez ve Gen", biyolojiTopics.mapIndexed { idx, t -> generateTopic("12_biy_t_$idx", t, "Biyoloji", "Fotosentez ve Gen") })))
        )
    }
}
