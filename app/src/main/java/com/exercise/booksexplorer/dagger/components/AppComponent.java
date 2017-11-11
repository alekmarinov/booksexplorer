/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/9/2017
 * Description:
 */
package com.exercise.booksexplorer.dagger.components;

import android.app.Application;
import android.content.res.Resources;

import com.exercise.booksexplorer.dagger.modules.AppModule;
import com.exercise.booksexplorer.dagger.modules.BookModule;
import com.google.api.services.books.Books;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class, BookModule.class} )
public interface AppComponent {
  Application application();
  Resources resources();
  Books books();
}
