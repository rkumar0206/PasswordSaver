package com.rohitthebest.passwordsaver.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rohitthebest.passwordsaver.database.databases.AppSettingDatabase
import com.rohitthebest.passwordsaver.database.databases.PasswordDatabase
import com.rohitthebest.passwordsaver.other.Constants.APP_SETTING_DATABASE_NAME
import com.rohitthebest.passwordsaver.other.Constants.PASSWORD_DATABASE_NAME
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

    //============================= Password Database ===========================

    val migration_1_2 = object : Migration(1, 2) {

        override fun migrate(database: SupportSQLiteDatabase) {

            database.execSQL("ALTER TABLE 'password_table' ADD COLUMN 'siteLink' TEXT DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun providesPasswordDB(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        PasswordDatabase::class.java,
        PASSWORD_DATABASE_NAME
    )
        .addMigrations(migration_1_2)
        .build()

    @Provides
    @Singleton
    fun providesPasswordDao(
        db: PasswordDatabase
    ) = db.getPasswordDao()
}