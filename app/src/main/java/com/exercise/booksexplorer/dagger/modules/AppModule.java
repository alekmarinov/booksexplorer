/*
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: General application module
 */
package com.exercise.booksexplorer.dagger.modules;

import android.app.Application;
import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
  private Application mApplication;

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
