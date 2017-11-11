/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/10/2017
 * Description:
 */

package com.exercise.booksexplorer.search;

import android.arch.paging.TiledDataSource;
import android.util.Log;

import com.google.api.services.books.Books;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookDataSource extends TiledDataSource<Volume> {
  private static final String TAG = BookDataSource.class.getSimpleName();
  private Books mBooks;
  private String mQuery;
  private Volumes mVolumes;
  private Books.Volumes.List mVolumesList;

  public BookDataSource(Books books, String query) {
    mBooks = books;
    mQuery = query;
    loadRange(0, 20);
    try {
      mVolumesList = mBooks.volumes().list(mQuery);
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
      // FIXME: handle the exception
    }
  }

  @Override public int countItems() {
    return mVolumes.getTotalItems();
  }

  @Override public List<Volume> loadRange(int startPosition, int count) {
    try {
      mVolumesList.setStartIndex(Long.valueOf(startPosition));
      mVolumesList.setMaxResults(Long.valueOf(count));
      return mVolumesList.execute().getItems();
    } catch (IOException e) {
      Log.e(TAG, e.getMessage(), e);
      // FIXME: handle the exception
    }
    return null;
  }
}
