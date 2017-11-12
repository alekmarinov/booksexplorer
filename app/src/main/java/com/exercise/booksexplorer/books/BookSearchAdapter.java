/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: An adapter for RecyclerView with paging capabilities and error resiliency.
 *              The class is based on the work of Suleiman Ali Shakir
 *              https://github.com/Suleiman19/Android-Pagination-with-RecyclerView
 */
package com.exercise.booksexplorer.books;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.util.StringUtils;
import com.google.api.services.books.model.Volume;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * An adapter for RecyclerView with paging capabilities and error resiliency.
 */
public class BookSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final String TAG = BookSearchAdapter.class.getSimpleName();
  private static final int ITEM = 0;
  private static final int LOADING = 1;
  private List<Volume> mBooks;
  private Context mContext;
  private boolean mIsLoadingAdded = false;
  private boolean mRetryPageLoad = false;
  private int mErrResId;
  private Callbacks mCallbacks;

  /**
   * Client specific callbacks
   */
  public interface Callbacks extends View.OnClickListener {
    /**
     * Called when the retry button is tapped
     */
    void retryPageLoad();
  }

  public BookSearchAdapter(Context context, Callbacks callbacks) {
    mContext = context;
    mCallbacks = callbacks;
    mBooks = new ArrayList<>();
  }

  /**
   * Creates RecyclerView.ViewHolder of type BookVH or LoadingVH depending on the given view type ITEM or LOADING
   * @param parent
   * @param viewType
   * @return
   */
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecyclerView.ViewHolder viewHolder = null;
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    switch (viewType) {
      case ITEM:
        ViewGroup viewItem = (ViewGroup)inflater.inflate(R.layout.book_item, parent, false);
        viewItem.setOnClickListener(mCallbacks);
        viewHolder = new BookVH(viewItem);
        break;
      case LOADING:
        View viewLoding = inflater.inflate(R.layout.item_progress, parent, false);
        viewHolder = new LoadingVH(viewLoding);
        break;
    }
    return viewHolder;
  }

  /**
   * Binds view holder's view elements with data provided by the item at the given position
   *
   * @param holder the view holder ITEM or LOADING
   * @param position the position in the adapter's list
   */
  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    Volume volume = mBooks.get(position);

    switch (getItemViewType(position)) {
      case ITEM:
        if (volume.getVolumeInfo() != null) {
          final BookVH bookVH = (BookVH) holder;
          if (volume.getVolumeInfo().getImageLinks() != null)
            Glide.with(mContext).load(volume.getVolumeInfo().getImageLinks().getThumbnail()).into(bookVH.mBookImage);

          bookVH.mBookTitle.setText(volume.getVolumeInfo().getTitle());
          bookVH.mBookAuthors.setText(StringUtils.concat(volume.getVolumeInfo().getAuthors()));
          bookVH.mBookDescription.setText(volume.getVolumeInfo().getDescription());
        }
        break;

      case LOADING:
        LoadingVH loadingVH = (LoadingVH) holder;
        if (mRetryPageLoad) {
          // show retry view
          loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
          loadingVH.mProgressBar.setVisibility(View.GONE);

          if (mErrResId != 0)
            loadingVH.mErrorTxt.setText(mErrResId);
          else
            loadingVH.mErrorTxt.setText(R.string.error_unknown);

        } else {
          // show loading progress
          loadingVH.mErrorLayout.setVisibility(View.GONE);
          loadingVH.mProgressBar.setVisibility(View.VISIBLE);
        }
        break;
    }
  }

  @Override
  public int getItemCount() {
    return mBooks == null ? 0 : mBooks.size();
  }

  @Override
  public int getItemViewType(int position) {
    // In loading mode the last position is the LOADING element, or remaining are ITEM
    return (position == mBooks.size() - 1 && mIsLoadingAdded) ? LOADING : ITEM;
  }

  /**
   * Appends a book to the adapter
   * @param volume new book to append to the adapter's list
   */
  public void add(Volume volume) {
    mBooks.add(volume);
    notifyItemInserted(mBooks.size() - 1);
  }

  /**
   * Appends multiple books to the adapter
   * @param volumes list of books to append to the adapter
   */
  public void addAll(List<Volume> volumes) {
    mBooks.addAll(volumes);
    notifyItemRangeInserted(mBooks.size() -  volumes.size(), volumes.size());
  }

  /**
   * Removes a book from the adapter
   * @param volume is the book to be removed
   */
  public void remove(Volume volume) {
    int position = mBooks.indexOf(volume);
    if (position > -1) {
      mBooks.remove(position);
      notifyItemRemoved(position);
    }
  }

  /**
   * Remove all books from the adapter
   */
  public void clear() {
    mIsLoadingAdded = false;
    while (getItemCount() > 0) {
      remove(getItem(0));
    }
  }

  /**
   * Shows loading progress indicator
   */
  public void addLoadingFooter() {
    if (mIsLoadingAdded)
      return ;
    mIsLoadingAdded = true;
    add(new Volume());
  }

  /**
   * Hides the loading progress indicator
   */
  public void removeLoadingFooter() {
    if (!mIsLoadingAdded)
      return ;
    mIsLoadingAdded = false;

    int position = mBooks.size() - 1;
    Volume volume = getItem(position);

    if (volume != null) {
      mBooks.remove(position);
      notifyItemRemoved(position);
    }
  }

  /**
   * Gets a book at specified position
   */
  public Volume getItem(int position) {
    return mBooks.get(position);
  }

  /**
   * Displays Pagination retry footer view along with appropriate error resource
   *
   * @param show true to show the retry view
   * @param errResId string resource to display if page load fails
   */
  public void showRetry(boolean show, @Nullable int errResId) {
    mRetryPageLoad = show;
    notifyItemChanged(mBooks.size() - 1);
    mErrResId = errResId;
  }

  /**
   * Book ViewHolder
   */
  public static class BookVH extends RecyclerView.ViewHolder {
    @BindView(R.id.book_title) TextView mBookTitle;
    @BindView(R.id.book_authors) TextView mBookAuthors;
    @BindView(R.id.book_image) ImageView mBookImage;
    @BindView(R.id.book_description) TextView mBookDescription;

    public BookVH(ViewGroup view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }

  /**
   * Loading ViewHolder
   */
  protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.loadmore_progress) ProgressBar mProgressBar;
    @BindView(R.id.loadmore_retry) ImageButton mRetryBtn;
    @BindView(R.id.loadmore_errortxt) TextView mErrorTxt;
    @BindView(R.id.loadmore_errorlayout) LinearLayout mErrorLayout;

    public LoadingVH(View view) {
      super(view);
      ButterKnife.bind(this, view);
      mRetryBtn.setOnClickListener(this);
      mErrorLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      switch (view.getId()) {
        case R.id.loadmore_retry:
        case R.id.loadmore_errorlayout:
          showRetry(false, 0);
          mCallbacks.retryPageLoad();
          break;
      }
    }
  }
}
