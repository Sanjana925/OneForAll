package com.sanjana.oneforall.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {
                Category.class,
                Item.class,
                ListFolder.class,
                ListItem.class,
                CalendarEvent.class   // <- Add this
        },
        version = 1  // <- Increment version because we added a new table
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract CategoryDao categoryDao();
    public abstract ItemDao itemDao();
    public abstract ListFolderDao listFolderDao();
    public abstract ListItemDao listItemDao();
    public abstract CalendarEventDao calendarEventDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "oneforall_db"
                            )
                            .fallbackToDestructiveMigration() // This will recreate DB on version change
                            .build();
                }
            }
        }
        return instance;
    }
}
