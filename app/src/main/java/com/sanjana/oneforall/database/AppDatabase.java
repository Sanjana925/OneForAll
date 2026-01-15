package com.sanjana.oneforall.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
@Database(
        entities = {
                Category.class,
                Item.class,
                ListFolder.class,
                ListItem.class,
                CalendarEvent.class,
                DailyProgress.class
        },
        version = 3 // incremented version
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract CategoryDao categoryDao();
    public abstract ItemDao itemDao();
    public abstract ListFolderDao listFolderDao();
    public abstract ListItemDao listItemDao();
    public abstract CalendarEventDao calendarEventDao();
    public abstract DailyProgressDao dailyProgressDao();

    // Migration from version 2 → 3
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            try {
                // Add new columns to Item table without deleting existing data

                database.execSQL("ALTER TABLE Item ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // If new tables were added, create them safely here
            // e.g., database.execSQL("CREATE TABLE IF NOT EXISTS NewTable (id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL)");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "oneforall_db"
                            )
                            .addMigrations(MIGRATION_2_3) // <-- safe migration
                            .build();
                }
            }
        }
        return instance;
    }
}
