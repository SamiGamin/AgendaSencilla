package com.SamiDev.agendasencilla.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos principal de la aplicación.
 * Define la configuración de Room y sirve como punto de acceso a los DAOs.
 */
@Database(entities = [FavoritoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provee acceso al DAO de favoritos.
     */
    abstract fun favoritoDao(): FavoritoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        /**
         * Obtiene la instancia única (Singleton) de la base de datos.
         * Utiliza bloqueo doble para asegurar que solo se cree una instancia.
         *
         * @param contexto El contexto de la aplicación.
         * @return La instancia de [AppDatabase].
         */
        fun obtenerInstancia(contexto: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    "contactos_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCIA = instancia
                instancia
            }
        }
    }
}