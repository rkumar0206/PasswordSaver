package com.rohitthebest.passwordsaver.module

import android.content.Context
import androidx.room.Room
import com.rohitthebest.passwordsaver.database.databases.AppSettingDatabase
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object Module {

    //============================= App Setting Database ===========================

    @Provides
    @Singleton
    fun provideAppSettingDB(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        AppSettingDatabase::class.java,
        APP_SETTING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun providesAppSettingDao(db: AppSettingDatabase) = db.getAppSettingsDao()


}