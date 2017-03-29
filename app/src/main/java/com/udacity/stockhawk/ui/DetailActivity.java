package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements  LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_symbol)
    TextView tvSymbol;
    @BindView(R.id.tv_price_detail)
    TextView tvPrice;
    @BindView(R.id.tv_change_detail)
    TextView tvChange;
    @BindView(R.id.chart)
    LineChart chart;


    public static final String EXTRA_INTENT = "extra_detail";
    private Uri stockUri;
    private static int LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        stockUri = getIntent().getData();
        updateView();
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

    }

    private void updateView(){
        tvName.setText("Apple inc");
        tvSymbol.setText("AAPL");
        tvPrice.setText("21");
        tvChange.setText("18");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                stockUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                null
                );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()){
            Log.d(Utils.TAG, "onLoadFinished: NAME "+data.getString(Contract.Quote.POSITION_NAME));
            Log.d(Utils.TAG, "onLoadFinished: HISTORY "+data.getString(Contract.Quote.POSITION_HISTORY));

            List<Entry> entries = new ArrayList();
            float i ;
            for (i=0; i<1000; i++){
                entries.add(new Entry(i,i));
            }
            LineDataSet dataSet = new LineDataSet(entries,"toto");
            dataSet.setColor(R.color.colorAccent);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void drawStockChart() {

    }

}
