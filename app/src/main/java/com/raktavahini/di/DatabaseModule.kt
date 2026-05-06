package com.raktavahini.di

import android.content.Context
import com.raktavahini.data.local.DonationDao
import com.raktavahini.data.local.DonorDao
import com.raktavahini.data.local.RaktaVahiniDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RaktaVahiniDatabase {
        return RaktaVahiniDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideDonorDao(database: RaktaVahiniDatabase): DonorDao {
        return database.donorDao()
    }

    @Provides
    @Singleton
    fun provideDonationDao(database: RaktaVahiniDatabase): DonationDao {
        return database.donationDao()
    }
}