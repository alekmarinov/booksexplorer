package com.exercise.booksexplorer.search;

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
import com.google.api.services.books.model.Volume;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final int ITEM = 0;
  private static final int LOADING = 1;

  private List<Volume> booksResult;
  private Context context;

  private boolean isLoadingAdded = false;
  private boolean retryPageLoad = false;

  private String errorMsg;
  private Callbacks mCallbacks;

  public interface Callbacks {
    void retryPageLoad();
  }

  public PaginationAdapter(Context context, Callbacks callbacks) {
    this.context = context;
    mCallbacks = callbacks;
    booksResult = new ArrayList<>();
  }

  public List<Volume> getBooks() {
    return booksResult;
  }

  public void setBooks(List<Volume> bookResults) {
    this.booksResult = bookResults;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    RecyclerView.ViewHolder viewHolder = null;
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());

    switch (viewType) {
      case ITEM:
        ViewGroup viewItem = (ViewGroup)inflater.inflate(R.layout.book_item, parent, false);
        viewHolder = new BookVH(viewItem);
        break;
      case LOADING:
        View viewLoding = inflater.inflate(R.layout.item_progress, parent, false);
        viewHolder = new LoadingVH(viewLoding);
        break;
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    Volume volume = booksResult.get(position);

    switch (getItemViewType(position)) {
      case ITEM:
        final BookVH bookVH = (BookVH) holder;
        Glide.with(context).load(volume.getVolumeInfo().getImageLinks().getSmall()).into(bookVH.mBookImage);
        bookVH.mBookTitle.setText(volume.getVolumeInfo().getTitle());
        bookVH.mBookDescription.setText(volume.getVolumeInfo().getSubtitle());
        break;

      case LOADING:
        LoadingVH loadingVH = (LoadingVH) holder;
        if (retryPageLoad) {
          loadingVH.mErrorLayout.setVisibility(View.VISIBLE);
          loadingVH.mProgressBar.setVisibility(View.GONE);

          loadingVH.mErrorTxt.setText(
                  errorMsg != null ?
                          errorMsg :
                          context.getString(R.string.error_msg_unknown));

        } else {
          loadingVH.mErrorLayout.setVisibility(View.GONE);
          loadingVH.mProgressBar.setVisibility(View.VISIBLE);
        }
        break;
    }
  }

  @Override
  public int getItemCount() {
    return booksResult == null ? 0 : booksResult.size();
  }

  @Override
  public int getItemViewType(int position) {
    return (position == booksResult.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
  }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

  public void add(Volume v) {
    booksResult.add(v);
    notifyItemInserted(booksResult.size() - 1);
  }

  public void remove(Volume v) {
    int position = booksResult.indexOf(v);
    if (position > -1) {
      booksResult.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void addLoadingFooter() {
    isLoadingAdded = true;
    add(new Volume());
  }

  public void removeLoadingFooter() {
    isLoadingAdded = false;

    int position = booksResult.size() - 1;
    Volume volume = getItem(position);

    if (volume != null) {
      booksResult.remove(position);
      notifyItemRemoved(position);
    }
  }

  public Volume getItem(int position) {
    return booksResult.get(position);
  }

  /**
   * Displays Pagination retry footer view along with appropriate errorMsg
   *
   * @param show
   * @param errorMsg to display if page load fails
   */
  public void showRetry(boolean show, @Nullable String errorMsg) {
    retryPageLoad = show;
    notifyItemChanged(booksResult.size() - 1);

    if (errorMsg != null) this.errorMsg = errorMsg;
  }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

  /**
   * Main list's content ViewHolder
   */
  public static class BookVH extends RecyclerView.ViewHolder {
    @BindView(R.id.book_image) ImageView mBookImage;
    @BindView(R.id.book_title) TextView mBookTitle;
    @BindView(R.id.book_description) TextView mBookDescription;

    public BookVH(ViewGroup view) {
      super(view);
      ButterKnife.bind(this, view);
    }
  }

  protected class LoadingVH extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ProgressBar mProgressBar;
    private ImageButton mRetryBtn;
    private TextView mErrorTxt;
    private LinearLayout mErrorLayout;

    public LoadingVH(View itemView) {
      super(itemView);

      // FIXME: ButterKnife
      mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
      mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
      mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
      mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);

      mRetryBtn.setOnClickListener(this);
      mErrorLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
      switch (view.getId()) {
        case R.id.loadmore_retry:
        case R.id.loadmore_errorlayout:

          showRetry(false, null);
          mCallbacks.retryPageLoad();

          break;
      }
    }
  }

}
