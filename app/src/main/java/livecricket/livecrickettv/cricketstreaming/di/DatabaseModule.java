package livecricket.livecrickettv.cricketstreaming.di;

import android.content.Context;

import livecricket.livecrickettv.cricketstreaming.data.repository.AppDatabase;
import livecricket.livecrickettv.cricketstreaming.data.repository.MatchDao;
import livecricket.livecrickettv.cricketstreaming.data.repository.SeriesDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    public static MatchDao provideMatchDao(AppDatabase database) {
        return database.matchDao();
    }

    @Provides
    public static SeriesDao provideSeriesDao(AppDatabase database) {
        return database.seriesDao();
    }
}
