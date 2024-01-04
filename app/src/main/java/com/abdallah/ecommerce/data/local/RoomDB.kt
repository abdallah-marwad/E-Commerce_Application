//package com.abdallah.ecommerce.data.local
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import com.abdallah.ecommerce.application.MyApplication
//import com.abdallah.ecommerce.data.local.Dao.BannerDao
//import com.abdallah.ecommerce.data.local.Dao.CategoriesDao
//import com.abdallah.ecommerce.data.local.Dao.ProductDao
//import com.abdallah.ecommerce.data.local.converter.ArrayListConverter
//import com.abdallah.ecommerce.data.local.converter.UriConverter
//import com.abdallah.ecommerce.data.model.Category
//import com.abdallah.ecommerce.data.model.HomeBanner
//import com.abdallah.ecommerce.data.model.Product
//
//@Database(entities = [Product::class, Category::class, HomeBanner::class], version = 1)
//@TypeConverters(UriConverter::class , ArrayListConverter::class)
//abstract class RoomDB : RoomDatabase() {
//
//
//    abstract fun bannerDao () : BannerDao
//    abstract fun categoriesDao () : CategoriesDao
//    abstract fun productDao () : ProductDao
//    companion object {
//        private var instance: RoomDB? = null
//
//        fun getInstance(): RoomDB? {
//            if (instance == null) {
//                synchronized(RoomDB::class.java) {
//                    if (instance == null) {
//                        try {
//                            instance = Room.databaseBuilder(
//                                MyApplication.myAppContext,
//                                RoomDB::class.java,
//                                "ecommerce.db"
//                            ).fallbackToDestructiveMigration()
//                                .build()
//                        } catch (e: Exception) {
//                            return null
//                        }
//                    }
//                }
//            }
//            return instance
//        }
//    }
//
//
//}