/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: Book details
 */
package com.exercise.booksexplorer.books;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.exercise.booksexplorer.BaseActivity;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.databinding.ActivityBookDetailsBinding;
import com.exercise.booksexplorer.util.StringUtils;
import com.google.api.services.books.model.Volume;

import java.io.IOException;

public class BookDetailsActivity extends BaseActivity {
    private static final String TAG = BookDetailsActivity.class.getSimpleName();
    private ActivityBookDetailsBinding mDetailsBinding;

    public enum Param {
        ID
    }

    /**
     * Load book callbacks interface
     */
    private interface LoadCallbacks {
        /**
         * Called back on load success
         *
         * @param volume
         */
        public void onSuccess(Volume volume);

        /**
         * Called back on load failure
         *
         * @param throwable
         */
        public void onFailure(Throwable throwable);
    }

    /**
     * Implements load responses
     */
    private LoadCallbacks mOnLoad = new LoadCallbacks() {
        @Override
        public void onSuccess(Volume volume) {
            updateVolume(volume);
        }

        @Override
        public void onFailure(Throwable throwable) {
            showError(R.string.error_unknown);
        }
    };


    private void updateVolume(Volume volume) {
        if (volume.getVolumeInfo().getImageLinks() != null)
            Glide.with(this).load(volume.getVolumeInfo().getImageLinks().getThumbnail()).into(mDetailsBinding.bookThumbnailImageview);
        mDetailsBinding.bookAuthorsTextview.setText(StringUtils.concat(volume.getVolumeInfo().getAuthors()));
        mDetailsBinding.bookTitleTextview.setText(volume.getVolumeInfo().getTitle());
        mDetailsBinding.bookPublisherTextview.setText(volume.getVolumeInfo().getPublisher());
        mDetailsBinding.bookDescription.setText(volume.getVolumeInfo().getDescription());

        getSupportActionBar().setTitle(volume.getVolumeInfo().getTitle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(android.R.style.Animation_Activity);
        mDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_details);

        setSupportActionBar(mDetailsBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
//            actionBar.setIcon(R.drawable.app_icon);
        }

        final String bookId = getIntent().getStringExtra(Param.ID.name());
        new LoadTask(mOnLoad).execute(bookId);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private class LoadTask extends AsyncTask<String, Integer, Volume> {
        private Throwable mThrowable;
        private LoadCallbacks mOnLoad;

        LoadTask(LoadCallbacks onLoad) {
            mOnLoad = onLoad;
        }

        @Override
        protected Volume doInBackground(String... bookId) {
            try {
                return getBooks().volumes().get(bookId[0]).execute();
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
        protected void onPostExecute(Volume volume) {
            if (mThrowable != null)
                mOnLoad.onFailure(mThrowable);
            else {
                mOnLoad.onSuccess(volume);
            }
        }
    }

    public static Intent makeIntent(Context context, Volume book) {
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(Param.ID.name(), book.getId());
        return intent;
    }
}
