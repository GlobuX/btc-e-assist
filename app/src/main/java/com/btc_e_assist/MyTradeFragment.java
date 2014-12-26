package com.btc_e_assist;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MyTradeFragment extends Fragment {
    private static CustomExpandAdapter adapter;
    private static volatile DataBox dataBox = new DataBox();
    private Context mContext;
    private TradeControl tradeControl;
    private ExpandableListView tradesList;
    private TextView noData;
    private String currentFragmentName = "";
    private SecondThread secondThread;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        currentFragmentName = getTag();
        tradeControl = TradeControl.getInstance();
        CommonHelper.showPasswordDialog(mContext);
        update();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_expandable_list,
                container, false);
        noData = (TextView) rootView
                .findViewById(R.id.standardFragmentExpNoData);
        String[] groupFrom = {"amount", "name0", "name1"};
        int[] groupTo = {R.id.itemMyTradeGroupAmount,
                R.id.itemMyTradeGroupName1, R.id.itemMyTradeGroupName2};

        String[] childFrom = {"rate", "total", "date"};
        int[] childTo = {R.id.itemMyTradeChildPriceValue,
                R.id.itemMyTradeChildTotalValue, R.id.itemMyTradeChildTimeValue};

        String[] aliases = {"sell", "buy"};
        int[] drawIds = {R.drawable.sell_icon, R.drawable.buy_icon};
        adapter = new CustomExpandAdapter(mContext, dataBox.data1, groupFrom,
                groupTo, dataBox.data2, childFrom, childTo,
                R.layout.item_mytrade_group_list,
                R.layout.item_mytrade_child_list, R.id.itemMyTradeGroupAmount,
                R.id.itemMyTradeChildViewDivider, aliases, drawIds);
        tradesList = (ExpandableListView) rootView
                .findViewById(R.id.standardFragmentExpList);
        tradesList.setAdapter(adapter);
        checkNoData();
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void checkNoData() {
        if (noData != null) {
            if (dataBox.data1.size() == 0) {
                noData.setVisibility(View.VISIBLE);
            } else {
                noData.setVisibility(View.GONE);
            }
        }
    }

    private void update() {
        if (!tradeControl.tradeApi.isKeysInstalled()) {
            CommonHelper.makeToastNoKeys(mContext);
            return;
        }
        if (secondThread != null) {
            secondThread.cancel(false);
        }
        secondThread = new SecondThread();
        secondThread.execute();
        CommonHelper.makeToastUpdating(mContext, currentFragmentName);
    }

    private class SecondThread extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            return tradeControl.loadTradeHistoryData();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!isAdded()) {
                return;
            }
            if (result.booleanValue()
                    && tradeControl.setTradeHistoryData(dataBox)) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                    checkNoData();
                    CommonHelper
                            .makeToastUpdated(mContext, currentFragmentName);
                }
            } else {
                CommonHelper.makeToastErrorConnection(mContext,
                        currentFragmentName);
            }
        }
    }
}
