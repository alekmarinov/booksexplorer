/*
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: A component to the application scope
 */
package com.exercise.booksexplorer.dagger.components;

import android.app.Application;

import com.exercise.booksexplorer.dagger.modules.AppModule;
import com.exercise.booksexplorer.dagger.modules.BookModule;
import com.google.api.services.books.Books;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { AppModule.class, BookModule.class} )
public interface AppComponent {
  Application application();
  Books books();
}
