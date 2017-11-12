/*
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: Book details
 */
package com.exercise.booksexplorer.books;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.exercise.booksexplorer.BaseActivity;
import com.exercise.booksexplorer.R;
import com.exercise.booksexplorer.databinding.ActivityBookDetailsBinding;
import com.exercise.booksexplorer.util.StringUtils;
import com.google.api.services.books.model.Volume;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Activity displaying the details of a single book
 */
public class BookDetailsActivity extends BaseActivity {
    private static final String TAG = BookDetailsActivity.class.getSimpleName();
    private ActivityBookDetailsBinding mDetailsBinding;
    private Disposable mDisposable;

    public enum Param {
        /**
         * The id of a volume item
         */
        VOLUME_ID
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setWindowAnimations(android.R.style.Animation_Activity);
        mDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_book_details);

        setSupportActionBar(mDetailsBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        final String bookId = getIntent().getStringExtra(Param.VOLUME_ID.name());

        mDisposable = Observable.create(new ObservableOnSubscribe<Volume>() {
            @Override
            public void subscribe(ObservableEmitter<Volume> e) throws Exception {
                e.onNext(getBooks().volumes().get(bookId).execute());
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Volume>() {
                            @Override
                            public void accept(Volume volume) throws Exception {
                                updateVolume(volume);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Log.e(TAG, throwable.getMessage(), throwable);
                                showError(R.string.error_unknown);
                            }
                        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void onStop() {
        super.onStop();
        Log.i(TAG,".onStop");
        if (mDisposable != null)
            mDisposable.dispose();
    }

    private void updateVolume(Volume volume) {
        if (volume.getVolumeInfo().getImageLinks() != null)
            Glide.with(this).load(volume.getVolumeInfo().getImageLinks().getThumbnail()).into(mDetailsBinding.bookThumbnailImageview);
        mDetailsBinding.bookAuthorsTextview.setText(StringUtils.concat(volume.getVolumeInfo().getAuthors()));
        mDetailsBinding.bookTitleTextview.setText(volume.getVolumeInfo().getTitle());
        mDetailsBinding.bookPublisherTextview.setText(volume.getVolumeInfo().getPublisher());
        mDetailsBinding.bookDescription.setText(volume.getVolumeInfo().getDescription());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(volume.getVolumeInfo().getTitle());
    }

    public static Intent makeIntent(Context context, Volume book) {
        Intent intent = new Intent(context, BookDetailsActivity.class);
        intent.putExtra(Param.VOLUME_ID.name(), book.getId());
        return intent;
    }
}
