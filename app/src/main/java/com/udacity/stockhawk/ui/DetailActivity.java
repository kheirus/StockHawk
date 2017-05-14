package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_symbol)
    TextView tvSymbol;
    @BindView(R.id.tv_price_detail)
    TextView tvPrice;
    @BindView(R.id.tv_change_detail)
    TextView tvChange;
    @BindView(R.id.chart)
    LineChart mChart;

    private String symbol;

    //Colors
    private int axisColor;
    private int chartColor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(Constants.EXTRA_SYMBOL);

        updateView();
        setToolBar(symbol);

        axisColor = ContextCompat.getColor(getApplicationContext(),R.color.white);
        chartColor = ContextCompat.getColor(getApplicationContext(),R.color.chart_color);
        drawStockChart(symbol);


    }

    private void updateView(){

        tvName.setText(symbol);
        //  tvSymbol.setText(symbol);
//        tvPrice.setText("21");
//        tvChange.setText("18");
    }


    private void drawStockChart(String symbole) {
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor cursor = contentResolver.query(Contract.Quote.makeUriForStock(symbole),
                null,
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL
        );
        assert cursor != null;
        cursor.moveToFirst();
        String symbol = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL));
        String history = cursor.getString(cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        List<Entry> entries = new ArrayList<>();
        CSVReader reader = new CSVReader(new StringReader(history));
        String[] nextLine;
        final List<Long> xAxisValues = new ArrayList<>();
        int xAxisPosition = 0;
        cursor.close();

        try {
            while ((nextLine = reader.readNext()) != null) {
                xAxisValues.add(Long.valueOf(nextLine[0]));
                Entry entry = new Entry(
                        xAxisPosition, // timestamp
                        Float.valueOf(nextLine[1])  // symbol value
                );
                entries.add(entry);
                xAxisPosition++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //style
        LineDataSet dataSet = new LineDataSet(entries, symbol);
        dataSet.setColors(chartColor);
        dataSet.setDrawCircles(false);
        //dataSet.setCircleColor(chartColor);

        LineData lineData = new LineData(dataSet);
        mChart.fitScreen();
        mChart.setClickable(false);
        mChart.setData(lineData);
        mChart.setDescription(null);
        mChart.getLegend().setTextColor(chartColor);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(axisColor);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(axisColor);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextColor(axisColor);
        //style fin
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date(xAxisValues.get(xAxisValues.size() - (int) value - 1));
                return new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH)
                        .format(date);
            }
        });

        mChart.invalidate();
    }


    public void setToolBar(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
