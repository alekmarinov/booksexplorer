/**
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: A base for all application activities
 */
package com.exercise.booksexplorer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.api.services.books.Books;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected BooksExplorerApplication getApp() {
        return (BooksExplorerApplication) getApplication();
    }

    protected Books getBooks() {
        return getApp().getAppComponent().books();
    }

    protected void showError(String errorMessage) {
        View rootView = findViewById(R.id.app_bar);
        final Snackbar snack = Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_INDEFINITE);
        snack.setAction(R.string.snackbar_action_dismiss, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snack.dismiss();
            }
        });

        snack.show();
    }

    protected void showError(int errResId) {
        showError(getString(errResId));
    }


    protected void startActivityWithTransition(Intent intent) {
        startActivity(
                intent,
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }
}
