package livecricket.livecrickettv.cricketstreaming.data.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import livecricket.livecrickettv.cricketstreaming.data.model.Match;
import livecricket.livecrickettv.cricketstreaming.data.model.Series;
import livecricket.livecrickettv.cricketstreaming.data.model.Standing;
import livecricket.livecrickettv.cricketstreaming.util.Constants;
import livecricket.livecrickettv.cricketstreaming.util.Converters;

@Database(entities = {Match.class, Series.class, Standing.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract MatchDao matchDao();
    public abstract SeriesDao seriesDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, Constants.DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
