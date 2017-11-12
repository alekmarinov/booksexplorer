/*
 * Project:     BooksExplorer
 * Date:        11/9/2017
 * Description: A scrolling listener triggering an event when the user scrolls near to the end of the currently loaded page.
 *              The source code is politely borrowed by Suleiman Ali Shakir
 *              https://github.com/Suleiman19/Android-Pagination-with-RecyclerView
 */
package com.exercise.booksexplorer.books;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Pagination
 * Created by Suleiman19 on 10/15/16.
 * Copyright (c) 2016. Suleiman Ali Shakir. All rights reserved.
 */
public abstract class PaginationScrollListener extends RecyclerView.OnScrollListener {
    private LinearLayoutManager layoutManager;

    PaginationScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

        if (!isLoading() && !isLastPage()) {
            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 50
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }
    }

    protected abstract void loadMoreItems();

    public abstract boolean isLastPage();

    public abstract boolean isLoading();

}
