/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/9/2017
 * Description:
 */

package com.exercise.booksexplorer.dagger.modules;

import android.app.Application;
import android.content.res.Resources;
import android.util.Log;

import com.exercise.booksexplorer.R;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
  private static final String TAG = AppModule.class.getSimpleName();

  Application mApplication;

  public AppModule(Application application) {
    mApplication = application;
  }

  @Provides
  @Singleton
  Application providesApplication() {
    return mApplication;
  }

  @Provides
  @Singleton
  Resources providesResources(Application application) {
    return application.getResources();
  }
}
