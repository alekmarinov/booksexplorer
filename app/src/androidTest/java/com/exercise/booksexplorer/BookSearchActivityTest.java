package com.exercise.booksexplorer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;

import com.exercise.booksexplorer.books.BookSearchActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class BookSearchActivityTest {
    private String queryAndroid;
    private String expectedBookTitle;

    @Rule
    public ActivityTestRule<BookSearchActivity> mActivityRule = new ActivityTestRule<>(
            BookSearchActivity.class);

    @Before
    public void init() {
        // query books
        queryAndroid = "lua";

        // expected book
        expectedBookTitle = "Programming in Lua";
    }

    // Convenience helper
    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    @Test
    public void performQuery_sameActivity() {
        // Open search view
        onView(withId(R.id.search_view)).perform(click());

        // Type query and then press enter to perform search
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText(queryAndroid), pressKey(KeyEvent.KEYCODE_ENTER));

        // Check
        onView(withRecyclerView(R.id.books_rv).atPosition(0)).check(matches(hasDescendant(withText(expectedBookTitle))));

    }
}
