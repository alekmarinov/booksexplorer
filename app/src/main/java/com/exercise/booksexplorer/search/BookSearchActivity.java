package com.exercise.booksexplorer.search;

import android.app.SearchManager;
import android.arch.paging.PagedListAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.DiffCallback;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;

import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.exercise.booksexplorer.BaseActivity;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.databinding.ActivitySearchBinding;
import com.google.api.services.books.Books;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookSearchActivity extends BaseActivity {
  private static final String TAG = BookSearchActivity.class.getSimpleName();
  private ActivitySearchBinding mActivitySearchBinding;
  @Inject Books mBooks;
  PaginationAdapter adapter;
  private static final int PAGE_START = 0;
  private static final int PAGE_SIZE = 20;
  private boolean isLoading = false;
  private boolean isLastPage = false;
  // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
  private int TOTAL_PAGES = 5;
  private int currentPage = PAGE_START;
  private String mQuery;

  private interface OnSearch {
    public void onSuccess(Volumes volumes);

    public void onFailure(Throwable throwable);
  }

  private OnSearch mOnSearch = new OnSearch() {
    @Override public void onSuccess(Volumes volumes) {
      List<Volume> volumeList = volumes.getItems();
      if (volumeList == null || volumeList.size() == 0) {
        updateEmptyMessage();
        return;
      }
      updateVolumes(volumeList);
    }

    @Override public void onFailure(Throwable throwable) {
      // FIXME: Handle the error
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mActivitySearchBinding = DataBindingUtil.setContentView(this, R.layout.activity_search);
    setSupportActionBar(mActivitySearchBinding.toolbar);
    mActivitySearchBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override public boolean onQueryTextSubmit(String query) {
        performSearch(query, 0, PAGE_SIZE, mOnSearch);
        return false;
      }

      @Override public boolean onQueryTextChange(String newText) {
        return false;
      }
    });
    mActivitySearchBinding.booksRv.setHasFixedSize(true);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    mActivitySearchBinding.booksRv.setLayoutManager(linearLayoutManager);
    adapter = new PaginationAdapter(this, new PaginationAdapter.Callbacks() {
      @Override public void retryPageLoad() {
        Log.i(TAG, ".retryPageLoad");
      }
    });

    mActivitySearchBinding.booksRv.setAdapter(adapter);
    mActivitySearchBinding.booksRv.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
      @Override
      protected void loadMoreItems() {
        isLoading = true;
        currentPage += 1;

        loadNextPage();
      }

      @Override
      public int getTotalPageCount() {
        return 5;
      }

      @Override
      public boolean isLastPage() {
        return isLastPage;
      }

      @Override
      public boolean isLoading() {
        return isLoading;
      }
    });

    handleIntent(getIntent());
  }

  private void loadNextPage() {

    performSearch(mQuery, currentPage * PAGE_SIZE, PAGE_SIZE, mOnSearch);
    adapter.removeLoadingFooter();
    isLoading = false;

    if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
    else isLastPage = true;
  }

  public void onNewIntent(Intent intent) {
    setIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      mQuery = intent.getStringExtra(SearchManager.QUERY);
      performSearch(mQuery, 0, PAGE_SIZE, mOnSearch);
    }
  }

  private void updateEmptyMessage() {
    Log.i(TAG, "No search results");
  }

  private void updateVolumes(List<Volume> volumes) {
    for (Volume volume : volumes) {
      Log.i(TAG, volume.toString());
    }
    adapter.setBooks(volumes);
    if (currentPage <= TOTAL_PAGES) adapter.addLoadingFooter();
    else isLastPage = true;
//    mActivitySearchBinding.booksRv.setAdapter(new BooksAdapter(volumes));
  }

  private class PerformSearchTask extends AsyncTask<String, Integer, Volumes> {
    private OnSearch mOnSearch;
    private Throwable mThrowable;
    private String mQuery;
    private int mStart;
    private int mCount;

    PerformSearchTask(String query, int start, int count, OnSearch onSearch) {
      mQuery = query;
      mStart = start;
      mCount = count;
      mOnSearch = onSearch;
    }

    @Override
    protected Volumes doInBackground(String... dummy) {
      Books books = getBooks();
      Books.Volumes.List volumesList = null;
      try {
        volumesList = books.volumes().list(mQuery);
        volumesList.setStartIndex(Long.valueOf(mStart));
        volumesList.setMaxResults(Long.valueOf(mCount));
        return volumesList.execute();

      } catch (IOException e) {
        Log.e(TAG, e.getMessage(), e);
        mThrowable = e;
      }
      return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(Volumes volumes) {
      if (mThrowable != null)
        mOnSearch.onFailure(mThrowable);
      else
        mOnSearch.onSuccess(volumes);
    }
  }

  private void performSearch(String query, int start, int count, OnSearch onSearch) {
    mQuery = query;
    new PerformSearchTask(query, start, count, onSearch).execute();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.book_image) ImageView mBookImage;
    @BindView(R.id.book_title) TextView mBookTitle;
    @BindView(R.id.book_description) TextView mBookDescription;

    public ViewHolder(ViewGroup view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }

  public class BooksAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Volume> mVolumes;

    public BooksAdapter(List<Volume> volumes) {
      mVolumes = volumes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
      ViewGroup volumeView = (ViewGroup) LayoutInflater.from(parent.getContext())
              .inflate(R.layout.book_item, parent, false);
      ViewHolder vh = new ViewHolder(volumeView);
      return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      Volume volume = mVolumes.get(position);
      Glide.with(BookSearchActivity.this).load(volume.getVolumeInfo().getImageLinks().getSmall()).into(holder.mBookImage);
      holder.mBookTitle.setText(volume.getVolumeInfo().getTitle());
      holder.mBookDescription.setText(volume.getVolumeInfo().getSubtitle());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
      return mVolumes.size();
    }
  }
}