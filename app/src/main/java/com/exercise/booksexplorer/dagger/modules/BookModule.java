/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/9/2017
 * Description:
 */
package com.exercise.booksexplorer.dagger.modules;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.exercise.booksexplorer.R;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class BookModule {
  @Provides
  @Singleton
  JsonFactory providesJsonFactory() {
    return JacksonFactory.getDefaultInstance();
  }

  @Provides
  @Singleton
  HttpTransport providesHttpTransport() {
    return new com.google.api.client.http.javanet.NetHttpTransport();
  }

  @Provides
  @Singleton
  Books providesBooks(HttpTransport httpTransport, JsonFactory jsonFactory, Application application) {
    String appName = application.getResources().getString(R.string.app_name);
    String apiKey = application.getResources().getString(R.string.google_api_key);
    return new Books.Builder(httpTransport, jsonFactory, null)
            .setApplicationName(appName)
            .setGoogleClientRequestInitializer(new BooksRequestInitializer(apiKey))
            .build();
  }
}
