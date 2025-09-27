package com.SamiDev.agendasencilla.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Clase principal de la base de datos Room para la aplicación.
 * Define la configuración de la base de datos, incluyendo las entidades y la versión.
 * Implementa el patrón Singleton para asegurar una única instancia de la base de datos.
 */
@Database(entities = [Contacto::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Proporciona acceso al Data Access Object (DAO) para la entidad [Contacto].
     *
     * @return Una instancia de [ContactoDao].
     */
    abstract fun contactoDao(): ContactoDao

    companion object {
        // La anotación @Volatile asegura que el valor de INSTANCIA sea siempre actualizado
        // y visible para todos los hilos de ejecución. Esto previene problemas de concurrencia
        // cuando múltiples hilos intentan acceder o modificar la instancia al mismo tiempo.
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        private const val NOMBRE_BASE_DATOS = "contactos_database"

        /**
         * Obtiene la instancia Singleton de [AppDatabase].
         * Si la instancia no existe, la crea de forma segura para hilos (thread-safe).
         *
         * @param contexto El contexto de la aplicación, necesario para crear la base de datos.
         * @return La instancia Singleton de [AppDatabase].
         */
        fun obtenerInstancia(contexto: Context): AppDatabase {
            // El bloque synchronized asegura que solo un hilo a la vez pueda ejecutar este código,
            // previniendo la creación de múltiples instancias de la base de datos si varios hilos
            // la solicitan simultáneamente cuando INSTANCIA es null.
            return INSTANCIA ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    contexto.applicationContext,
                    AppDatabase::class.java,
                    NOMBRE_BASE_DATOS
                )
                // Aquí se pueden añadir migraciones si se cambia la versión de la base de datos.
                // .addMigrations(MIGRATION_1_2)
                // Por simplicidad, si no hay migraciones y se cambia el esquema, se puede usar:
                // .fallbackToDestructiveMigration() // ¡PRECAUCIÓN! Esto borrará los datos existentes.
                .build()
                INSTANCIA = instancia
                instancia // Devuelve la instancia recién creada
            }
        }
    }
}
