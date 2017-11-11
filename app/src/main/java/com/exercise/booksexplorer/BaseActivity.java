/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/9/2017
 * Description:
 */
package com.exercise.booksexplorer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.api.services.books.Books;

public class BaseActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  protected BooksExplorerApplication getApp() {
    return (BooksExplorerApplication)getApplication();
  }

  protected Books getBooks() {
    return getApp().getAppComponent().books();
  }
}
