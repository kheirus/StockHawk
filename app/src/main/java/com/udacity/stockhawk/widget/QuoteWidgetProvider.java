package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.ui.DetailActivity;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.utils.Constants;

/**
 * Created by kheirus on 20/05/2017.
 */

public class QuoteWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_default);

//        Intent intent = new Intent(context, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
//        views.setOnClickPendingIntent(R.id.widget, pendingIntent);


        // Set up my collection
        setRemoteAdapter(context, views);

        Intent intentStockGraph = new Intent(context, DetailActivity.class);
        PendingIntent pendingIntent = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(intentStockGraph)
                .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private void setRemoteAdapter(Context context, RemoteViews views) {
        views.setRemoteAdapter(R.id.lv_widget_list, new Intent(context, QuoteWidgetRemoteViewsService.class));
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        // Receive Broadcast About Stock Data Update
        super.onReceive(context, intent);
        //if (intent.getAction().equals(Constants.ACTION_STOCK_UPDATE)){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,getClass()));
            // update All Widgets
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.lv_widget_list);
        //}
    }

}
