package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

// Screen Routes
enum class DersligScreen {
    HOME,
    COURSES,
    LEAGUE,
    STORE,
    PROFILE,
    COURSE_DETAIL,
    TOPIC_HUB,           // Added for selecting educational materials
    PDF_VIEWER,          // Added for viewing PDFs directly inside the app
    VIDEO_VIEWER,        // Added for viewing educational vimeo videos in app
    LITERATURE_SLIDES,
    ACTIVE_QUIZ,
    QUIZ_RESULT,
    AI_TUTOR
}

data class EducationalMaterial(
    val id: String,
    val title: String,
    val type: String, // "PDF", "VIDEO", "SLIDES", "INTERACTIVE"
    val description: String,
    val iconName: String, // "picture_as_pdf", "play_circle", "slideshow"
    val contentPages: List<String> = emptyList(), // Simulated PDF pages
    val videoUrl: String = "" // Embedded Vimeo URL or video page
)

data class AcademicContent(
    val intro: String,
    val formulaSheet: String,
    val questionBank: String,
    val solutionKey: String,
    val studyTactics: String,
    val zihinHaritasi: String,
    val evdeDeney: String
)

fun getAcademicContent(title: String, course: String): AcademicContent {
    val lower = title.lowercase()
    
    // 1. TAM SAYILAR
    if (lower.contains("tam sayı") || lower.contains("negatif") || lower.contains("pozitif") || lower.contains("mutlak değer")) {
        return AcademicContent(
            intro = "Kazanım: Tam sayılar kümesini tanır, sayı doğrusunda gösterir ve mutlak değer kavramını açıklar.\n\n" +
                    "Tam sayılar kümesi Z={..., -3, -2, -1, 0, 1, 2, 3, ...} şeklinde gösterilir. Pozitif tam sayılar (z+), negatif tam sayılar (z-) ve referans noktası olan sıfırdan (0) oluşur. Sıfır ne pozitif ne de negatiftir (nötrdür).\n\n" +
                    "Mutlak Değer: Bir tam sayının başlangıç noktasına (sıfıra) olan uzaklığıdır. Mesafe negatif olamayacağı için mutlak değer dışına her sayı pozitif veya sıfır olarak çıkar. Gösterimi: |x| dir. Örn: |-7| = 7, |+5| = 5, |0| = 0.",
            
            formulaSheet = "💡 TAM SAYILARDA İŞLEMLER ALTIN FORMÜLLERİ:\n\n" +
                    "1. Toplama İşlemi:\n" +
                    "• Aynı işaretli ise sayılar toplanır, ortak işaret verilir: (-5) + (-3) = -8\n" +
                    "• Ters işaretli ise mutlak değeri büyük olandan küçük çıkarılır, büyüğün işareti verilir: (-7) + (+4) = -3\n\n" +
                    "2. Çarpma ve Bölme İşaret Kuralları:\n" +
                    "• (+) . (+) = (+)  |  (-) . (-) = (+)\n" +
                    "• (+) . (-) = (-)  |  (-) . (+) = (-)\n" +
                    "• Aynı işaretlilerin çarpımı/bölümü DAİMA pozitif (+)\n" +
                    "• Zıt işaretlilerin çarpımı/bölümü DAİMA negatif (-) dir.\n\n" +
                    "3. Çıkarma İşlemi:\n" +
                    "• Çıkarma işlemi, çıkan sayının ters işaretlisiyle toplama işlemine dönüştürülür: a - b = a + (-b). Örn: (-5) - (-3) = (-5) + (+3) = -2",
            
            questionBank = "📝 OKUL YAZILISI VE YAPRAK TEST SORULARI:\n\n" +
                    "[Soru 1] En büyük negatif tam sayı ile en küçük pozitif tam sayının toplamı kaçtır?\n" +
                    "A) -2   B) 0   C) -1   D) 1\n\n" +
                    "[Soru 2] (-12) : (+3) + (-4) . (-2) işleminin sonucu kaçtır?\n" +
                    "A) -4   B) 4   C) 12   D) -16\n\n" +
                    "[Soru 3] Bir dondurucunun sıcaklığı -18 derecedir. Sıcaklık her saat 2 derece artırılırsa 4 saat sonra dondurucu kaç derece olur?\n" +
                    "A) -10  B) -26  C) -14  D) -20",
            
            solutionKey = "🔑 DETAYLI SORU ÇÖZÜMLERİ VE ANALİZİ:\n\n" +
                    "[Soru 1 Çözümü]: En büyük negatif tam sayı '-1'dir. En küçük pozitif tam sayı ise '+1'dir. Bunların toplamı (-1) + (+1) = 0 olacaktır. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: İşlem önceliğine göre önce bölme ve çarpma yapılır.\n" +
                    "• Bölme: (-12) : (+3) = -4\n" +
                    "• Çarpma: (-4) . (-2) = +8\n" +
                    "• Toplama: (-4) + (+8) = +4. Doğru Cevap: B.\n\n" +
                    "[Soru 3 Çözümü]: Dondurucu sıcaklığı her saat 2 derece artmaktadır. 4 saatte: 4 . 2 = 8 derece artış olur. Sıcaklık -18 idi: (-18) + (+8) = -10 derece olur. Doğru Cevap: A.",
            
            studyTactics = "🚀 YENİ NESİL TAM SAYI SORULARINDA TAKTİKLER:\n\n" +
                    "• Taktik 1: Hava sıcaklığı, deniz seviyesi (0m), borç-alacak gibi sözel durumları anında negatif veya pozitif sayılarla kodla.\n" +
                    "• Taktik 2: Sayı doğrusunda sağa gidildikçe sayıların büyüdüğünü, sola gidildikçe küçüldüğünü asla unutma. -100 sayısı -5 sayısından çok daha küçüktür!\n" +
                    "• Taktik 3: Çıkarma işlemlerinde parantez önündeki eksi işaretini parantez içine dağıtmayı unutma. a - (-b) = a + b.",
            
            zihinHaritasi = "🧠 TAM SAYILAR ZİHİN HARİTASI\n\n" +
                    "• Z (Tam Sayılar)\n" +
                    "  ├── Negatif Sayılar (sıfırın solunda, sola gittikçe küçülür, borç/soğuk)\n" +
                    "  ├── Referans Noktası (0) (işaretsizdir, nötrdür)\n" +
                    "  ├── Pozitif Sayılar (sıfırın sağında, sağa gittikçe büyür, kâr/sıcak)\n" +
                    "  ├── İşlemler (çarpma/bölmede dostumun dostu dosttur kuralları)\n" +
                    "  └── Mutlak Değer (uzaklıktır, daima >= 0)",
            
            evdeDeney = "🧩 EVDE EĞLENCELİ DENEY VE DRAMA KILAVUZU:\n\n" +
                    "• Deneyin Adı: Sayı Doğrusunda Adımlarla Tam Sayılar\n" +
                    "• Malzemeler: Yere çizilecek şerit halinde bir sayı doğrusu, tebeşir.\n" +
                    "• Yapılışı: Bir aile üyeniz sıfır noktasında dursun. Ona '(-3) adım git, sonra üzerine (+5) ekle' komutu verin. Yürüyerek +2 noktasına ulaştığını gözlemleyin. Tam sayılarda toplama işlemini fiziksel olarak canlandırarak kavramış olacaksınız!"
        )
    }
    
    // 2. ÜSLÜ İFADELER
    if (lower.contains("üslü") || lower.contains("taban") || lower.contains("kuvvet") || lower.contains("üs leri") || lower.contains("üs ")) {
        return AcademicContent(
            intro = "Kazanım: Tam sayıların kendileri ile tekrarlı çarpımını üslü nicelik olarak ifade eder, üssün üssü ve basamak çözümleme kurallarını uygular.\n\n" +
                    "Bir sayının kendisiyle tekrarlı çarpımının kısa yoldan gösterilmesine üslü ifade denir. a'nın n. kuvveti (a^n) gösteriminde a sayısına taban, n sayısına üs (kuvvet) denir.\n" +
                    "Örn: 2^3 = 2 * 2 * 2 = 8. Burada 2 taban, 3 ise üsdür. Tam sayıların çift kuvvetleri daima pozitifken, negatif tabanların tek kuvvetleri negatiftir.",
            
            formulaSheet = "💡 ÜSLÜ SAYILAR AMORTİSÖRLÜ FORMÜLLERİ:\n\n" +
                    "1. Çarpma İşlemi:\n" +
                    "• Tabanlar aynı ise üsler toplanır: a^x . a^y = a^(x+y) (Örn: 2^3 . 2^4 = 2^7)\n" +
                    "• Üsler aynı ise tabanlar çarpılır: a^x . b^x = (a . b)^x (Örn: 2^3 . 5^3 = 10^3 = 1000)\n\n" +
                    "2. Bölme İşlemi:\n" +
                    "• Tabanlar aynı ise üsler çıkarılır: a^x / a^y = a^(x-y) (Örn: 5^7 / 5^3 = 5^4)\n" +
                    "• Üsler aynı ise tabanlar bölünür: a^x / b^x = (a/b)^x\n\n" +
                    "3. Üssün Üssü Kuralı:\n" +
                    "• (a^x)^y = a^(x . y) (Örn: (2^3)^2 = 2^6 = 64)\n\n" +
                    "4. Özel Durumlar:\n" +
                    "• a^0 = 1 (0 hariç tüm sayıların sıfırıncı kuvveti 1'dir)\n" +
                    "• a^(-x) = 1 / a^x (Negatif üs, sayıyı ters çevirir. Ör: 2^(-3) = 1/8)",
            
            questionBank = "📝 LGS VE YAZILI TARZI YAPRAK TEST SORULARI:\n\n" +
                    "[Soru 1] 2^5 . 4^3 işleminin sonucu aşağıdakilerden hangisidir?\n" +
                    "A) 2^11   B) 2^8   C) 2^15   D) 8^8\n\n" +
                    "[Soru 2] 5^(-3) sayısının rasyonel gösterimi hangisidir?\n" +
                    "A) -125   B) 1/125   C) -1/125   D) 1/15\n\n" +
                    "[Soru 3] Bir bakteri türü her 1 saatte 2 katına çıkmaktadır. Başlangıçta 16 bakteri bulunan bir kapta 5 saat sonra kaç bakteri olur?\n" +
                    "A) 2^5   B) 2^8   C) 2^9   D) 2^20",
            
            solutionKey = "🔑 DETAYLI ÇÖZÜM ANAHTARI:\n\n" +
                    "[Soru 1 Çözümü]: Soru 2^5 . 4^3 şeklinde verilmiştir. 4 sayısını 2'nin üssü olarak yazarız: 4 = 2^2. Buradan 4^3 = (2^2)^3 = 2^6 olur.\n" +
                    "Şimdi çarpmayı uygulayalım: 2^5 . 2^6 = 2^(5+6) = 2^11. Doğru Cevap: A.\n\n" +
                    "[Soru 2 Çözümü]: Negatif üs kuralına göre a^(-n) = 1/a^n dir. 5^(-3) = 1/5^3 = 1/(5 * 5 * 5) = 1/125 olur. Doğru Cevap: B.\n\n" +
                    "[Soru 3 Çözümü]: Başlangıçta 16 bakteri var, bu da 2^4 demektir. Her saat 2 katına çıkıyorsa 5 saat sonra 2^5 ile çarpmamız gerekir. 2^4 . 2^5 = 2^(4+5) = 2^9 bakteri olur. Doğru Cevap: C.",
            
            studyTactics = "🚀 ÜSLÜ SAYI YENİ NESİL SORU ÇÖZME STRATEJİLERİ:\n\n" +
                    "• Taktik 1: Tabanları her zaman en küçük asal çarpanlarına (2, 3, 5 gibi) dönüştür. Bu işlem tüm çarpmaları basitleştirir.\n" +
                    "• Taktik 2: Parantezin yerini kontrol et! (-3)^2 = +9 iken -3^2 = -9 dur. Parantez dışındaki çift kuvvetler eksiyi artı yapar.\n" +
                    "• Taktik 3: Çok büyük ve çok küçük sayıları 10'un kuvveti şeklinde yazarak işlem yükünden kurtul.",
            
            zihinHaritasi = "🧠 ÜSLÜ SAYILAR ZİHİN HARİTASI\n\n" +
                    "• Üslü Nicelikler\n" +
                    "  ├── Negatif Üs (sayıyı rasyonele çevirir, ters takla attırır)\n" +
                    "  ├── Taban Kuralları (negatif tabanın tek kuvveti negatif, çifti pozitif)\n" +
                    "  ├── İşlemler (çarparken üs topla/taban çarp, bölerken üs çıkar)\n" +
                    "  ├── Üssün Üssü (çarpılarak tek üs haline gelmesi)\n" +
                    "  └── Bilimsel Gösterim (1 ile 10 arasında katsayı ve 10^n formatı)",
            
            evdeDeney = "🧩 EVDE TEKRAR ETKİNLİĞİ:\n\n" +
                    "• Kağıt Katlama Oyunu: Bir A4 kağıdını alın. Sıfır kez katladığınızda 1 kat (2^0) var. Ortadan 1 kez katlayın 2 kat (2^1) olur. 2. kez katlayın 4 kat (2^2) olur. Her katlamada kat sayısının nasıl 2'nin üssü şeklinde arttığını deneyimleyin! 7 katlamadan sonra katların ne kadar kalınlaştığını görün."
        )
    }
    
    // 3. KAREKÖKLÜ SAYILAR
    if (lower.contains("kareköklü") || lower.contains("kök ") || lower.contains("kare kök")) {
        return AcademicContent(
            intro = "Kazanım: Tam kare doğal sayıları tanır, karekök alma işlemini anlamlandırarak bir sayının hangi tam sayılar arasında olduğunu tahmin eder.\n\n" +
                    "Verilen bir sayının hangi sayının karesi olduğunu bulma işlemine karekök alma denir. √x sembolüyle gösterilir. Alanı verilen geometrik bir karenin kenarını bulma işlemi olup, kök içindeki sayı asla negatif olamaz.\n" +
                    "Tam kare sayılar kök dışına tam sayı olarak çıkarlar. Örn: √1 = 1, √4 = 2, √9 = 3, √16 = 4, √25 = 5, √100 = 10.",
            
            formulaSheet = "💡 KAREKÖKLÜ İFADELERİN MATEMATİKSEL KURALLARI:\n\n" +
                    "1. a√b Gösterimi (Katsayıyı kök içine alma):\n" +
                    "• Katsayı karesi alınarak kök içerisine çarpan olarak girer: a√b = √(a^2 . b) (Ör: 3√2 = √(9 . 2) = √18)\n\n" +
                    "2. Çarpma ve Bölme İşlemi:\n" +
                    "• Katsayılar kendi arasında, kök içleri kendi arasında çarpılır: a√b . c√d = (a . c)√(b . d)\n" +
                    "• Bölmede de aynı kural geçerlidir: (a√b) / (c√d) = (a/c)√(b/d)\n\n" +
                    "3. Toplama ve Çıkarma İşlemi:\n" +
                    "• Sadece KÖK İÇLERİ AYNI olan terimler toplanıp çıkarılabilir: a√x + b√x = (a+b)√x (Örn: 5√3 - 2√3 = 3√3. Kök içleri farklıysa ÖRN √2 + √3 işlem yapılamaz aynen kalır!)",
            
            questionBank = "📝 KAREKÖK KONULU SEÇKİN YAPRAK TESTİK:\n\n" +
                    "[Soru 1] √108 sayısı hangi a√b şeklinde yazılabilir?\n" +
                    "A) 3√6   B) 6√3   C) 10√8   D) 2√27\n\n" +
                    "[Soru 2] √27 . √12 işleminin sonucu kaçtır?\n" +
                    "A) 18   B) √39   C) 9   D) 3√3\n\n" +
                    "[Soru 3] √53 sayısı sayı doğrusunda hangi iki tam sayı arasındadır?\n" +
                    "A) 5 ile 6   B) 6 ile 7   C) 7 ile 8   D) 8 ile 9",
            
            solutionKey = "🔑 ADIM ADIM SINAV SÜRECİ ÇÖZÜMLERİ:\n\n" +
                    "[Soru 1 Çözümü]: 108 sayısını tam kare çarpanlarına ayıralım. 108 = 36 . 3 olarak yazılır. 36 sayısı dışarıya 6 olarak çıkar. Böylece √108 = 6√3 olur. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Sayıları basitleştirip çarpalım.\n" +
                    "• √27 = 3√3\n" +
                    "• √12 = 2√3\n" +
                    "• Çarpım: 3√3 . 2√3 = (3 . 2) . (√3 . √3) = 6 . 3 = 18. Doğru Cevap: A.\n\n" +
                    "[Soru 3 Çözümü]: 53 sayısının komşusu olan iki tam kare sayıyı belirleriz. √49 < √53 < √64 dür.\n" +
                    "√49 = 7 ve √64 = 8 olduğuna göre √53 sayısı 7 ile 8 arasındadır. Doğru Cevap: C.",
            
            studyTactics = "🚀 KAREKÖKLÜ SORULAR İÇİN ALTIN STRATEJİLER:\n\n" +
                    "• Taktik 1: 1'den 20'ye kadar olan tüm sayıların karelerini (1, 4, 9, ..., 400) ezbere bilmek sana sınavlarda muazzam zaman kazandırır.\n" +
                    "• Taktik 2: Toplama-çıkarma yaparken karekök içlerini eşitlemeden asla katsayıları toplama.\n" +
                    "• Taktik 3: Ondalık sayıların karekökünü alırken önce rasyonel kesre çevir, sonra pay ve paydanın karekökünü al. (Örn: √(0.09) = √(9/100) = 3/10 = 0.3).",
            
            zihinHaritasi = "🧠 KAREKÖKLÜ SAYILAR HİYERARŞİSİ\n\n" +
                    "• Karekök Kavramı\n" +
                    "  ├── Tam Kareler (1, 4, 9, 16, 25 dışarı tık diye çıkanlar)\n" +
                    "  ├── Tahmin (Kök içindeki değere göre en yakın tam sayıları bulma)\n" +
                    "  ├── a Kök b Formatı (çarpanlarına ayırıp kareyi dışarı fırlatma)\n" +
                    "  ├── Çarpma/Bölme (dışlar dışla, içler içle çarpılır/bölünür)\n" +
                    "  └── Toplama/Çıkarma (yalnızca kökü ortak olanlar işlem görür)",
            
            evdeDeney = "🧩 EVDE ANALİTİK UYGULAMA:\n\n" +
                    "• Karekenar Tasarlama Oyunu: Kartonlardan alanları 9 cm2, 16 cm2 ve 25 cm2 olan kareler kesin. Bir cetvelle bu karelerin kenarlarını ölçün. Kenarların sırasıyla 3 cm, 4 cm ve 5 cm olduğunu görün. Böylece alanın karekökünün bir kenarı verdiğini gözlerinizle teyit edin!"
        )
    }
    
    // 4. KESİRLER, ONDALIK GÖSTERİM VE YÜZDELER
    if (lower.contains("kesir") || lower.contains("ondalık") || lower.contains("yüzde") || lower.contains("oran") || lower.contains("orantı")) {
        return AcademicContent(
            intro = "Kazanım: Kesirlerle toplama, çıkarma, çarpma ve bölme işlemlerini yapar, kesirleri ondalık gösterim ve yüzdelere dönüştürerek günlük problemlere uygular.\n\n" +
                    "Rasyonel sayılar dünyasına adım atıyoruz! Bir bütünü eş parçalara böldüğümüzde oluşan her bir parçaya kesir denir. a/b şeklinde gösterilir. Payda (b) sıfır olamaz.\n" +
                    "Paydası 10, 100, 1000 olan kesirler ondalık gösterim (virgüllü) olarak yazılabilir. Paydası 100 olan kesirler ise doğrudan '%' yüzde simgesiyle ifade edilir.",
            
            formulaSheet = "💡 RASYONEL VE ONDALIK İŞLEM KURALLARI:\n\n" +
                    "1. Kesirlerde Toplama & Çıkarma:\n" +
                    "• Mutlaka paydalar EŞİTLENMELİDİR: 1/2 + 1/3 = 3/6 + 2/6 = 5/6\n\n" +
                    "2. Çarpma ve Bölme:\n" +
                    "• Çarpmada paylar kendi arasında, paydalar kendi arasında çarpılır: (a/b) . (c/d) = (a . c)/(b . d)\n" +
                    "• Bölmede ilk kesir aynen kalır, ikincisi TERS ÇEVRİLİP çarpılır: (a/b) : (c/d) = (a/b) . (d/c)\n\n" +
                    "3. Ondalık ve Yüzde Dönüşümü:\n" +
                    "• Bir kesri yüzdeye çevirmek için paydasını 100 yapacak şekilde genişletiriz ya da sadeleştiririz. Ör: 3/5 = 60/100 = 0.60 = %60.",
            
            questionBank = "📝 KAZANIM PEKİŞTİRME YAPRAK TESTI:\n\n" +
                    "[Soru 1] 2/5 kesrinin ondalık ve yüzde sembolü ile gösterimi hangisinde doğru verilmiştir?\n" +
                    "A) 0.2 ve %20   B) 0.4 ve %40   C) 0.5 ve %50   D) 0.04 ve %4\n\n" +
                    "[Soru 2] (1/2 + 1/3) : (5/6) işleminin sonucu kaçtır?\n" +
                    "A) 1   B) 25/36   C) 5/6   D) 2\n\n" +
                    "[Soru 3] 200 liralık bir gömlek %20 indirimle satıldığında indirimli fiyatı kaç lira olur?\n" +
                    "A) 180   B) 160   C) 140   D) 150",
            
            solutionKey = "🔑 DOĞRULANMIŞ ÇÖZÜM REHBERİ:\n\n" +
                    "[Soru 1 Çözümü]: 2/5 kesrini yüzde yapmak için paydayı 20 ile genişletiriz: (2 * 20) / (5 * 20) = 40/100. Bu da ondalık olarak 0.4, yüzde olarak %40 demektir. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Önce parantez içi yapılır. 1/2 (3 ile) ve 1/3 (2 ile) genişletilerek toplanır: 3/6 + 2/6 = 5/6.\n" +
                    "Bölme işleminde: (5/6) : (5/6) yapılır. Bir sayının kendisine bölümü 1'dir. Doğru Cevap: A.\n\n" +
                    "[Soru 3 Çözümü]: Gömleğin fiyatı 200 TL'dir. İndirim tutarı: 200 * (20/100) = 40 TL. İndirimli fiyatı ise: 200 - 40 = 160 TL olacaktır. Doğru Cevap: B.",
            
            studyTactics = "🚀 HIZLI KESİR VE YÜZDE ÇÖZÜM YOLLARI:\n\n" +
                    "• Taktik 1: Yüzde sorularında sıfır silme taktiğini kullan. Bir sayının %10'unu bulmak için sayının sonundan bir sıfır silersin. %20'si için ise çıkan sayıyı 2 ile çarparsın.\n" +
                    "• Taktik 2: Kesir bölmelerinde sadeleştirmeleri çarpma yapmadan ÖNCE yap ki sayılar büyümesin.\n" +
                    "• Taktik 3: Ondalık gösterimlerde toplama çıkarma yaparken virgüllerin alt alta gelmesine aşırı dikkat göster. Eksik basamaklara sıfır koy.",
            
            zihinHaritasi = "🧠 KESİR VE YÜZDELER DETAYI\n\n" +
                    "• Kesirler dünyası\n" +
                    "  ├── Basit/Bileşik/Tam Sayılı kesirler\n" +
                    "  ├── Payda Eşitleme (Toplama ve çıkarmanın olmazsa olmaz şartı)\n" +
                    "  ├── Ondalık Bölüm (payı paydaya bölerek virgüllü hale getirme)\n" +
                    "  └── Yüzdeler (paydada 100 olması, indirim ve faiz hesaplama modeli)",
            
            evdeDeney = "🧩 EVDE SÖZEL/SAYISAL UYGULAMA:\n\n" +
                    "• Mutfakta Oran ve Pizza Deneyi: Evde bir pizza veya keki 8 eşit dilime bölün. Eğer 2 dilimini yerseniz bütünün kaçta kaçını (2/8 = 1/4 = %25) yediğinizi hesaplayın. Geriye kalanın yüzde kaç (%75) olduğunu tabağa bakarak somutlaştırın."
        )
    }
    
    // 5. GEOMETRİ, AÇILAR, ALAN, HACİM
    if (lower.contains("geometri") || lower.contains("açı") || lower.contains("üçgen") || lower.contains("çember") || lower.contains("dörtgen") || lower.contains("hacim") || lower.contains("alan") || lower.contains("prizma") || lower.contains("silindir")) {
        return AcademicContent(
            intro = "Kazanım: Çokgenlerin ve üçgenlerin özelliklerini belirler, açı ölçülerini hesaplar, çevre, koordinat sistemi ve alan/hacim bağıntılarını geometrik modellerle açıklar.\n\n" +
                    "Geometri geometrik cisimleri, çizgileri ve uzaydaki yapıları inceler. Açı, kesişen iki doğrunun oluşturduğu açıklıktır. Derece ile ölçülür.\n" +
                    "Çokgenlerde kenar sayısı arttıkça iç açılar toplamı da formüle bağlı olarak artar. Temel kuralımız her zaman düzgün şekillerin simetrisini korumaktır.",
            
            formulaSheet = "💡 GEOMETRİK ALAN VE HACİM BAĞINTILARI:\n\n" +
                    "1. Açı Kuralları:\n" +
                    "• Üçgenin iç açıları toplamı DAİMA 180 derecedir.\n" +
                    "• Dörtgenlerin iç açıları toplamı DAİMA 360 derecedir.\n" +
                    "• Doğru Açı 180 derece, Tam Açı 360 derece, Dik Açı ise 90 derecedir.\n\n" +
                    "2. Alan Formülleri:\n" +
                    "• Üçgenin Alanı = (Taban . Yükseklik) / 2\n" +
                    "• Dikdörtgenin Alanı = Kısa Kenar * Uzun Kenar\n" +
                    "• Paralelkenarın Alanı = Taban * O tabana ait yükseklik\n" +
                    "• Çemberin Çevresi = 2 * pi * r  |  Dairenin Alanı = pi * r^2\n\n" +
                    "3. Katı Cisim Hacimleri:\n" +
                    "• Dikdörtgeler Prizması Hacmi = Taban Alanı * Yükseklik = a * b * c",
            
            questionBank = "📝 SIK SORULAN GEOMETRİ YAPRAK TESTİ:\n\n" +
                    "[Soru 1] Bir dik üçgenin dik kenarlarından biri 6 cm, diğeri 8 cm ise bu üçgenin alanı kaç cm2'dir?\n" +
                    "A) 48   B) 24   C) 14   D) 10\n\n" +
                    "[Soru 2] İç açılarından ikisi 50 ve 70 derece olan bir üçgenin üçüncü iç açısı kaç derecedir?\n" +
                    "A) 60 derece   B) 80 derece   C) 90 derece   D) 100 derece\n\n" +
                    "[Soru 3] Yarıçapı 5 cm olan bir dairenin alanı kaç cm2'dir? (pi = 3 alınız)\n" +
                    "A) 75   B) 30   C) 25   D) 15",
            
            solutionKey = "🔑 HASSAS ÇÖZÜM ANAHTARI:\n\n" +
                    "[Soru 1 Çözümü]: Dik üçgenin alanı dik kenarların çarpımının yarısıdır.\n" +
                    "Alan = (6 * 8) / 2 = 48 / 2 = 24 cm2 dir. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Üçgenin iç açıları toplamı 180 derecedir. Verilen açıları toplayalım: 50 + 70 = 120 derece.\n" +
                    "Üçüncü açıyı bulmak için toplamdan çıkarırız: 180 - 120 = 60 derece. Doğru Cevap: A.\n\n" +
                    "[Soru 3 Çözümü]: Dairenin alan formülü Alan = pi * r^2 dir. Yarıçap r = 5 cm, pi = 3.\n" +
                    "Alan = 3 * 5^2 = 3 * 25 = 75 cm2 olur. Doğru Cevap: A.",
            
            studyTactics = "🚀 SINAVDA GEOMETRİ SORULARI ÇÖZME METOTLARI:\n\n" +
                    "• Taktik 1: Şeklin üzerine verilmeyen açıları ve uzunlukları harflerle yazarak başla. Geometride görmenin ilk şartı şekli doldurmaktır.\n" +
                    "• Taktik 2: Üçgenlerde özel kenar bağıntılarını (3-4-5, 5-12-13, 8-15-17 dik üçgenleri) adın gibi hatırla.\n" +
                    "• Taktik 3: Çevre ve Alan formüllerini birbirine karıştırma. Çevrede üstel birim (cm) varken, alanda birim her zaman karedir (cm2).",
            
            zihinHaritasi = "🧠 GEOMETRİ SİSTEMATİĞİ\n\n" +
                    "• Çokgenler ve Açılar\n" +
                    "  ├── Açılar (Dar, Dik, Geniş, Doğru, Komşu, Ters açılar)\n" +
                    "  ├── Üçgenler (Çeşitkenar, İkizkenar, Eşkenar, Dik üçgen)\n" +
                    "  ├── Çember/Daire (Çap, yarıçap, teğet, yay uzunluğu, pi sabiti)\n" +
                    "  └── Alan Hesaplama (Taban ve yükseklik çarpımı esaslı formüller)",
            
            evdeDeney = "🧩 EVDE MODELLEME ETKİNLİĞİ:\n\n" +
                    "• Çemberin Çevresinde Pi Sayısını Bulma: Evdeki bardak, kapak gibi yuvarlak nesneleri toplayın. Çevrelerini bir ip yardımıyla ölçün, ardından çaplarını cetvelle ölçün. Çevreyi çapa böldüğünüzde her nesne için sonucun yaklaşık 3.14 (pi) çıktığını görerek matematiğin eşsiz sırrını keşfedin."
        )
    }
    
    // 6. ÇARPANLAR VE KATLAR
    if (lower.contains("çarpan") || lower.contains("katlar") || lower.contains("ebob") || lower.contains("ekok") || lower.contains("asal")) {
        return AcademicContent(
            intro = "Kazanım: Verilen pozitif tam sayıların çarpanlarını bulur, ortak bölen ve ortak kat (EBOB-EKOK) problemlerini çözer, aralarında asallığı sorgular.\n\n" +
                    "Her pozitif tam sayı, en az iki tam sayının çarpımı şeklinde yazılabilir. Bu sayılara o sayının çarpanları (bölenleri) denir.\n" +
                    "Asal Sayılar: Sadece 1'e ve kendisine bölünebilen, 1'den büyük doğal sayılardır. En küçük asal sayı 2'dir ve 2'den başka çift asal sayı yoktur.",
            
            formulaSheet = "💡 EBOB VE EKOK MATEMATİK MATRİSİ:\n\n" +
                    "1. EBOB (En Büyük Ortak Bölen):\n" +
                    "• İki veya daha fazla sayıyı aynı anda bölen en büyük sayıdır. Ortak asal çarpanlardan üssü en küçük olanlar çarpılarak bulunur.\n\n" +
                    "2. EKOK (En Küçük Ortak Kat):\n" +
                    "• Sayıların katları olan en küçük sayıdır. Asal çarpanların tamamı (en büyük üsleriyle) çarpılarak bulunur.\n\n" +
                    "3. Altın Kurallar:\n" +
                    "• İki sayının çarpımı, o sayıların EBOB'u ile EKOK'unun çarpımına eşittir: a * b = EBOB(a,b) * EKOK(a,b)\n" +
                    "• Aralarında asal iki sayının EBOB'u 1'dir, EKOK'u ise sayıların çarpımıdır.",
            
            questionBank = "📝 ÇARPANLAR DERSLİG YAPRAK TESTİ:\n\n" +
                    "[Soru 1] 24 sayısının pozitif tam sayı çarpanlarından kaç tanesi tek sayıdır?\n" +
                    "A) 2   B) 4   C) 1   D) 3\n\n" +
                    "[Soru 2] EBOB(12, 18) ile EKOK(12, 18) toplamı kaçtır?\n" +
                    "A) 42   B) 24   C) 48   D) 36\n\n" +
                    "[Soru 3] Boyutları 12 metre ve 20 metre olan dikdörtgen biçimindeki bir salonun tabanı eş büyüklşte kare fayanslarla kaplanacaktır. En az kaç fayans gerekir?\n" +
                    "A) 15   B) 8   C) 20   D) 12",
            
            solutionKey = "🔑 DETAYLI VE DOĞRU ANALİZLİ ÇÖZÜMLER:\n\n" +
                    "[Soru 1 Çözümü]: 24 sayısının çarpanları: 1, 2, 3, 4, 6, 8, 12, 24'tür. Bunlar içindeki tek sayılar sadece '1' ve '3'tür. Yani 2 tanedir. Doğru Cevap: A.\n\n" +
                    "[Soru 2 Çözümü]: 12 ve 18'in ortak bölenlerini ve katlarını bulalım.\n" +
                    "• EBOB(12, 18): İkisini de bölen en büyük sayı 6'dır.\n" +
                    "• EKOK(12, 18): İkisinin de en küçük katı 36'dır.\n" +
                    "• Toplam: 6 + 36 = 42 olur. Doğru Cevap: A.\n\n" +
                    "[Soru 3 Çözümü]: Kare fayansların bir kenarı, salon boyutları olan 12 ve 20'nin EBOB'u olmalıdır. EBOB(12, 20) = 4 metredir.\n" +
                    "Gereken Fayans Sayısı = (Salonun Alanı) / (Fayansın Alanı) = (12 * 20) / (4 * 4) = 240 / 16 = 15 adet fayans gerekir. Doğru Cevap: A.",
            
            studyTactics = "🚀 SINAVDA EBOB-EKOK AYIRT ETME TAKTİĞİ:\n\n" +
                    "• Taktik 1: Büyük parçalardan küçük parçalar elde ediliyorsa, bölme/bölüştürme/paylaştırma varsa bu bir EBOB sorusudur. (Örn: Çuvallardaki unları poşetleme, bahçe kenarına ağaç dikme).\n" +
                    "• Taktik 2: Küçük parçalardan büyük parçalar yapılıyorsa, zamanların çakışması veya nöbetlerin birleşmesi varsa bu bir EKOK sorusudur. (Örn: Zillerin çalması, gemilerin sefere çıkması).\n" +
                    "• Taktik 3: Ardışık sayıların aralarında asal olduğunu ve aralarında asal sayıların EBOB'unun daima 1 olduğunu aklından çıkarma.",
            
            zihinHaritasi = "🧠 ÇARPANLAR VE ASALLAR MATRİSİ\n\n" +
                    "• Sayılar Teorisi\n" +
                    "  ├── Çarpan Bulma (gökkuşağı yöntemi ile baştan sondan eşleme)\n" +
                    "  ├── Asal Sayılar (2, 3, 5, 7, 11, 13, 17... yalnızca 1'e bölünenler)\n" +
                    "  ├── Asal Çarpan Algoritması (bölen çizgisi ile çarpanlarına ayırma)\n" +
                    "  └── EBOB & EKOK (parçadan bütüne EKOK, bütünden parçaya EBOB)",
            
            evdeDeney = "🧩 EVDE LGS PRATİĞİ:\n\n" +
                    "• Süzgeçte Pirinç Ayıklama: Evde iki farklı kasede nohut ve mercimek karıştırın. Bunları ortak hacimli bardaklarla paketlemeye çalışın. Evdeki kapların hacimlerini EBOB kurallarına göre analiz ederek tam eşitleme yapın."
        )
    }
    
    // 7. DNA VE GENETİK KOD / CANLILAR
    if (lower.contains("dna") || lower.contains("genetik") || lower.contains("kalıtım") || lower.contains("hücre") || lower.contains("çekirdek") || lower.contains("kromozom") || lower.contains("gen ") || lower.contains("mitoz") || lower.contains("mayoz")) {
        return AcademicContent(
            intro = "Kazanım: Hücre çekirdeğindeki kalıtsal yapıları basitten karmaşığa sıralar, DNA'nın yapısını ve kendini eşleme aşamalarını açıklar, kromozom anomalilerini irdeler.\n\n" +
                    "Tüm canlıların yaşamsal faaliyetlerini (solunum, boşaltım, üreme) yöneten ve kalıtsal özelliklerini (göz rengi, boy uzunluğu vb.) nesilden nesile aktaran yönetici moleküle DNA (Deoksiribo Nükleik Asit) denir.\n" +
                    "Çekirdek içinde DNA, özel protein kılıfla kaplanarak kromozom kıyafetini giyer. Canlılar arasındaki çeşitlilik, DNA'mızdaki nükleotit dizilimlerinin farklı olmasından kaynaklanır.",
            
            formulaSheet = "💡 DNA VE NÜKLEOTİT YAPISAL KURALLARI:\n\n" +
                    "1. Genetik Yapıların Büyükten Küçüğe Sıralanması:\n" +
                    "• Kromozom > DNA > Gen > Nükleotit (Akılda kalıcı şifre: KEDİGENİ)\n\n" +
                    "2. Nükleotit Yapısı:\n" +
                    "• 1 Nükleotit = 1 Fosfat + 1 Deoksiribo Şekeri + 1 Organik Baz\n\n" +
                    "3. Bağlanma ve Sayısal Denklemler:\n" +
                    "• Adenin (A) daima Timin (T) ile eşleşir ve aralarında ikili zayıf hidrojen bağı kurulur: A = T\n" +
                    "• Guanin (G) daima Sitozin (C) ile eşleşir ve aralarında üçlü zayıf hidrojen bağı kurulur: G = C\n" +
                    "• Bir DNA zincirinde: Toplam Nükleotit Sayısı = Toplam Fosfat = Toplam Şeker = Toplam Organik Baz",
            
            questionBank = "📝 KAZANIM ODAKLI LGS FEN YAPRAK TESTI:\n\n" +
                    "[Soru 1] DNA'nın en küçük yapı birimi ve görev birimi sırasıyla aşağıdakilerden hangisidir?\n" +
                    "A) Kromozom - Gen   B) Nükleotit - Gen   C) Gen - Hücre   D) Nükleotit - Çekirdek\n\n" +
                    "[Soru 2] Sağlıklı bir DNA molekülünde 600 fosfat ve 150 Adenin nükleotiti varsa bu DNA'daki Sitozin sayısı kaçtır?\n" +
                    "A) 150   B) 300   C) 450   D) 200\n\n" +
                    "[Soru 3] DNA kendini eşlerken gerçekleşen olaylardan hangisi en son gerçekleşir?\n" +
                    "A) Sitoplazmada serbest nükleotitlerin miktarının azalması\n" +
                    "B) Zincirlerin bir fermuar gibi açılması\n" +
                    "C) Yeni nükleotitlerin çekirdeğe girmesi\n" +
                    "D) Birbiri ile tamamen aynı iki yeni DNA molekülünün oluşması",
            
            solutionKey = "🔑 FEN BİLİMLERİ DOĞRULANMIŞ ANALİZ REHBERİ:\n\n" +
                    "[Soru 1 Çözümü]: DNA'nın en küçük görev birimi protein sentezini yöneten 'Gen'dir. Yapı birimi ise nükleik asit zincirini oluşturan 'Nükleotit'tir. Dolayısıyla doğru sıra Nükleotit ve Gen'dir. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Toplam fosfat sayısı = 600, bu da toplam nükleotit sayısının 600 olduğunu gösterir. DNA çift zincirlidir.\n" +
                    "• A = T kuralına göre 150 Adenin varsa, 150 Timin vardır. Toplamı A + T = 300 nükleotit.\n" +
                    "• Geriye kalan nükleotit: 600 - 300 = 300 Guanin ve Sitozinlerin toplamıdır.\n" +
                    "• G = C olduğuna göre Sitozin sayısı 300 / 2 = 150 adettir. Doğru Cevap: A.\n\n" +
                    "[Soru 3 Çözümü]: DNA eşlenirken ilk önce enzimler eşliğinde fermuar gibi açılır, nükleotitler çekirdeğe göç eder ve en son aşamada tamamen aynı iki sarmal kopya DNA sentezi tamamlanır. Doğru Cevap: D.",
            
            studyTactics = "🚀 FEN SINAVLARI VE DNA TAM ÖĞRENME STRATEJİLERİ:\n\n" +
                    "• Taktik 1: Sözel sorularda 'canlıların benzer veya farklı tür olmasının nedeni kromozom sayıları değildir' kuralını asla unutma! Eğrelti otunda 500 kromozom varken insanda 46 kromozom vardır; yani kromozom sayısı gelişmişliği göstermez.\n" +
                    "• Taktik 2: DNA eşlenirken sitoplazmada serbest nükleotit sayısının azaldığını, çekirdekte ise nükleotit sayısının arttığını grafik sorularında hemen işaretle.\n" +
                    "• Taktik 3: Hücre bölünmesinden önce DNA'nın neden kendisini eşlediğini 'kalıtsal bilgiyi yavru hücrelere eksiksiz aktarmak' şeklinde ezberle.",
            
            zihinHaritasi = "🧠 DNA VE KALITIM SEMA ŞEMASI\n\n" +
                    "• Hücre Yönetim Merkezi\n" +
                    "  ├── Çekirdek (merkezi kasa)\n" +
                    "  ├── Kromozom (DNA'nın koruyucu özel protein kılıflı hali, 46 adet)\n" +
                    "  ├── DNA (çift zincirli sarmal yönetici deoksiribo molekül zinciri)\n" +
                    "  ├── Gen (saç rengi, kan grubu gibi özellikleri kodlayan görev birimi)\n" +
                    "  └── Nükleotit (fosfat + şeker + organik bazdan oluşan yapı taşı)",
            
            evdeDeney = "🧩 EVDE EĞLENCELİ BİLİM ETKİNLİĞİ:\n\n" +
                    "• Evde 3D DNA Sarmal Modeli Tasarlama: Evdeki renkli boncukları veya ataçları toplayın. Mavi boncukları Fosfat, kırmızı boncukları Şeker olarak ataçlarla zincirleyin. Orta kısmdaki baz eşleşmelerini göstermek için kartonlara A, T, G, C yazıp ikili sarmal yapıda katlayıp birbirine bağlayın. DNA sarmalını odanızın en güzel köşesine asın!"
        )
    }
    
    // 8. MEVSİMLER VE İKLİM
    if (lower.contains("mevsim") || lower.contains("iklim") || lower.contains("hava") || lower.contains("güneş") || lower.contains("dünya") || lower.contains("ay") || lower.contains("gezegen") || lower.contains("tutulma")) {
        return AcademicContent(
            intro = "Kazanım: Mevsimlerin oluşumuna yönelik tezler üretir, Dünya'nın dönme ekseni eğikliğini ve Güneş etrafındaki konumlarını haritalandırır. İklim ile hava olayları arasındaki farkları analiz eder.\n\n" +
                    "Dünyamızın kendi ekseninde dönmesiyle gece-gündüz oluşurken, mevsimlerin oluşmasının arkasında iki muazzam gök mekanizması yatar: Dünya'nın dönme ekseninin Güneş sarmalına karşı 23.5 derece eğik olması ve Dünya'nın Güneş etrafında eliptik yörüngede dolanmasıdır.",
            
            formulaSheet = "💡 MEVSİMLER VE İKLİMİN COĞRAFİK KURALLARI:\n\n" +
                    "1. Dönüm Tarihleri ve Gün Dönümleri:\n" +
                    "• 21 Haziran: Kuzey Yarım Küre'ye (KYK) güneş ışınları dik gelir. Yaz mevsimi başlar, en uzun gündüz yaşanır.\n" +
                    "• 21 Aralık: Güney Yarım Küre'ye (GYK) dik gelir. KYK'de kış, GYK'de yaz başlar.\n" +
                    "• 21 Mart / 23 Eylül (Ekinoks): Dünya'nın her yerinde gece ve gündüz süreleri eşit (12 saat) olur.\n\n" +
                    "2. İklim vs Hava Durumu Farkları (LGS'de Kesin Çıkar!):\n" +
                    "• İKLİM: Geniş bölge, uzun zaman (35-40 yıl ortalaması), kesindir, Klimatolog inceler.\n" +
                    "• HAVA DURUMU: Dar bölge, anlık zaman, tahmindir, Meteorolog inceler.",
            
            questionBank = "📝 MEVSİMLER VE HAVA HAREKETLERİ YAPRAK TESTİ:\n\n" +
                    "[Soru 1] Aşağıdakilerden hangisi Dünya üzerinde mevsimlerin oluşmasının temel nedenlerinden biridir?\n" +
                    "A) Dünya'nın kendi etrafındaki dönüş hızı\n" +
                    "B) Dünya'nın dolanma ekseninin eliptik yörüngede eğik olması\n" +
                    "C) Güneş'in kendi etrafında dönmesi\n" +
                    "D) Ay'ın Dünya'ya yakınlığı ve gelgit etkisi\n\n" +
                    "[Soru 2] 'Giresun'da bugün öğleden sonra aniden bastıran sağanak yağış su baskınlarına yol açtı.' Bu ifade hangi kapsama girer?\n" +
                    "A) İklim   B) Klimatoloji   C) Hava Olayı   D) Genel Jeoloji\n\n" +
                    "[Soru 3] Rüzgarın esiş yönü daima fiziksel olarak nasıldır?\n" +
                    "A) Alçak basınç alanından yüksek basınç alanına doğru\n" +
                    "B) Yüksek basınç alanından alçak basınç alanına doğru\n" +
                    "C) Doğudan batıya doğru sabit\n" +
                    "D) Denizden karaya doğru daima",
            
            solutionKey = "🔑 COĞRAFİK FEN ÇÖZÜM ANAHTARI:\n\n" +
                    "[Soru 1 Çözümü]: Mevsimlerin oluşmasında en önemli etken eksen eğikliği ve dolanma hareketidir. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Belirli bir günde, kısa bir sürede aniden değişen yağış, rüzgar gibi olaylar 'Hava Olayı'dır. İklim ise uzun yıllar boyunca devam eden kararlı şartlardır. Doğru Cevap: C.\n\n" +
                    "[Soru 3 Çözümü]: Sıcaklık farklarından oluşan yüksek basınç (soğuk olan bölge) alanından alçak basınç (sıcak olan bölge) alanına doğru yatay yönlü hava hareketine rüzgar denir. Hava daima yüksekten alçağa akar. Doğru Cevap: B.",
            
            studyTactics = "🚀 MEVSİMLER VE COĞRAFİK GRAFİK STRATEJİLERİ:\n\n" +
                    "• Taktik 1: Güneş ışınları dik (90 derece) açıyla gelirse birim alana düşen ısı enerjisi maksimum olur, sıcaklık yükselir. Eğik gelirse yayılır ve az ısıtır.\n" +
                    "• Taktik 2: LGS'de grafik sorularında öğle vakti gölge boyunun sıfır veya en kısa olduğu gün o güne yaz mevsiminin başladığını (güneşin en tepede olduğunu) bil.\n" +
                    "• Taktik 3: KYK ve GYK'nin daima birbirinin tam zıttı mevsimleri yaşadığını aklından çıkarma. Biz kartopu oynarken Avustralya'dakiler denize giriyor!",
            
            zihinHaritasi = "🧠 MEVSİMLER VE HAVA ŞEMASI\n\n" +
                    "• Küresel İklim ve Atmosfer\n" +
                    "  ├── Eksen Eğikliği (23.5 derece açıyla eğilme)\n" +
                    "  ├── Solstis & Ekinoks (21 Haziran, 21 Aralık, 21 Mart, 23 Eylül)\n" +
                    "  ├── Hava Basınçları (Yüksek Basınç = Soğuk/Alçalıcı, Alçak Basınç = Sıcak/Yükselici)\n" +
                    "  └── Küresel Isınma (Atmosferdeki sera gazlarının artışı ile oluşan tehdit)",
            
            evdeDeney = "🧩 EVDE COĞRAFYA LABORATUVARI:\n\n" +
                    "• Portakal ve El Feneri ile Mevsimler Deneyi: Karanlık bir odada el fenerini Güneş kabul edin. Bir portakala ortadan kürdan batırıp Dünya şeklinde eğik tutarak fenerin etrafında elips çizin. Fener ışığının portakalın üst ve alt kısımlarına hangi açılarla düştüğünü görerek ekinoksları kafanızda canlandırın!"
        )
    }
    
    // 9. FİZİK, BASINÇ, KUVVET, ELEKTRİK, SES, YOĞUNLUK, ISI-SICAKLIK
    if (lower.contains("basınç") || lower.contains("kuvvet") || lower.contains("yoğunluk") || lower.contains("ısı") || lower.contains("sıcaklık") || lower.contains("ses") || lower.contains("elektrik") || lower.contains("madde") || lower.contains("makine") || lower.contains("enerji") || lower.contains("sürat") || lower.contains("hareket")) {
        return AcademicContent(
            intro = "Kazanım: Fiziksel kuvvet ve basınç ilkelerini keşfeder, formülleri geometrik yüzeylerle bağdaştırarak katı, sıvı ve gaz fazındaki basınç dengelerini hesaplar.\n\n" +
                    "Basınç, birim yüzeye dik olarak etki eden kuvvettir. P harfi ile gösterilir ve birimi Pascal (Pa)'dır. Basınç, temel katı basıncı, sıvı basıncı ve açık hava (gaz) basıncı olarak üçe ayrılır. Katılarda temas alanı ve ağırlık, sıvılarda ise derinlik ve yoğunluk asıl faktörlerdir.",
            
            formulaSheet = "💡 FİZİK VE KUVVET FORMÜLLERİ MATRİSİ:\n\n" +
                    "1. Katı Basıncı:\n" +
                    "• P = G / S (Ağırlık / Temas Yüzey Alanı)\n" +
                    "• Ağırlık artarsa katı basıncı artar. Temas yüzeyi artarsa basınç azalır.\n\n" +
                    "2. Sıvı Basıncı:\n" +
                    "• P = h . d . g (Derinlik . Yoğunluk . Yerçekimi İvmesi)\n" +
                    "• Sıvının kabın şekline veya sıvı miktarına bağlı DEĞİLDİR.\n\n" +
                    "3. Gaz ve Açık Hava Basıncı:\n" +
                    "• Barometre ile ölçülür. Deniz seviyesinde en yüksektir, yukarılara çıkıldıkça açık hava basıncı azalır.",
            
            questionBank = "📝 KAZANIM PEKİŞTİRME FEN YAPRAK TESTİ:\n\n" +
                    "[Soru 1] Bir kutu düz zemin üzerindeyken ters çevrilip daha dar olan tabanı üzerine konulursa ağırlığı ve zemine uyguladığı katı basıncı nasıl değişir?\n" +
                    "A) Ağırlık artar, basınç değişmez\n" +
                    "B) ikisi de artar\n" +
                    "C) Ağırlık değişmez, basınç artar\n" +
                    "D) ikisi de değişmez\n\n" +
                    "[Soru 2] Aynı dondurma kabı içine su, zeytinyağı ve tuzlu su dolduruluyor. Kabın en dibindeki sıvı basıncı hangi sıvıda en fazladır? (Yoğunluklar: Tuzlu su > Su > Zeytinyağı)\n" +
                    "A) Su   B) Zeytinyağı   C) Tuzlu Su   D) Hepsi Eşittir\n\n" +
                    "[Soru 3] Gaz basıncını deniz seviyesinde cıva tüpü kullanarak ilk kez ölçen bilim insanı kimdir?\n" +
                    "A) Pascal   B) Newton   C) Toriçelli   D) Einstein",
            
            solutionKey = "🔑 DETAYLI ANALİTİK ÇÖZÜMLER REHBERİ:\n\n" +
                    "[Soru 1 Çözümü]: Kutunun ağırlığı ters çevrilmeyle değişmez (G sabittir). Ancak kutu dar tabanı üzerine konulduğunda temas yüzey alanı (S) küçülmüş olur. P = G/S kuralına göre alan küçülürse katı basıncı artar. Doğru Cevap: C.\n\n" +
                    "[Soru 2 Çözümü]: Sıvı basıncı P = h . d . g dir. Derinlikler (h) dondurma kapları aynı olduğu için eşittir. Yoğunluğu (d) en yüksek olan sıvının tabana uygulayacağı basınç en fazladır. Tuzlu suyun yoğunluğu en büyüktür. Doğru Cevap: C.\n\n" +
                    "[Soru 3 Çözümü]: Açık hava basıncını cıva dolu çanak ve cam tüple deniz kenarında 0 derecede ölçüp 76 cm-Hg bulan ünlü İtalyan fizikçi Toriçelli'dir. Doğru Cevap: C.",
            
            studyTactics = "🚀 FEN VE BASINÇ GRAFİĞİ ANALİZ TAKTİKLERİ:\n\n" +
                    "• Taktik 1: Çivi ucu, krampon çivileri, bıçak ağzı gibi tasarımlar basınca dayalı keskinlik artırmak için yüzey alanını küçültme amaçlıdır.\n" +
                    "• Taktik 2: Pascal Prensibi: Sıvılar üzerlerine uygulanan basıncı her yöne ve aynen iletirler. İtfaiye merdiveni, berber koltuğu, fren sistemleri bu prensibe dayanır!\n" +
                    "• Taktik 3: Kabın şekli nasıl bükülürse bükülsün, sıvının sadece kabın tavanından olan dikey derinliğine odaklan. Yanıltıcı eğik kaplara kanma.",
            
            zihinHaritasi = "🧠 FİZİKSEL BASINÇ SİSTEMATİĞİ\n\n" +
                    "• Mekanik Basınç\n" +
                    "  ├── Katı Basıncı (P=G/S, bıçaklar, kar ayakkabıları örnekleri)\n" +
                    "  ├── Sıvı Basıncı (P=hdg, baraj duvarlarının alta doğru kalınlaşması)\n" +
                    "  ├── Gaz Basıncı (Toriçelli deneyi, Magdeburg küreleri, vantuzlar)\n" +
                    "  └── Uygulamalar (Hidrolik liftler, su cendereleri, damperli kamyonlar)",
            
            evdeDeney = "🧩 EVDE LİSANS LABORATUVARI:\n\n" +
                    "• Haşlanmış Yumurta ve Şişe Deneyi (Açık Hava Basıncı): Bir cam şişenin içine yanan bir kibrit parçası atın. Şişenin ağzına soyulmuş katı haşlanmış yumurtayı yerleştirin. Kibrit sönünce iç basınç düşecek ve dışarıdaki açık hava basıncı yumurtayı şişenin içine doğru itecektir. Açık hava basıncını gözlerinizle görün!"
        )
    }
    
    // 10. TÜRKÇE, ANLAMI, DİL BİLGİSİ
    if (lower.contains("sözcük") || lower.contains("cümle") || lower.contains("parça") || lower.contains("anlam") || lower.contains("ses bilgisi") || lower.contains("dil") || lower.contains("fiil") || lower.contains("edat") || lower.contains("yazım") || lower.contains("noktalama") || lower.contains("zamir") || lower.contains("isim") || lower.contains("sıfat") || lower.contains("metin")) {
        return AcademicContent(
            intro = "Kazanım: Sözcüklerin kazandığı mecaz ve gerçek anlamları ayırt eder, cümledeki örtülü anlatım bağlarını kurar, edebi parçanın ana fikir ve yapısını çözümler.\n\n" +
                    "Türkçe dersinin kalbi anlam bilgisinde atar! Dil, yaşayan ve gelişen bir organizmadır. Bir kelimenin cümledeki konumu, yanındaki kelimelerle kurduğu bağ, onun gerçek, yan veya tamamen soyutlaşarak mecaz anlam kazanmasını sağlar. Sınavlarda kelime dağarcığı ve okuma hızı başarının kilididir.",
            
            formulaSheet = "💡 TÜRKÇE VE ANLAM BİLGİSİ ÖNEMLİ KURALLARI:\n\n" +
                    "1. Kelimede Anlam Katmanları:\n" +
                    "• Gerçek Anlam: Akla gelen ilk, nesnel anlamdır. (Örn: Odunlar fırında yandı.)\n" +
                    "• Mecaz Anlam: Gerçek anlamdan uzak, soyut anlamdır. (Örn: Bu soğuk davranışlarıyla beni yaktı.)\n\n" +
                    "2. Cümle Türleri ve Anlam İlişkileri:\n" +
                    "• Neden-Sonuç: Gerekçe bildirir (Örn: Yağmur yağdığı için sırılsıklam olduk).\n" +
                    "• Amaç-Sonuç: Ulaşılmak istenen bir hedef vardır (Örn: Sınavı kazanmak amacıyla kütüphaneye gitti).\n\n" +
                    "3. Paragraf Yapı Kuralları:\n" +
                    "• Giriş Cümlesi: Kendinden önce bir açıklama gerektirmeyen, bağımsız cümlelerdir. 'Çünkü', 'Ancak' gibi bağlaçlarla başlayamaz.",
            
            questionBank = "📝 SÖZEL KAZANIM YAPRAK TESTİ:\n\n" +
                    "[Soru 1] 'Kuru' sözcüğü aşağıdakilerin hangisinde mecaz anlamda kullanılmıştır?\n" +
                    "A) Kuru otlar rüzgarda savruluyordu.\n" +
                    "B) Çamaşırlar nihayet kurumuş gibiydi.\n" +
                    "C) Bu kuru ve heyecansız hayat beni çok yordu.\n" +
                    "D) Kuru ekmekleri çorbaya doğradık.\n\n" +
                    "[Soru 2] Aşağıdaki cümlelerin hangisinde 'Öznel' bir yargı söz konusudur?\n" +
                    "A) Kitap toplam 12 bölümden ve 300 sayfadan oluşmaktadır.\n" +
                    "B) Yazarın bu son romanı okuyucunun ruhuna dokunan harika bir başyapıt.\n" +
                    "C) Türkiye Cumhuriyeti 29 Ekim 1923 yılında kurulmuştur.\n" +
                    "D) Sınavda Türkçe dersinden toplam 20 soru sorulmaktadır.\n\n" +
                    "[Soru 3] Aşağıdaki cümlelerin hangisinde amaç-sonuç ilişkisi vardır?\n" +
                    "A) Kar yağınca tüm köy yolları ulaşıma kapandı.\n" +
                    "B) Kitap okumak üzere sessizce odasına çekildi.\n" +
                    "C) Dişi ağrıdığından sabah erkenden doktora gitti.\n" +
                    "D) Geç saatte uyuduğu için sabah servisi kaçırdı.",
            
            solutionKey = "🔑 DOĞRU SEÇENEK ÇÖZÜMLERİ VE EDİTÖRYAL YORUMLAR:\n\n" +
                    "[Soru 1 Çözümü]: 'Kuru' sözcüğü A, B ve D şıklarında fiziksel olarak nemi olmayan, ıslak karşıtı anlamıyla gerçek anlamdadır. C şıkkındaki 'kuru hayat' ise tekdüze, sıkıcı anlamında mecazdır. Doğru Cevap: C.\n\n" +
                    "[Soru 2 Çözümü]: A, C ve D şıkları nesnel ve kanıtlanabilir bilimsel verilerdir. B şıkkındaki 'harika bir başyapıt' ifadesi kişisel bir beğenidir ve kanıtlanamaz (özneldir). Doğru Cevap: B.\n\n" +
                    "[Soru 3 Çözümü]: B şıkkında odasına çekilme eyleminin 'kitap okumak' gibi gerçekleşmemiş bir 'amacı' vardır. A, C ve D şıklarında ise neden-sonuç (eylemler gerçekleşmiştir) bağı vardır. Doğru Cevap: B.",
            
            studyTactics = "🚀 SÖZEL SINAVLARDA VE PARAGRAFTA REKOR HIZ TAKTİKLERİ:\n\n" +
                    "• Taktik 1: Paragraf sorularında önce soru kökünü ve şıkları oku! Ne aradığını bilerek paragrafa başlarsan süre yarı yarıya düşer.\n" +
                    "• Taktik 2: Altı çizili söz öbeği sorularında sadece altı çizili kısmı değil, cümlenin tamamını oku. Bağlam her zaman anlamı yönlendirir.\n" +
                    "• Taktik 3: 'Değildir', 'Ulaşılamaz', 'Yoktur' gibi olumsuz soru ifadelerinin altını kalınca çizerek oku, beynini uyar.",
            
            zihinHaritasi = "🧠 TÜRKÇE VE ANLAM YAPISI\n\n" +
                    "• Edebi Dil & Anlam\n" +
                    "  ├── Sözcükte Anlam (Gerçek, Mecaz, Yan, Terim, Eş/Zıt anlamlılıklar)\n" +
                    "  ├── Cümlede Anlam (Öznel, Nesnel, Neden-Sonuç, Koşul, Karşılaştırma)\n" +
                    "  ├── Paragraf Bilgisi (Ana düşünce, ana duygu, yardımcı düşünceler, başlık)\n" +
                    "  └── Dil Bilgisi (Sözcük türleri, ses olayları, yazım ve noktalama kuralları)",
            
            evdeDeney = "🧩 EVDE SÖZEL AKTİVİTE:\n\n" +
                    "• Kelime Avı ve Deyim Canlandırma: Ailenizle bir araya gelin. Bir kağıda en çok kullanılan 10 deyimi (göze girmek, kulak kabartmak vb.) yazın. Her bir üye seçtiği deyimi konuşmadan, sadece sessiz pandomim taklitleriyle anlatsın. Eğlenirken dilimizin sözel zenginliğini zihninize kazıyın!"
        )
    }
    
    // 11. TARİH, SOSYAL BİLGİLER, İNKILAP TARİHİ
    if (lower.contains("tarih") || lower.contains("sosyal") || lower.contains("inkılap") || lower.contains("atatürk") || lower.contains("savaş") || lower.contains("lozan") || lower.contains("mücadele") || lower.contains("genelge") || lower.contains("kongre") || lower.contains("uygarlık") || lower.contains("kültür") || lower.contains("birey") || lower.contains("yurttaş") || lower.contains("yönetim")) {
        return AcademicContent(
            intro = "Kazanım: Birinci Dünya Savaşı ve Milli Mücadele dönemindeki stratejik adımları sebep-sonuç bağlamında analiz eder. Atatürk İlkelerini ve inkılap tarihini kronolojik olarak yorumlar.\n\n" +
                    "Milli Mücadele ruhu Derslig'de! Tarih sadece geçmiş olaylar yığını değil, geleceğimizi inşa eden bir ders aynasıdır. Türk milletinin bağımsızlık aşkıyla Mustafa Kemal liderliğinde başlattığı Kurtuluş Savaşı, her bir cephesi ve belgesiyle egemenliğimizin tescillendiği kahramanlık destanıdır.",
            
            formulaSheet = "💡 TARİH CEP BROŞÜRÜ VE STRATEJİK BELGELER:\n\n" +
                    "1. Kurtuluş Savaşı Genelge ve Kongreler Haritası:\n" +
                    "• Havza Genelgesi: Milli Mücadele'nin ilk protesto ve miting çağrısıdır, uyanışı başlatmıştır.\n" +
                    "• Amasya Genelgesi: Kurtuluş Savaşı'nın amacı, gerekçesi ve yöntemi ilk kez ilan edilmiştir.\n" +
                    "• Erzurum Kongresi: Manda ve himaye ilk kez reddedilmiş, milli sınırlardan ilk kez bahsedilmiştir.\n" +
                    "• Sivas Kongresi: Cemiyetler tek çatı altında (Anadolu ve Rumeli Müdafaa-i Hukuk Cemiyeti) birleştirilmiştir.\n\n" +
                    "2. Diplomatik Başarılar:\n" +
                    "• Mudanya Ateşkes: Kurtuluş Savaşı'nın askeri safhası bitmiş, diplomatik safhası başlamıştır.\n" +
                    "• Lozan Barış Antlaşması: Yeni Türk devletinin bağımsızlığı tüm dünya tarafından hukuken kabul edilmiştir.",
            
            questionBank = "📝 İNKILAP TARİHİ YAPRAK TESTİK:\n\n" +
                    "[Soru 1] 'Milletin bağımsızlığını yine milletin azim ve kararı kurtaracaktır.' Amasya Genelgesi'ne ait bu madde Milli Mücadele'nin hangi yönünü ortaya koyar?\n" +
                    "A) Gerekçesini   B) Amacını ve Yöntemini   C) Süresini   D) Sınırlarını\n\n" +
                    "[Soru 2] Milli sınırların içinde vatan bir bütündür, bölünemez kararı İLK KEZ nerede alınmıştır?\n" +
                    "A) Havza Genelgesi   B) Amasya Genelgesi   C) Erzurum Kongresi   D) Lozan Antlaşması\n\n" +
                    "[Soru 3] Aşağıdaki ilkelerden hangisi doğrudan milli egemenlik ve halkın kendi kendini yönetmesi fikrine dayanmaktadır?\n" +
                    "A) Cumhuriyetçilik   B) Milliyetçilik   C) Laiklik   D) Devletçilik",
            
            solutionKey = "🔑 SÖZEL TARİH ANALİZİ VE AÇIKLAMALARI:\n\n" +
                    "[Soru 1 Çözümü]: Maddede yer alan 'bağımsızlığın kurtarılması' ifadesi 'amacı', 'milletin azim ve kararı ile kurtarılacağı' ise bu amaca ulaşma 'yöntemi'ni gösterir. Doğru Cevap: B.\n\n" +
                    "[Soru 2 Çözümü]: Erzurum Kongresi'nde vatan sınırları ilk kez resmi kongre kararı olarak 'Milli sınırlar içinde vatan bir bütündür, parçalanamaz' şeklinde tescillenmiştir. Doğru Cevap: C.\n\n" +
                    "[Soru 3 Çözümü]: Cumhuriyetçilik ilkesinin temelini ulusal irade, milli egemenlik, halkın seçme ve seçilme hakkı oluşturur. Dolayısıyla yönetim biçimi cumhuriyetçiliktir. Doğru Cevap: A.",
            
            studyTactics = "🚀 SINAVDA TARİH SORULARI FULLEME TAKTİKLERİ:\n\n" +
                    "• Taktik 1: Tarihte 'bağımsızlık' (dış güçlere boyun eğmeme, egemen devlet olma) ile 'milli egemenlik' (iç yönetimde kararı milletin vermesi, oy hakkı, TBMM) kavramlarını birbirine karıştırma.\n" +
                    "• Taktik 2: Paragraflı sözel sorularda 'yalnızca verilen metne göre' ibaresine aşırı dikkat et. Kendi genel tarih bilgini metnin sınırları dışına taşırma.\n" +
                    "• Taktik 3: Mondros Ateşkes Antlaşması'nın ünlü 7. maddesinin (Güvenliği tehdit eden stratejik bir noktayı işgal hakkı) İtilaf devletlerinin işgalleri meşrulaştırma çabası olduğunu adın gibi bil.",
            
            zihinHaritasi = "🧠 MİLLİ MÜCADELE MATRİSİ\n\n" +
                    "• Bağımsızlık Yolculuğu\n" +
                    "  ├── Genelgeler Cephesi (Havza ve Amasya ile ihtilal çağrısı)\n" +
                    "  ├── Doğu/Güney/Batı Cepheleri (Gümrü, Londra Konferansı, Sakarya Savaşları)\n" +
                    "  ├── TBMM'nin Açılışı (23 Nisan 1920, Millet iradesinin kalbi)\n" +
                    "  └── Atatürk İnkılapları (Siyasi, toplumsal, hukuksal ve eğitimsel reformlar)",
            
            evdeDeney = "🧩 EVDE TARİH KÜLTÜRÜ:\n\n" +
                    "• Aile Müzesi Kurma: Evinizdeki eski saatleri, eski madeni paraları, eski fotoğrafları, mektupları toplayın. Bunları bir masanın üzerine kronolojik olarak dizerek ailenizin küçük müzesini tasarlayın ve tarihi objelerin geçmişle günümüz arasındaki köprü rolünü tartışın."
        )
    }
    
    // FALLBACK GENERATOR (DÜZENLİ DİNAMİK YAPAY ZEKA MODELLEMESİ)
    return AcademicContent(
        intro = "Kazanım: $title konusunun temel kavramlarını ve akademik ilkelerini MEB müfredatı çerçevesinde öğrenir.\n\n" +
                "$title konusu, $course müfredatının en temel kazanımlarından birini barındırır. Bu çalışmada, konuya dair terminoloji, kavramlar arası ilişkiler ve sınavlarda çıkabilecek soru şemaları ayrıntılı olarak sunulmaktadır.\n\n" +
                "Başarılı olmak için her gün en az 15 dakika bu fasikülü tekrar ediniz ve derslig pekiştirme testlerini tamamlayınız.",
        
        formulaSheet = "💡 $title DERS NOTU VE FORMÜLLERİ:\n\n" +
                "1. Temel İlke:\n" +
                "• $title çalışırken ilk adımda kavramların kelime kökenlerini ve tanımlarını zihninde kalıcı hale getir.\n\n" +
                "2. Stratejik Formüller:\n" +
                "• Değişkenlerin birbiriyle olan oran ve dengesi daima bütünü oluşturur.\n" +
                "• Sorunun can alıcı noktalarında yer alan verileri şifreleyerek çalışmak hafızayı destekler.",
        
        questionBank = "📝 $title KAZANIM YAPRAK TESTİ:\n\n" +
                "[Soru 1] Yukarıda öğrendiğimiz $title konusuyla ilgili en temel ve doğru yargı hangisidir?\n" +
                "A) Konuyu öğrenmek sadece zaman kaybıdır.\n" +
                "B) Bu konu MEB sınavlarında yer alan en değerli kazanımlardan biridir.\n" +
                "C) Ezber yapmak konunun mantığını kavramaktan iyidir.\n" +
                "D) Hiçbiri\n\n" +
                "[Soru 2] Derslig platformundaki bu $title dokümanlarını pekiştirmek için hangisini yapmak en etkilidir?\n" +
                "A) Hemen platformdaki yaprak testleri ve pekiştirme testlerini tamamlamak\n" +
                "B) Dersleri hiç tekrar etmeden kapamak\n" +
                "C) Formülleri tamamen ezberleyip soru çözmemek\n" +
                "D) Sadece son gün sabahlayarak çalışmak",
        
        solutionKey = "🔑 HIZLI YORUMLU CEVAP ANAHTARI VE ÇÖZÜMLER:\n\n" +
                "[Soru 1 Çözümü]: $title konusu MEB ders kazanımları müfredatı ile %100 uyumludur ve okul derslerimizdeki başarıya doğrudan etki eder. Doğru Cevap: B.\n\n" +
                "[Soru 2 Çözümü]: Bir konuyu teorik olarak öğrendikten sonra hemen pekiştirme testleri ve lig mücadeleleri ile pekiştirmek en etkili kalıcı öğrenme yoludur. Doğru Cevap: A.",
        
        studyTactics = "🚀 $title DERSİNDE BAŞARILI OLMANIN YOLLARI:\n\n" +
                "• Taktik 1: Konuyu küçük parçalara (ünitelere) bölerek çalış, bütünü yavaş yavaş inşa et.\n" +
                "• Taktik 2: Yanlış yaptığın her soruda moral bozma; yanlışlar, eksik olduğun kazanımı gösteren en dürüst kılavuzlardır.\n" +
                "• Taktik 3: Derslig Yapay Zeka Öğretmeninden (AI Tutor) her gün bu konu hakkında 2 adet soru talep et.",
        
        zihinHaritasi = "🧠 ZİHİN HARİTASI VE DETAYLARI\n\n" +
                "• $title\n" +
                "  ├── Temel Tanımlar ve Sözlük\n" +
                "  ├── Formüller ve Temel Yasalar\n" +
                "  ├── Taktikler ve Sınav Sorusu İpuçları\n" +
                "  └── Ev Evde Yapılabilecek Pratik Canlandırmalar",
        
        evdeDeney = "🧩 EVDE ETKİNLİK PLANI:\n\n" +
                "• Konu Canlandırma Oyunu: Evde ailenize veya bir arkadaşınıza bugün derslig üzerinde çalıştığınız $title konusunu 3 dakika boyunca kendi kelimelerinizle, takılmadan anlatmayı deneyin. Fikirlerinizi sesli olarak paylaştıkça konunun ne kadar pratikleştiğini göreceksiniz!"
    )
}

fun generateMaterialsForTopic(topic: TopicDetail, courseName: String): List<EducationalMaterial> {
    val title = topic.title
    val academic = getAcademicContent(title, courseName)
    
    return listOf(
        EducationalMaterial(
            id = "${topic.id}_m1",
            title = "Birebir Konu Anlatım Videosu 🎥",
            type = "VIDEO",
            description = "$courseName dersi $title konusu için hazırlanmış derslig video anlatımı.",
            iconName = "play_circle",
            videoUrl = "https://player.vimeo.com/video/900107538?autoplay=1"
        ),
        EducationalMaterial(
            id = "${topic.id}_m2",
            title = "Konu Özet Slayt Kartları ⚡",
            type = "SLIDES",
            description = "İnteraktif slayt kartları ile konuyu hızlı adımlarla kavrayın.",
            iconName = "slideshow"
        ),
        EducationalMaterial(
            id = "${topic.id}_m3",
            title = "MEB Müfredat Konu Kitabı 📘 (PDF)",
            type = "PDF",
            description = "Milli Eğitim Bakanlığı müfredatı ile %100 uyumlu, formüllü konu fasikülü.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- SAYFA 1: GİRİŞ VE TEMEL TANIMLAR ---\n\n$courseName dersi $title konusu akademik konu notları:\n\n${academic.intro}",
                "--- SAYFA 2: FORMÜLLER VE İLİŞKİLER ---\n\n${academic.formulaSheet}",
                "--- SAYFA 3: KAZANIM YAPRAK TESTİ ---\n\n${academic.questionBank}",
                "--- SAYFA 4: MEB SINAV ÇIKMIŞ SORU ANALİZİ ---\n\n${academic.studyTactics}",
                "--- SAYFA 5: BİTİRİŞ VE ÖDEV GÖREVLERİ ---\n\n📅 Günlük Ödev Listesi - $title:\n\n• Sitemizdeki veya derslig içindeki pekiştirme testlerini çözün.\n• Derslig AI Öğretmenine bu konudan 2 zor soru sorun ve cevapları defterinize not dökün.\n• Lig Sıralamasında yükselerek Altın ve Şampiyonlar ligine girmek için konuyu pekiştirin."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m4",
            title = "Formüller ve Kritik Kurallar Broşürü 🌟 (PDF)",
            type = "PDF",
            description = "Tüm can alıcı formülleri ve stratejik kuralları tek sayfada toplayan başucu rehberi.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- FORMÜL VE KURAL BROŞÜRÜ ---\n\n$title Konusunda Başarılı Olmanın 3 Temel Kuralı:\n\n1. Asla Ezberleme, Mantığını Öğren!\n2. Soru Kökündeki 'Değildir', 'Olamaz' gibi ifadelere azami dikkat göster!\n3. Formülleri renkli kalemlerle küçük kartlara yazıp çalışma masana yapıştır.",
                "--- FORMÜL VE KANUN MATRİSİ ---\n\n${academic.formulaSheet}"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m5",
            title = "Yeni Nesil Soru Çözüm Taktikleri 🚀 (PDF)",
            type = "PDF",
            description = "Beceri temelli yeni nesil uzun soruları saniyeler içinde çözmenizi sağlayacak taktikler.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- MEB SINAV TAKTİK BROŞÜRÜ ---\n\n${academic.studyTactics}",
                "--- GERÇEK SENARYO UYGULAMASI ---\n\n* $title yeni nesil sorularda sıkça kullanılan senaryolar:\n- Günlük yaşamda alışveriş, mühendislik veya doğa olayları üzerinden kurgulanan sorular.\n- Tablo okuma ve grafik yorumlama odaklı görsel modeller."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m6",
            title = "Yaprak Test Soru Kitapçığı 📝 (PDF)",
            type = "PDF",
            description = "Seviyenizi ölçecek ve derslig sınavlarına hazırlayacak yaprak test soruları.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- YAPRAK TEST - SEÇKİN SORULAR ---\n\n${academic.questionBank}",
                "--- YAPRAK TEST - SAYFA 2 ---\n\n• Soru Çözerken dikkat sürecinizi artırmak için Derslig AI Öğretmeninden (AI Tutor) anında ipucu talep edebilirsiniz!"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m7",
            title = "Detaylı Soru Çözümleri Dokümanı 📑 (PDF)",
            type = "PDF",
            description = "Yaprak testteki ve sınavlardaki tüm soruların adım adım, açıklayıcı video tadında çözümleri.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- DETAYLI ÖZGÜN ÇÖZÜMLER VE ANALİZLER ---\n\n${academic.solutionKey}",
                "[Soru Analiz Yorumu]: Doğru seçeneklerin arkasındaki zihinsel süreç, soruda verilen öncüllerin tek tek formüller ile eşleştirilmesiyle ortaya çıkmaktadır."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m8",
            title = "Öğrenci Çalışma ve Hedef Günlüğü 🏆 (PDF)",
            type = "PDF",
            description = "$title konusu boyunca eksiklerinizi takip edip hedefler koyabileceğiniz pratik ajanda.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- HEDEF DEFTERİ VE KAZANIM PLANI ---\n\n• Haftalık Hedef: $title konusuna ait toplam 3 özgün testi sıfır hata ile tamamla.\n• Eksikleri Kapatma: Yanlış yaptığın her sorunun doğru çözümünü Derslig AI Öğretmenine sorup öğren.\n• Lig Derecesi: Haftalık lig yarışında ilk 3'e girerek ünvanları topla!"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m9",
            title = "Derslig Zihin Haritası ve Kavram Şeması 🧠 (PDF)",
            type = "PDF",
            description = "Bilgileri görselleştirerek beynin sağ lobunu aktive eden ve ezberi engelleyen harika şema.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- ZİHİN HARİTASI VE KAVRAM ŞABLONU ---\n\n${academic.zihinHaritasi}"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m10",
            title = "Müfredat Yaprak Testi - Temel Seviye A 📄 (PDF)",
            type = "PDF",
            description = "Okul yazılılarında ve temel sınavlarda çıkabilecek kolay seviye ısındırma test kağıdı.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- SEVİYE A YAPRAK TESTİ (KOLAY SEVİYE) ---\n\n${academic.questionBank}\n\n*İpucu: Sayfanın altındaki özet pratik bilgilere göz atarak soruları hızla çözebilirsin!"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m11",
            title = "Müfredat Yaprak Testi - İleri Seviye B 📄 (PDF)",
            type = "PDF",
            description = "LGS ve YKS gibi seçici sınavlarda derece yapmak isteyenlerin çözmesi gereken yeni nesil test.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- SEVİYE B İLERİ DÜZEY TEST ---\n\n${academic.questionBank}\n\n[Soru Analiz]: Yukarıdaki metne ve tabloya göre $title ile ilgili parametrelerin analizi yapıldığında hangi çıkarıma kesin olarak ulaşılamaz?"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m12",
            title = "LGS & YKS Çıkmış Sorular Kitapçığı 🎯 (PDF)",
            type = "PDF",
            description = "Son 10 yılda ÖSYM ve MEB tarafından bu konu hakkında sorulmuş orijinal sorular.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- SINAV ARŞİVİ VE ORİJİNAL ÇIKAN SORULAR ---\n\n${academic.questionBank}\n\n• MEB Sınavı Çıkmış Sorusu Analiz Yorumu: 'Aşağıdakilerden hangisi $title kavramının hayatımızdaki doğrudan yansımasıdır?' sorgulanmıştır."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m13",
            title = "Evde Eğlenceli Deney ve Etkinlik Kılavuzu 🧩 (PDF)",
            type = "PDF",
            description = "Ailenizle veya arkadaşlarınızla evde yapabileceğiniz pratik deney, drama ve pekiştirme oyunları.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- EVDE EĞLENCELİ ETKİNLİK PLANI ---\n\n${academic.evdeDeney}"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m14",
            title = "Sık Yapılan Hatalar ve Kaçınma Rehberi 💡 (PDF)",
            type = "PDF",
            description = "Öğrencilerin sınavlarda en çok düştüğü yanılgılar ve bu hataları engelleme kılavuzu.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- DİKKAT ENGELLERİ VE SIK YAPILAN HATALAR ---\n\n🛑 Sık Yapılan Hata: $title konusunda soruda yer alan gizli olumsuzluk ifadelerini gözden kaçırmak.\n✅ Çözüm: Soru kökünü okurken her zaman kalemle altını çizerek oku.\n\n🛑 Sık Yapılan Hata: İşlem önceliğini dikkate almadan doğrudan soldan sağa işlem yapmak.\n\n${academic.studyTactics}"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m15",
            title = "Bilişsel Akıl Oyunları ve Dikkat Egzersizi 🧠 (PDF)",
            type = "PDF",
            description = "$title çalışırken konsantrasyonunuzu 3 katına çıkaracak beyin egzersizleri.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- BİLİŞSEL EGZERSİZ SÜRECİ ---\n\n• Egzersiz 1: Konuyu çalışmaya başlamadan önce 1 dakika boyunca gözlerini kapat ve derin derin nefes al.\n• Egzersiz 2: $title kelimesini tersten yazıp kodlayarak zihnindeki odaklanma merkezlerini tetikle."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m16",
            title = "Eksik Analiz Bilgi ve Kontrol Fişi 🎯 (PDF)",
            type = "PDF",
            description = "Hangi kazanımlarda eksiğiniz olduğunu tespit ederek nokta atışı düzeltme yapmanızı sağlayan form.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- EKSİK ANALİZ FORMU ---\n\n• Kazanım 1: $title ile ilgili tanımları biliyorum. [Evet / Kısmen / Hayır]\n• Kazanım 2: Çözümlü soruları tek başıma çözebiliyorum. [Evet / Kısmen / Hayır]\n• Kazanım 3: Yeni nesil mantık sorularında hızımdan memnunum. [Evet / Kısmen / Hayır]"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m17",
            title = "Haftalık Sınav & Soru Takip Kartı 📅 (PDF)",
            type = "PDF",
            description = "Hangi gün kaç soru çözdüğünüzü ve başarı yüzdenizi gösteren şık grafik şablonu.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- SORU TAKİP TABLOSU ---\n\n📅 Günlük Çetele:\n- Pazartesi: ____ Soru (%____ Doğruluk) [$title]\n- Salı: ____ Soru (%____ Doğruluk) [$title]\n- Çarşamba: ____ Soru (%____ Doğruluk) [$title]\n- Perşembe: ____ Soru (%____ Doğruluk) [$title]\n- Cuma: ____ Soru (%____ Doğruluk) [$title]\n- Hafta Sonu: Toplam ____ Soru Çözüldü"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m18",
            title = "Sınav Motivasyon Ders Notu Kitapçığı 💪 (PDF)",
            type = "PDF",
            description = "Stresi kontrol altına alan, sınav sabahı yapılması gerekenleri özetleyen mentorluk kitapçığı.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- MOTİVASYON KAMPÜSÜ ---\n\n💪 Şampiyon, kendine inan!\n\n$title konusundan çıkabilecek hiçbir soru senin azminden daha büyük olamaz. Unutma, derslig üzerindeki düzenli çalışmaların seni şampiyonlar ligine taşıyacaktır. Her gün bir adım daha ileri gidiyorsun, başarı seninle!"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m19",
            title = "Veli Rehberlik ve Gelişim Broşürü 👨‍👩‍👦 (PDF)",
            type = "PDF",
            description = "Velinizin Derslig eğitim sürecinizi takip etmesini ve size doğru şekilde destek olmasını sağlayan rehber.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- VELİ BİLEN REHBERİ ---\n\nSaygıdeğer Veli,\n\nÖğrencimizin $title konusunu tam olarak öğrenebilmesi için evde sakin bir çalışma ortamı hazırlamanız ve günlük derslig çalışmalarından sonra kazandığı XP puanları için onu tebrik etmeniz motivasyonunu inanılmaz derecede artıracaktır."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m20",
            title = "Ders Çalışırken Odaklanma Teknikleri 🧘 (PDF)",
            type = "PDF",
            description = "Pomodoro ve Feynman metotları ile ders çalışma sürenizi en verimli şekilde kullanma rehberi.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- ODAKLANMA REHBERİ ---\n\n• Pomodoro Tekniği: 25 Dakika kesintisiz $title çalış, 5 dakika hak edilmiş mola ver. Bu döngüyü 4 kez tekrarla.\n• Feynman Tekniği: Konuyu hiç bilmeyen bir çocuğa anlatıyormuş gibi basit kelimelerle kendi kendine sesli anlat."
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m21",
            title = "Yıl Sonu Genel Başarı Fasikülü 📔 (PDF)",
            type = "PDF",
            description = "Tüm senenin $title kazanımlarını tek bir çatı altında özetleyen dev arşiv dosya.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- MASTER ARŞİV DOKÜMANI ---\n\nBu doküman, sene sonu tekrar kampı için $title konusunun tüm kritik detaylarını tek sayfada özetler. Sınavdan önceki hafta bu son sayfayı okuyarak hafızanı tazeleyebilirsin şampiyon!"
            )
        ),
        EducationalMaterial(
            id = "${topic.id}_m22",
            title = "Derslig Prova Deneme Sınav Sayfası 🎓 (PDF)",
            type = "PDF",
            description = "Gerçek LGS/YKS sınav ortamını simüle eden, süreli prova denemesi.",
            iconName = "picture_as_pdf",
            contentPages = listOf(
                "--- PROVA DENEME KATALOĞU ---\n\nSüre: 40 Dakika • Soru Sayısı: 20 Soru\n\nBu deneme kitapçığı, derslig uzman kadrosu tarafından $title konusundan gelebilecek her türlü varyasyonu test etmek üzere hazırlanmıştır. Şimdi süre tutarak çözmeye başlayabilirsin!"
            )
        )
    )
}

data class ChatMessage(
    val sender: String, // "STUDENT" or "TEACHER"
    val text: String,
    val timeMillis: Long = System.currentTimeMillis()
)

data class LeagueCompetitor(
    val rank: Int,
    val name: String,
    val school: String,
    val xp: Int,
    val isCurrentUser: Boolean = false,
    val avatarColorHex: String = "#FF9800",
    val avatarBorder: String = "None"
)

class DersligViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DersligDatabase.getDatabase(application, viewModelScope)
    val repository = DersligRepository(db.dersligDao())

    // UI Navigation State
    private val _currentScreen = MutableStateFlow(DersligScreen.HOME)
    val currentScreen: StateFlow<DersligScreen> = _currentScreen.asStateFlow()

    // Selected states for learning detail flow
    private val _selectedCourse = MutableStateFlow<AppCourse?>(null)
    val selectedCourse: StateFlow<AppCourse?> = _selectedCourse.asStateFlow()

    private val _selectedTopic = MutableStateFlow<TopicDetail?>(null)
    val selectedTopic: StateFlow<TopicDetail?> = _selectedTopic.asStateFlow()

    private val _selectedMaterial = MutableStateFlow<EducationalMaterial?>(null)
    val selectedMaterial: StateFlow<EducationalMaterial?> = _selectedMaterial.asStateFlow()

    private val _topicMaterials = MutableStateFlow<List<EducationalMaterial>>(emptyList())
    val topicMaterials: StateFlow<List<EducationalMaterial>> = _topicMaterials.asStateFlow()

    private val _isLoadingMaterials = MutableStateFlow(false)
    val isLoadingMaterials: StateFlow<Boolean> = _isLoadingMaterials.asStateFlow()

    private val _currentAcademicContent = MutableStateFlow<AcademicContent?>(null)
    val currentAcademicContent: StateFlow<AcademicContent?> = _currentAcademicContent.asStateFlow()

    // Slide state or current card index
    private val _currentSlideIndex = MutableStateFlow(0)
    val currentSlideIndex: StateFlow<Int> = _currentSlideIndex.asStateFlow()

    // Live Database States
    val userStatsState: StateFlow<UserStats?> = repository.userStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val completedQuizzesState: StateFlow<List<CompletedQuiz>> = repository.completedQuizzes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val shopItemsState: StateFlow<List<ShopItem>> = repository.shopItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active Quiz State
    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow(-1)
    val selectedAnswerIndex: StateFlow<Int> = _selectedAnswerIndex.asStateFlow()

    private val _isQuestionChecked = MutableStateFlow(false)
    val isQuestionChecked: StateFlow<Boolean> = _isQuestionChecked.asStateFlow()

    private val _quizCorrectCount = MutableStateFlow(0)
    val quizCorrectCount: StateFlow<Int> = _quizCorrectCount.asStateFlow()

    private val _quizWrongCount = MutableStateFlow(0)
    val quizWrongCount: StateFlow<Int> = _quizWrongCount.asStateFlow()

    // Just completed quiz results holder
    private val _lastQuizResult = MutableStateFlow<Pair<Int, Int>?>(null) // Correct, Wrong
    val lastQuizResult: StateFlow<Pair<Int, Int>?> = _lastQuizResult.asStateFlow()

    // AI Tutor Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("TEACHER", "Merhaba Şampiyon! Derslig Yapay Zeka Öğretmenine hoş geldin. Zorlandığın her konuyu bana sorabilirsin! Örneğin: 'ebob ve ekok farkı nedir?'")
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _tutorErrorMsg = MutableStateFlow<String?>(null)
    val tutorErrorMsg: StateFlow<String?> = _tutorErrorMsg.asStateFlow()

    // Navigation Methods
    fun navigateTo(screen: DersligScreen) {
        _currentScreen.value = screen
    }

    fun selectCourseAndNavigate(course: AppCourse) {
        _selectedCourse.value = course
        navigateTo(DersligScreen.COURSE_DETAIL)
    }

    fun selectTopicAndStartLecture(topic: TopicDetail) {
        _selectedTopic.value = topic
        _currentSlideIndex.value = 0
        val courseName = _selectedCourse.value?.name ?: "Eğitim"
        _currentAcademicContent.value = getAcademicContent(topic.title, courseName)
        navigateTo(DersligScreen.LITERATURE_SLIDES)
        loadMaterialsForSelectedTopic()
    }

    fun selectTopicAndGoToHub(topic: TopicDetail) {
        _selectedTopic.value = topic
        val courseName = _selectedCourse.value?.name ?: "Eğitim"
        _currentAcademicContent.value = getAcademicContent(topic.title, courseName)
        navigateTo(DersligScreen.TOPIC_HUB)
        loadMaterialsForSelectedTopic()
    }

    fun getSlidesForSelectedTopic(): List<String> {
        val topic = _selectedTopic.value ?: return emptyList()
        val courseName = _selectedCourse.value?.name ?: "Eğitim"
        val academic = _currentAcademicContent.value ?: getAcademicContent(topic.title, courseName)
        
        return listOf(
            "📖 **${topic.title} - GEÇERLİ KONU ANLATIMI**\n\n${academic.intro}",
            "💡 **FORMÜLLER VE KRİTİK KURALLAR**\n\n${academic.formulaSheet}",
            "🧠 **ZİHİN HARİTASI VE KAVRAM İLİŞKİLERİ**\n\n${academic.zihinHaritasi}",
            "🚀 **SINAVLARDA EN ÇOK ÇIKAN TAKTİKLER**\n\n${academic.studyTactics}",
            "🧩 **EVDE PEKİŞTİRİCİ ETKİNLİK PLANI**\n\n${academic.evdeDeney}"
        )
    }

    fun loadMaterialsForSelectedTopic() {
        val topic = _selectedTopic.value ?: return
        val courseName = _selectedCourse.value?.name ?: "Eğitim"
        val gradeName = "Müfredat"
        
        _isLoadingMaterials.value = true
        _topicMaterials.value = emptyList()

        viewModelScope.launch {
            try {
                val aiAcademic = GeminiService.generateAcademicContent(topic.title, courseName, gradeName)
                if (aiAcademic != null) {
                    _currentAcademicContent.value = aiAcademic
                    val list = listOf(
                        EducationalMaterial(
                            id = "${topic.id}_m1",
                            title = "Birebir Konu Anlatım Videosu 🎥",
                            type = "VIDEO",
                            description = "$courseName dersi ${topic.title} konusu için hazırlanmış derslig video anlatımı.",
                            iconName = "play_circle",
                            videoUrl = "https://player.vimeo.com/video/900107538?autoplay=1"
                        ),
                        EducationalMaterial(
                            id = "${topic.id}_m2",
                            title = "Konu Özet Slayt Kartları ⚡",
                            type = "SLIDES",
                            description = "İnteraktif Slayt Kartları ile konuyu hızlı adımlarla kavrayın.",
                            iconName = "slideshow"
                        ),
                        EducationalMaterial(
                            id = "${topic.id}_m3",
                            title = "Yardımcı Ders Kitabı 📘 (PDF)",
                            type = "PDF",
                            description = "Milli Eğitim Bakanlığı müfredatı ile %100 uyumlu, formüllü konu fasikülü.",
                            iconName = "picture_as_pdf",
                            contentPages = listOf(
                                "--- SAYFA 1: GİRİŞ VE TEMEL TANIMLAR ---\n\n$courseName dersi ${topic.title} konusu akademik konu notları:\n\n${aiAcademic.intro}",
                                "--- SAYFA 2: FORMÜLLER VE İLİŞKİLER ---\n\n${aiAcademic.formulaSheet}",
                                "--- SAYFA 3: KAZANIM YAPRAK TESTİ ---\n\n${aiAcademic.questionBank}",
                                "--- SAYFA 4: MEB SINAV ÇIKMIŞ SORU ANALİZİ ---\n\n${aiAcademic.studyTactics}",
                                "--- SAYFA 5: BİTİRİŞ VE ÖDEV GÖREVLERİ ---\n\n📅 Günlük Ödev Listesi - ${topic.title}:\n\n• Sitemizdeki veya derslig içindeki pekiştirme testlerini çözün.\n• Derslig AI Öğretmenine bu konudan 2 zor soru sorun ve cevapları defterinize not dökün.\n• Lig Sıralamasında yükselerek Altın ve Şampiyonlar ligine girmek için konuyu pekiştirin."
                            )
                        ),
                        EducationalMaterial(
                            id = "${topic.id}_m4",
                            title = "Formüller ve Kritik Kurallar Broşürü 🌟 (PDF)",
                            type = "PDF",
                            description = "${topic.title} konusuna ait zihin haritası, pratik formüller ve ezberleme kuralları.",
                            iconName = "picture_as_pdf",
                            contentPages = listOf(
                                "--- SAYFA 1: FORMÜLLER VE KURALLAR ---\n\n${aiAcademic.formulaSheet}",
                                "--- SAYFA 2: ZİHİN HARİTASI ---\n\n${aiAcademic.zihinHaritasi}",
                                "--- SAYFA 3: EVDE ETKİNLİK PLANI ---\n\n${aiAcademic.evdeDeney}"
                            )
                        ),
                        EducationalMaterial(
                            id = "${topic.id}_m5",
                            title = "MEB Ders Kitabı Soruları ve Gerekçeli Çözümleri 📝 (PDF)",
                            type = "PDF",
                            description = "Ünite sonlarında karşına çıkabilecek soruların ve çözümlerinin yer aldığı yaprak test.",
                            iconName = "picture_as_pdf",
                            contentPages = listOf(
                                "--- SAYFA 1: YAPRAK TEST SORULARI ---\n\n${aiAcademic.questionBank}",
                                "--- SAYFA 2: DETAYLI SÖZEL ÇÖZÜM ANAHTARI ---\n\n${aiAcademic.solutionKey}"
                            )
                        )
                    )
                    _topicMaterials.value = list
                } else {
                    _topicMaterials.value = generateMaterialsForTopic(topic, courseName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _topicMaterials.value = generateMaterialsForTopic(topic, courseName)
            } finally {
                _isLoadingMaterials.value = false
            }
        }
    }

    fun selectMaterial(material: EducationalMaterial) {
        _selectedMaterial.value = material
        when (material.type) {
            "VIDEO" -> navigateTo(DersligScreen.VIDEO_VIEWER)
            "SLIDES" -> {
                _currentSlideIndex.value = 0
                navigateTo(DersligScreen.LITERATURE_SLIDES)
            }
            "PDF" -> navigateTo(DersligScreen.PDF_VIEWER)
        }
    }

    fun nextSlide() {
        val slidesCount = getSlidesForSelectedTopic().size
        if (_currentSlideIndex.value < slidesCount - 1) {
            _currentSlideIndex.value += 1
        } else {
            // Done with lecture slides, start the quiz!
            _quizQuestionIndex.value = 0
            _selectedAnswerIndex.value = -1
            _isQuestionChecked.value = false
            _quizCorrectCount.value = 0
            _quizWrongCount.value = 0
            navigateTo(DersligScreen.ACTIVE_QUIZ)
        }
    }

    fun prevSlide() {
        if (_currentSlideIndex.value > 0) {
            _currentSlideIndex.value -= 1
        }
    }

    // Active Quiz Logic
    fun selectAnswer(index: Int) {
        if (!_isQuestionChecked.value) {
            _selectedAnswerIndex.value = index
        }
    }

    fun checkAnswer() {
        val topic = _selectedTopic.value ?: return
        val currentQuestion = topic.questions.getOrNull(_quizQuestionIndex.value) ?: return
        if (_selectedAnswerIndex.value == -1 || _isQuestionChecked.value) return

        _isQuestionChecked.value = true
        if (_selectedAnswerIndex.value == currentQuestion.correctAnswerIndex) {
            _quizCorrectCount.value += 1
        } else {
            _quizWrongCount.value += 1
        }
    }

    fun nextQuestion() {
        val topic = _selectedTopic.value ?: return
        if (_quizQuestionIndex.value < topic.questions.size - 1) {
            _quizQuestionIndex.value += 1
            _selectedAnswerIndex.value = -1
            _isQuestionChecked.value = false
        } else {
            // Quiz Finished! Persist results
            val totalQuestions = topic.questions.size
            val scorePercent = (quizCorrectCount.value.toFloat() / totalQuestions * 100).toInt()
            
            // 50 XP and 10 Coins per correct answer, 10 XP per wrong as a consolation prize
            val xpEarned = (_quizCorrectCount.value * 50) + (_quizWrongCount.value * 10)
            val coinsEarned = _quizCorrectCount.value * 10

            viewModelScope.launch {
                repository.completeQuiz(
                    quizId = topic.id,
                    correctCount = _quizCorrectCount.value,
                    wrongCount = _quizWrongCount.value,
                    scorePercent = scorePercent,
                    xpEarned = xpEarned,
                    coinsEarned = coinsEarned
                )
                
                // Track results for rendering
                _lastQuizResult.value = Pair(_quizCorrectCount.value, _quizWrongCount.value)
                navigateTo(DersligScreen.QUIZ_RESULT)
            }
        }
    }

    // Store Purchases
    fun buyItem(item: ShopItem) {
        viewModelScope.launch {
            val stats = userStatsState.value ?: return@launch
            repository.purchaseShopItem(item, stats)
        }
    }

    fun changeUserGrade(grade: String) {
        viewModelScope.launch {
            repository.changeGrade(grade)
        }
    }

    fun changeUsername(name: String) {
        viewModelScope.launch {
            repository.updateUsername(name)
        }
    }

    // Profile Customization
    fun changeProfileBorder(itemTitle: String) {
        viewModelScope.launch {
            repository.selectBorder(itemTitle)
        }
    }

    // AI Tutor Flow
    fun sendQuestionToTutor(questionText: String) {
        if (questionText.trim().isEmpty()) return
        
        // Add student message to list
        val updatedList = _chatMessages.value.toMutableList()
        updatedList.add(ChatMessage("STUDENT", questionText))
        _chatMessages.value = updatedList
        _isChatLoading.value = true
        _tutorErrorMsg.value = null

        val stats = userStatsState.value
        val grade = stats?.selectedGrade ?: "8. Sınıf"
        val activeCourseName = _selectedCourse.value?.name

        viewModelScope.launch {
            val response = GeminiService.askTutor(questionText, activeCourseName, grade)
            _isChatLoading.value = false
            
            val newListWithResponse = _chatMessages.value.toMutableList()
            newListWithResponse.add(ChatMessage("TEACHER", response))
            _chatMessages.value = newListWithResponse
        }
    }

    // Dynamic League calculations
    fun getLeagues() = listOf(
        "Bronz Lig Ligi" to "#CD7F32",
        "Gümüş Lig Ligi" to "#C0C0C0",
        "Altın Lig Ligi" to "#FFD700",
        "Platin Lig Ligi" to "#E5E4E2",
        "Şampiyonlar Ligi" to "#1E88E5"
    )

    fun getUserLeague(xp: Int): String {
        return when {
            xp < 250 -> "Bronz Lig"
            xp < 750 -> "Gümüş Lig"
            xp < 1500 -> "Altın Lig"
            xp < 2800 -> "Platin Lig"
            else -> "Şampiyonlar Ligi"
        }
    }

    fun getLeagueColor(leagueName: String): String {
        return when(leagueName) {
            "Bronz Lig" -> "#CD7F32"
            "Gümüş Lig" -> "#9E9E9E"
            "Altın Lig" -> "#FFB300"
            "Platin Lig" -> "#00ACC1"
            "Şampiyonlar Ligi" -> "#3949AB"
            else -> "#757575"
        }
    }

    fun getLeagueCompetitors(userXp: Int): List<LeagueCompetitor> {
        val userLeague = getUserLeague(userXp)
        val stats = userStatsState.value
        
        // Competitors centered around user's score to make a competitive weekly race
        val seedCompetitors = listOf(
            Triple("Ayhan Kaya", "Atatürk Ortaokulu", 1.4f),
            Triple("Melis Öztürk", "Bahçeşehir Koleji", 1.25f),
            Triple("Burak Şahin", "Galatasaray Ortaokulu", 1.15f),
            Triple("Zeynep Yıldız", "Esenyurt İÖO", 0.95f),
            Triple("Kerem Yılmaz", "İzmir Özel Koleji", 0.85f),
            Triple("Eda Aksoy", "Cumhuriyet Okulu", 0.70f),
            Triple("Emre Çetin", "TED Ankara Koleji", 0.55f),
            Triple("Selin Demir", "Karaköy Ortaokulu", 0.40f)
        )

        // Maps relative multiplier based on user's current league or baseline points
        val leagueMultiplier = when(userLeague) {
            "Bronz Lig" -> 150
            "Gümüş Lig" -> 500
            "Altın Lig" -> 1100
            "Platin Lig" -> 2100
            else -> 3200
        }

        val list = mutableListOf<LeagueCompetitor>()
        
        // Add User
        list.add(
            LeagueCompetitor(
                rank = -1, // Sort will fix
                name = stats?.username ?: "Süper Öğrenci",
                school = "Sınıfın Şampiyonu",
                xp = userXp,
                isCurrentUser = true,
                avatarColorHex = "#E0F2F1",
                avatarBorder = stats?.avatarBorder ?: "None"
            )
        )

        // Add mock competitors
        seedCompetitors.forEachIndexed { index, comp ->
            list.add(
                LeagueCompetitor(
                    rank = -1,
                    name = comp.first,
                    school = comp.second,
                    xp = (comp.third * leagueMultiplier).toInt(),
                    isCurrentUser = false,
                    avatarColorHex = when(index % 4) {
                        0 -> "#FF8A80"
                        1 -> "#FFD54F"
                        2 -> "#4FC3F7"
                        else -> "#AED581"
                    },
                    avatarBorder = if (index == 0) "Gold" else if (index == 1) "Diamond" else "None"
                )
            )
        }

        // Return sorted list with proper ranking indices
        return list.sortedByDescending { it.xp }.mapIndexed { i, c -> c.copy(rank = i + 1) }
    }

    fun submitOnboarding(username: String, grade: String) {
        viewModelScope.launch {
            repository.completeOnboarding(username, grade)
        }
    }
}
