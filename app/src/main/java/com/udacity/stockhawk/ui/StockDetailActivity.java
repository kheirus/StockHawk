package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.utils.Constants;
import com.udacity.stockhawk.utils.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import au.com.bytecode.opencsv.CSVReader;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity {

    @BindView(R.id.tv_name)
    TextView tvName;
    @BindView(R.id.tv_price_detail)
    TextView tvPrice;
    @BindView(R.id.tv_change_detail)
    TextView tvChange;
    @BindView(R.id.chart)
    LineChart mChart;

    Context mContext;

    private String symbol;

    //Colors for the graph
    private int axisColor;
    private int chartColor;

    private  DecimalFormat dollarFormatWithPlus;
    private  DecimalFormat percentageFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        symbol = getIntent().getExtras().getString(Constants.EXTRA_SYMBOL);
        mContext = this;

        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormatWithPlus.setPositivePrefix("+$");
        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

        setToolBar(symbol);

        axisColor = ContextCompat.getColor(getApplicationContext(),R.color.white);
        chartColor = ContextCompat.getColor(getApplicationContext(),R.color.chart_color);

        // Launch the process that retrieve data and draw the graphic
        new DrawStockAsyncTask().execute(symbol);


    }

    // Set the toolbar
    public void setToolBar(String title) {
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    // Set the back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // AsyncTask that retrieve data and draw chart in a separate thread
    class DrawStockAsyncTask extends AsyncTask<String, Void, Cursor>{


        @Override
        protected Cursor doInBackground(String... strings) {
            String symbol = strings[0];

            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            Cursor cursor = contentResolver.query(Contract.Quote.makeUriForStock(symbol),
                    null,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL
            );
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            super.onPostExecute(cursor);

            Cursor mData = cursor;
            drawChart(mData);
        }

        // Draw the graphic of designed symbol
        private void drawChart(Cursor cursor) {

            assert cursor != null;
            cursor.moveToFirst();

            // Update the view
            updateView(cursor);

            String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
            String history = cursor.getString(Contract.Quote.POSITION_HISTORY);

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
            dataSet.setLineWidth(2f);
            //dataSet.setCircleColor(chartColor);

            LineData lineData = new LineData(dataSet);
            mChart.fitScreen();
            mChart.setClickable(false);
            mChart.setData(lineData);
            mChart.setDescription(null);
            mChart.getLegend().setTextColor(chartColor);


            //mChart.getDescription().setTextColor(chartColor);
            mChart.animateX(2000, Easing.EasingOption.Linear);

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

        // Update other views details about the quote
        private void updateView(Cursor cursor){
            assert cursor != null;
            cursor.moveToFirst();

            String price = cursor.getString(Contract.Quote.POSITION_PRICE);
            String name = cursor.getString(Contract.Quote.POSITION_NAME);
            float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);


            if (rawAbsoluteChange > 0) {
                tvChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                tvChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

            String changeIcon = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(mContext)
                    .equals(mContext.getString(R.string.pref_display_mode_absolute_key))) {
                tvChange.setText(changeIcon);
            } else {
                tvChange.setText(percentage);
            }

            tvPrice.setText("$"+price);
            tvName.setText(name);
        }
    }

}
