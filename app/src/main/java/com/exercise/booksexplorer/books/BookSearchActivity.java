/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: Google Books API based search activity
 */
package com.exercise.booksexplorer.books;

import android.app.SearchManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;

import com.exercise.booksexplorer.BaseActivity;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.databinding.ActivityBookSearchBinding;
import com.exercise.booksexplorer.util.SimpleIdlingResource;
import com.google.api.services.books.Books;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Search books activity. Handles intent ACTION_SEARCH.
 */
public class BookSearchActivity extends BaseActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = BookSearchActivity.class.getSimpleName();
    private ActivityBookSearchBinding mBookSearchBinding;
    private BookSearchAdapter adapter;
    private VolumesObservable mVolumesObservable;
    private SimpleIdlingResource mIdlingResource;

    // Google Books API accessor
    @Inject
    Books mBooks;

    /**
     * Observable providing new page with volume items asynchronously
     */
    private class VolumesObservable implements ObservableOnSubscribe<List<Volume>> {

        // The search term
        private String mQuery;

        // The start item to request from server. Used to perform page by page request.
        private int mStart = 0;

        // The total number of items by the query result
        private int mTotalItems;

        // The number of items in the current page
        private int mItemsCount;

        // A handler to the background task
        private Disposable mDisposable;

        /**
         * Constructs the VolumesObservable with query and success/failure callback handlers
         *
         * @param query           a query to perform the search for books
         */
        public VolumesObservable(String query) {
            mQuery = query;
        }

        /**
         * @return true if the task is working in progress, false otherwise
         */
        public boolean isLoading() {
            return !getIdlingResource().isIdleNow();
        }

        /**
         * @return true if more volume pages are present
         */
        boolean hasMore() {
            return mStart < mTotalItems;
        }

        /**
         * Requests the first or next page
         */
        public void nextPage() {
            Log.i(TAG, ".nextPage: mStart = " + mStart + ", mItemsCount = " + mItemsCount);
            mStart = mStart + mItemsCount;

            // Prevent more than one tasks at a time by replacing the previous one with the new requested
            if (mDisposable != null) {
                mDisposable.dispose();
            }

            mDisposable = Observable.create(this)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<List<Volume>>() {
                                @Override
                                public void accept(List<Volume> volumeList) throws Exception {
                                    mItemsCount = volumeList.size();
                                    if (mIdlingResource != null)
                                        mIdlingResource.setIdleState(true);

                                    // stops the loading indicator and add the new volumes
                                    adapter.removeLoadingFooter();
                                    addVolumes(volumeList);
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG, throwable.getMessage(), throwable);
                                    mItemsCount = 0;
                                    if (mIdlingResource != null)
                                        mIdlingResource.setIdleState(true);
                                    if (adapter.getItemCount() > 0)
                                        // Show retry at the bottom of the list if failure occurred when some items are already present
                                        adapter.showRetry(true, R.string.error_unknown);
                                    else {
                                        // Show central error message at the place of the items list
                                        showError(R.string.error_unknown);
                                        hideResultPlaceHolder();
                                    }
                                }
                            });
        }

        /**
         * Retries retrieving last page
         */
        public void retry() {
            Log.i(TAG, ".retry");
            // next page will start at the same position as the current
            mItemsCount = 0;
            nextPage();
        }

        /**
         * Implements the subscribe method from ObservableOnSubscribe interface
         * used to retrieve the next page volumes in a background task
         */
        @Override
        public void subscribe(ObservableEmitter<List<Volume>> emitter) throws Exception {
            if (mIdlingResource != null)
                mIdlingResource.setIdleState(false);
            Books.Volumes.List volumeList = getBooks().volumes().list(mQuery);

            // Set offset of the new page requested
            volumeList.setStartIndex(Long.valueOf(mStart));
            Volumes volumes = volumeList.execute();

            // Keep the total items in order to know if there are more pages to come
            mTotalItems = volumes.getTotalItems();

            List<Volume> items = volumes.getItems();
            if (items == null) {
                // Null values are generally not allowed in 2.x operators and sources.
                items = new ArrayList<>();
            }
            emitter.onNext(items);
            emitter.onComplete();
        }

        /**
         * Cancels the subscription to this observable
         */
        public void cancel() {
            if (mDisposable != null) {
                mDisposable.dispose();
            }
        }
    }


    /**
     * Handles query submit.
     * The method can be invoked by SearchView or in response of intent ACTION_SEARCH
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, ".performSearch: query = " + query);
        mBookSearchBinding.searchView.setIconified(true);
        setResultPlaceHolder(getString(R.string.search_progress, query));
        adapter.clear();
        mVolumesObservable = new VolumesObservable(query);
        mVolumesObservable.nextPage();
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookSearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_search);
        setSupportActionBar(mBookSearchBinding.toolbar);

        // apply searchable options on the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mBookSearchBinding.searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mBookSearchBinding.searchView.setOnQueryTextListener(this);

        mBookSearchBinding.booksRv.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mBookSearchBinding.booksRv.setLayoutManager(linearLayoutManager);
        adapter = new BookSearchAdapter(this, new BookSearchAdapter.Callbacks() {
            @Override
            public void onClick(View view) {
                int itemPosition = mBookSearchBinding.booksRv.getChildLayoutPosition(view);
                Volume book = adapter.getItem(itemPosition);
                startActivityWithTransition(BookDetailsActivity.makeIntent(BookSearchActivity.this, book));
            }

            @Override
            public void retryPageLoad() {
                mVolumesObservable.retry();
            }
        });

        mBookSearchBinding.booksRv.setAdapter(adapter);
        mBookSearchBinding.booksRv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                Log.i(TAG, ".loadMoreItems");
                if (mVolumesObservable.hasMore()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Shows loading indicator and requests next page
                            adapter.addLoadingFooter();
                            mVolumesObservable.nextPage();
                        }
                    });
                }
            }

            @Override
            public boolean isLastPage() {
                return !mVolumesObservable.hasMore();
            }

            @Override
            public boolean isLoading() {
                return mVolumesObservable.isLoading();
            }
        });

        handleIntent(getIntent());
    }

    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            onQueryTextSubmit(query);
        }
    }

    private void addVolumes(List<Volume> volumes) {
        if (volumes != null && volumes.size() > 0) {
            for (Volume volume : volumes) {
                Log.i(TAG, volume.toString());
            }
            adapter.addAll(volumes);
            adapter.notifyDataSetChanged();
        }
        if (adapter.getItemCount() > 0)
            hideResultPlaceHolder();
        else
            setResultPlaceHolder(getString(R.string.search_no_results));
    }

    private void setResultPlaceHolder(String msg) {
        if (msg == null) {
            mBookSearchBinding.booksRv.setVisibility(View.VISIBLE);
            mBookSearchBinding.placeholderText.setVisibility(View.INVISIBLE);
        } else {
            mBookSearchBinding.booksRv.setVisibility(View.INVISIBLE);
            mBookSearchBinding.placeholderText.setVisibility(View.VISIBLE);
            mBookSearchBinding.placeholderText.setText(msg);
        }
    }

    private void hideResultPlaceHolder() {
        setResultPlaceHolder(null);
    }

    public void onStop() {
        super.onStop();
        Log.i(TAG,".onStop");
        if (mVolumesObservable != null)
            mVolumesObservable.cancel();
    }

    /**
     * Only called from test, creates and returns a new {@link SimpleIdlingResource}.
     */
    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new SimpleIdlingResource();
        }
        return mIdlingResource;
    }
}
