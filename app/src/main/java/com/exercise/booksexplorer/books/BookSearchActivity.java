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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;

import com.exercise.booksexplorer.BaseActivity;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.databinding.ActivityBookSearchBinding;
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
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class BookSearchActivity extends BaseActivity {
    private static final String TAG = BookSearchActivity.class.getSimpleName();
    private ActivityBookSearchBinding mBookSearchBinding;
    private BookSearchAdapter adapter;
    private VolumesObservable mVolumesObservable;

    private class VolumesObservable implements ObservableOnSubscribe<List<Volume>> {
        private String mQuery;
        private int mStart = 0;
        private int mTotalItems;
        private int mItemsCount;
        private boolean mIsLoading;
        private SearchCallbacks mSearchCallbacks;

        public VolumesObservable(String query, SearchCallbacks searchCallbacks) {
            mQuery = query;
            mSearchCallbacks = searchCallbacks;
        }

        public boolean isLoading() {
            return mIsLoading;
        }

        boolean hasMore() {
            return mStart < mTotalItems;
        }

        @Override
        public void subscribe(ObservableEmitter<List<Volume>> e) throws Exception {
            mIsLoading = true;
            Books.Volumes.List volumeList = getBooks().volumes().list(mQuery);
            volumeList.setStartIndex(Long.valueOf(mStart));
            Volumes volumes = volumeList.execute();
            mTotalItems = volumes.getTotalItems();

            List<Volume> items = volumes.getItems();
            if (items == null) {
                // Null values are generally not allowed in 2.x operators and sources.
                items = new ArrayList<>();
            }
            e.onNext(items);
            e.onComplete();
        }

        public void nextPage() {
            Log.i(TAG, ".nextPage: mStart = " + mStart + ", mItemsCount = " + mItemsCount);
            mStart = mStart + mItemsCount;
            Observable.create(this)
                    .subscribeOn(Schedulers.newThread()) // Create a new Thread
                    .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
                    .subscribe(
                            new Consumer<List<Volume>>() {
                                @Override
                                public void accept(List<Volume> volumeList) throws Exception {
                                    mSearchCallbacks.onSuccess(volumeList);
                                    if (volumeList != null)
                                        mItemsCount = volumeList.size();
                                    mIsLoading = false;
                                }
                            }, new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    Log.e(TAG, throwable.getMessage(), throwable);
                                    mSearchCallbacks.onFailure(throwable);
                                    mItemsCount = 0;
                                    mIsLoading = false;
                                }
                            });
        }

        public void retry() {
            Log.i(TAG, ".retry");
            mItemsCount = 0;
            nextPage();
        }

    }

    @Inject
    Books mBooks;

    private void performSearch(String query) {
        Log.i(TAG, ".performSearch: query = " + query);
        setResultPlaceHolder(R.string.search_progress);
        adapter.clear();
        mVolumesObservable = new VolumesObservable(query, mOnSearch);
        mVolumesObservable.nextPage();
    }

    /**
     * Search result callbacks interface
     */
    private interface SearchCallbacks {
        /**
         * Called back on search success
         *
         * @param volumes
         */
        public void onSuccess(List<Volume> volumes);

        /**
         * Called back on search failure
         *
         * @param throwable
         */
        public void onFailure(Throwable throwable);
    }

    /**
     * Implements search responses
     */
    private SearchCallbacks mOnSearch = new SearchCallbacks() {
        @Override
        public void onSuccess(List<Volume> volumeList) {
            adapter.removeLoadingFooter();
            updateVolumes(volumeList);
        }

        @Override
        public void onFailure(Throwable throwable) {
            if (adapter.getItemCount() > 0)
                adapter.showRetry(true, R.string.error_unknown);
            else {
                showError(R.string.error_unknown);
                hideResultPlaceHolder();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBookSearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_search);
        setSupportActionBar(mBookSearchBinding.toolbar);
        mBookSearchBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mBookSearchBinding.searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
            performSearch(query);
        }
    }

    private void updateVolumes(List<Volume> volumes) {
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
            setResultPlaceHolder(R.string.search_no_results);
    }

    private void setResultPlaceHolder(int resId) {
        if (resId == 0) {
            mBookSearchBinding.booksRv.setVisibility(View.VISIBLE);
            mBookSearchBinding.placeholderText.setVisibility(View.INVISIBLE);
        } else {
            mBookSearchBinding.booksRv.setVisibility(View.INVISIBLE);
            mBookSearchBinding.placeholderText.setVisibility(View.VISIBLE);
            mBookSearchBinding.placeholderText.setText(resId);
        }
    }

    private void hideResultPlaceHolder() {
        setResultPlaceHolder(0);
    }
}
