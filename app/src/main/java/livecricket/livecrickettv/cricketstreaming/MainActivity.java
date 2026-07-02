package livecricket.livecrickettv.cricketstreaming;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import livecricket.livecrickettv.cricketstreaming.ui.MatchesFragment;
import livecricket.livecrickettv.cricketstreaming.ui.RecentFragment;
import livecricket.livecrickettv.cricketstreaming.ui.StandingsFragment;
import livecricket.livecrickettv.cricketstreaming.ui.UpcomingFragment;
import livecricket.livecrickettv.cricketstreaming.util.WorkManagerScheduler;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private Fragment matchesFragment, recentFragment, upcomingFragment, standingsFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Load and apply saved theme preference before applying layout styles
        android.content.SharedPreferences prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        if (prefs.contains("is_dark_mode")) {
            boolean isDarkMode = prefs.getBoolean("is_dark_mode", false);
            int mode = isDarkMode ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                                  : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != mode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
            }
        }


        setSupportActionBar(findViewById(R.id.toolbar));

        // Handle window insets for Edge-to-Edge compatibility (API 35/36)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Apply top inset as padding to toolbar to prevent overlapping with status bar
            View toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.setPadding(0, systemBars.top, 0, 0);
                TypedValue tv = new TypedValue();
                int actionBarHeight = 0;
                if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                    actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
                }
                if (actionBarHeight > 0) {
                    toolbar.getLayoutParams().height = actionBarHeight + systemBars.top;
                }
            }

            // Apply bottom inset as margin to card_bottom_navigation to float above system navigation bar
            View cardBottomNav = findViewById(R.id.card_bottom_navigation);
            if (cardBottomNav != null) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) cardBottomNav.getLayoutParams();
                int originalBottomMargin = (int) (16 * getResources().getDisplayMetrics().density); // 16dp
                marginParams.bottomMargin = originalBottomMargin + systemBars.bottom;
                cardBottomNav.setLayoutParams(marginParams);
            }

            return windowInsets;
        });

        // Debug: Force one-time sync to verify caching works
        WorkManagerScheduler.runOneTimeSync(this);

        if (savedInstanceState == null) {
            initFragments();
        } else {
            restoreFragments();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
        if (itemId == R.id.action_theme) {
            toggleThemeSetting();
            return true;
        }
            if (itemId == R.id.nav_matches) {
                showFragment(matchesFragment);
                return true;
            } else if (itemId == R.id.nav_recent) {
                showFragment(recentFragment);
                return true;
            } else if (itemId == R.id.nav_upcoming) {
                showFragment(upcomingFragment);
                return true;
            } else if (itemId == R.id.nav_standings) {
                showFragment(standingsFragment);
                return true;
            }
            return false;
        });
    }

    private void initFragments() {
        matchesFragment = new MatchesFragment();
        recentFragment = new RecentFragment();
        upcomingFragment = new UpcomingFragment();
        standingsFragment = new StandingsFragment();

        activeFragment = matchesFragment;

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, standingsFragment, "4").hide(standingsFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, upcomingFragment, "3").hide(upcomingFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, recentFragment, "2").hide(recentFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, matchesFragment, "1").commit();
    }

    private void restoreFragments() {
        matchesFragment = getSupportFragmentManager().findFragmentByTag("1");
        recentFragment = getSupportFragmentManager().findFragmentByTag("2");
        upcomingFragment = getSupportFragmentManager().findFragmentByTag("3");
        standingsFragment = getSupportFragmentManager().findFragmentByTag("4");

        // Determine active fragment (this is simplified, ideally you save it in outState)
        activeFragment = matchesFragment;
        if (matchesFragment == null) initFragments();
    }

    private void showFragment(Fragment fragment) {
        if (activeFragment == fragment) return;
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(fragment).commit();
        activeFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_theme) {
            toggleThemeSetting();
            return true;
        }
        if (itemId == R.id.action_privacy) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://doc-hosting.flycricket.io/cricket-pulse-live-scores-privacy-policy/82b25c92-0566-4847-9774-bc6b22c16ae3/privacy"));
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_share) {
            String shareBody = "Download Cricket Pulse for live scores and cricket updates! https://play.google.com/store/apps/details?id=" + getPackageName();
            android.content.Intent sharingIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(android.content.Intent.createChooser(sharingIntent, "Share via"));
            return true;
        } else if (itemId == R.id.action_rate_us) {
            String packageName = getPackageName();
            try {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
            return true;
        } else if (itemId == R.id.action_more_apps) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://play.google.com/store/apps/developer?id=abubakar+nadeem"));
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_about) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            intent.setData(android.net.Uri.parse("https://thebicodes.com/"));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleThemeSetting() {
        int currentMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
        boolean toDark = (currentMode != androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            toDark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES 
                   : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        );
        
        // Save preference
        android.content.SharedPreferences.Editor editor = getSharedPreferences("theme_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("is_dark_mode", toDark);
        editor.apply();
    }
}
