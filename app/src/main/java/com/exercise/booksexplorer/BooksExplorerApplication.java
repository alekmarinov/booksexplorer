/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: The application class
 */
package com.exercise.booksexplorer;

import android.app.Application;

import com.exercise.booksexplorer.dagger.components.AppComponent;
import com.exercise.booksexplorer.dagger.components.DaggerAppComponent;
import com.exercise.booksexplorer.dagger.modules.AppModule;

import io.reactivex.internal.functions.Functions;
import io.reactivex.plugins.RxJavaPlugins;

public class BooksExplorerApplication extends Application {
  private AppComponent mAppComponent;

  public void onCreate() {
    super.onCreate();

    mAppComponent = DaggerAppComponent.builder()
            .appModule(new AppModule(this))
            .build();

    RxJavaPlugins.setErrorHandler(Functions.<Throwable>emptyConsumer());
  }

  public AppComponent getAppComponent() {
    return mAppComponent;
  }
}
