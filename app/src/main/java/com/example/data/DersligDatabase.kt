package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserStats::class, CompletedQuiz::class, ShopItem::class], version = 1, exportSchema = false)
abstract class DersligDatabase : RoomDatabase() {
    abstract fun dersligDao(): DersligDao

    companion object {
        @Volatile
        private var INSTANCE: DersligDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DersligDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DersligDatabase::class.java,
                    "derslig_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.dersligDao()
                    
                    // Seed initial user stats
                    dao.insertUserStats(UserStats())

                    // Seed shop items
                    val defaultItems = listOf(
                        ShopItem("border_gold", "Altın Çerçeve", "BORDER", 50, false, "Profiline şık bir altın çerçeve ve parıltı kazandırır."),
                        ShopItem("border_diamond", "Ejderha Elması", "BORDER", 120, false, "Kraliyet mavisi ve eflatun elmaslardan oluşan asil profil çerçevesi."),
                        ShopItem("border_fire", "Alevli Lig Çerçevesi", "BORDER", 200, false, "En prestijli, animasyonlu alev efektli profil süslemesi!"),
                        ShopItem("reward_pro_week", "1 Haftalık Pro Üyelik", "REWARD", 150, false, "Tüm premium soru çözümlerine ve animasyonlu derslere tam erişim."),
                        ShopItem("reward_lgs_book", "LGS Kamp Soru Kitabı", "REWARD", 400, false, "Derslig Yayınları'ndan özel LGS soru bankası kargoyla adresine!"),
                        ShopItem("reward_gift_100", "50 TL Google Play Hediye Kodu", "REWARD", 350, false, "Google Play Store'da dilediğince harcayabileceğin dijital cüzdan kodu.")
                    )
                    dao.insertShopItems(defaultItems)
                }
            }
        }
    }
}
