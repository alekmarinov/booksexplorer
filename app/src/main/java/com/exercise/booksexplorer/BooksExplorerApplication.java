/**
 * Copyright (c) 2017, Polygon Group
 * Project:     BooksExplorer
 * Author:      alekm
 * Date:        11/9/2017
 * Description:
 */
package com.exercise.booksexplorer;

import android.app.Application;

import com.exercise.booksexplorer.dagger.components.AppComponent;
import com.exercise.booksexplorer.dagger.components.DaggerAppComponent;
import com.exercise.booksexplorer.dagger.modules.AppModule;

public class BooksExplorerApplication extends Application {
  private AppComponent mAppComponent;

  public void onCreate() {
    super.onCreate();

    mAppComponent = DaggerAppComponent.builder()
            .appModule(new AppModule(this))
            .build();
  }

  public AppComponent getAppComponent() {
    return mAppComponent;
  }
}
