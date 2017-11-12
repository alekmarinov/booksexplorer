/*
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: BookSearchActivity testing class
 */
package com.exercise.booksexplorer;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.AutoCompleteTextView;

import com.exercise.booksexplorer.books.BookSearchActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.greaterThan;

@RunWith(AndroidJUnit4.class)
public class BookSearchActivityTest {
    private IdlingResource mIdlingResource;
    private static final String TEST_QUERY = "Lua";

    @Rule
    public ActivityTestRule<BookSearchActivity> mActivityRule = new ActivityTestRule<>(
            BookSearchActivity.class);

    @Before
    public void registerIdlingResource() {
        mIdlingResource = mActivityRule.getActivity().getIdlingResource();
        // To prove that the test fails, omit this call:
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @After
    public void unregisterIdlingResource() {
        if (mIdlingResource != null) {
            Espresso.unregisterIdlingResources(mIdlingResource);
        }
    }

    @Test
    public void performQuery_sameActivity() {
        // Open search view
        onView(withId(R.id.search_view)).perform(click());

        // Type query and then press enter to perform search
        onView(isAssignableFrom(AutoCompleteTextView.class)).perform(typeText(TEST_QUERY), pressKey(KeyEvent.KEYCODE_ENTER));

        // Check if the list is populated with some results
        onView(withId(R.id.books_rv)).check(new RecyclerViewItemCountAssertion(greaterThan(0)));
    }
}
