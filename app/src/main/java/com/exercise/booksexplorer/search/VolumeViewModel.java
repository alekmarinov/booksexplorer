/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/10/2017
 * Description:
 */
package com.exercise.booksexplorer.search;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.LivePagedListProvider;
import android.arch.paging.PagedList;

import com.google.api.services.books.Books;
import com.google.api.services.books.model.Volume;

import java.io.IOException;
import java.util.List;

public class VolumeViewModel extends ViewModel {

  public LiveData<PagedList<Volume>> volumeList;

  public void init(Books books, String query) throws IOException {

    Books.Volumes.List volumesList = books.volumes().list(query);
    volumesList.execute();
  }
}
