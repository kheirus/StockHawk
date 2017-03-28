package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.utils.Utils;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 0;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;
    private StockAdapter adapter;

    @Override
    public void onClick(String symbol) {
        Timber.d("Symbol clicked: %s", symbol);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                Log.d(Utils.TAG, "onSwiped: "+PrefUtils.getStocks(MainActivity.this).size());
                if (PrefUtils.getStocks(MainActivity.this).size() == 0){
                    updateEmptyView();
                }
            }
        }).attachToRecyclerView(stockRecyclerView);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onRefresh() {
        if (networkUp()){
            QuoteSyncJob.syncImmediately(this);
        }
        else {
            Utils.showLongToastMessage(this, getString(R.string.error_no_network));
        }



//        if (!networkUp() && adapter.getItemCount() == 0) {
//            swipeRefreshLayout.setRefreshing(false);
//            error.setText(getString(R.string.error_no_network));
//            error.setVisibility(View.VISIBLE);
//        } else if (!networkUp()) {
//            swipeRefreshLayout.setRefreshing(false);
//            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
//        } else if (PrefUtils.getStocks(this).size() == 0) {
//            swipeRefreshLayout.setRefreshing(false);
//            error.setText(getString(R.string.error_no_stocks));
//            error.setVisibility(View.VISIBLE);
//        } else {
//            error.setVisibility(View.GONE);
//        }

    }

    private void updateEmptyView(){
        swipeRefreshLayout.setVisibility(View.GONE);

        int  msg ;
        @QuoteSyncJob.StockStatus int status = PrefUtils.getStockStatus(this);
        Log.d(Utils.TAG, "updateView: "+status);
        switch (status){

            case QuoteSyncJob.STOCK_STATUS_EMPTY:
                msg = R.string.error_empty_stock;
                break;

            case QuoteSyncJob.STOCK_STATUS_SERVER_DOWN:
                msg = R.string.error_server_down;
                break;

            case QuoteSyncJob.STOCK_STATUS_SERVER_INVALID:
                msg = R.string.error_server_invalid;
                break;

            case QuoteSyncJob.STOCK_STATUS_UNKNOWN:
                msg = R.string.error_unknown;
                break;
            default:
                msg = R.string.error_empty_stock;

        }

        if (!networkUp()) msg = R.string.toast_no_connectivity;
        if (PrefUtils.getStocks(this).size() == 0) msg = R.string.error_no_stocks;
        error.setText(msg);
        error.setVisibility(View.VISIBLE);


    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        if (data.getCount() != 0) {
            error.setVisibility(View.GONE);
        }else {
            updateEmptyView();
        }
        adapter.setCursor(data);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.setCursor(null);
        updateEmptyView();
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
