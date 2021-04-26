package com.techpuzzle.keopi.di

import androidx.room.Room
import com.techpuzzle.keopi.data.room.KeopiDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.app.Application
import com.techpuzzle.keopi.data.repositiories.cafebar.CafeBarRepository
import com.techpuzzle.keopi.data.repositiories.cafebar.CafeBarDefaultRepository
import com.techpuzzle.keopi.data.repositiories.cafebars.CafeBarsDefaultRepository
import com.techpuzzle.keopi.data.repositiories.cafebars.CafeBarsRepository
import com.techpuzzle.keopi.data.repositiories.calendar.CalendarDefaultRepository
import com.techpuzzle.keopi.data.repositiories.calendar.CalendarRepository
import com.techpuzzle.keopi.data.repositiories.checkout.CheckoutDefaultRepository
import com.techpuzzle.keopi.data.repositiories.checkout.CheckoutRepository
import com.techpuzzle.keopi.data.repositiories.order.OrderDefaultRepository
import com.techpuzzle.keopi.data.repositiories.order.OrderRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideKeopiDatabase(
        app: Application
    ) = Room.databaseBuilder(app, KeopiDatabase::class.java, "KeopiDB")
        .fallbackToDestructiveMigration()
        .build()

    //singleton by default
    @Provides
    fun provideKeopiDao(db: KeopiDatabase) = db.getDao()

    @Singleton
    @Provides
    fun provideCafeBarRepository(
        database: KeopiDatabase
    ) = CafeBarDefaultRepository(database) as CafeBarRepository

    @Singleton
    @Provides
    fun provideCafeBarsRepository(
        database: KeopiDatabase
    ) = CafeBarsDefaultRepository(database) as CafeBarsRepository

    @Singleton
    @Provides
    fun provideCalendarRepository() = CalendarDefaultRepository() as CalendarRepository

    @Singleton
    @Provides
    fun provideOrderRepository() = OrderDefaultRepository() as OrderRepository

    @Singleton
    @Provides
    fun provideCheckoutRepository() = CheckoutDefaultRepository() as CheckoutRepository
}