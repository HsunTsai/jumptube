package com.turtle.hsun.jumptube.Custom.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.turtle.hsun.jumptube.R;
import com.turtle.hsun.jumptube.Utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SuggestAdapter {
    public static void set(String suggestList, Context context, final SearchView searchView) {
        LogUtil.show("suggestList", suggestList);
        try {
            JSONArray suggestList_ = new JSONArray(suggestList);
            ArrayList<String> suggestions = new ArrayList<>();
            JSONArray suggestListArray = (suggestList_.length() == 1) ?
                    (JSONArray) suggestList_.get(0) : (JSONArray) suggestList_.get(1);
            for (int i = 0; i < suggestListArray.length(); i++) {
                String suggestion = suggestListArray.get(i).toString();
                if (null != suggestion) {
                    suggestions.add(suggestion);
                }
                if (i == 10) break; //limit = 10
            }
            String[] columnNames = {"_id", "suggestion"};
            MatrixCursor cursor = new MatrixCursor(columnNames);
            String[] temp = new String[2];
            int id = 0;
            for (String item : suggestions) {
                if (item != null) {
                    temp[0] = Integer.toString(id++);
                    temp[1] = item;
                    cursor.addRow(temp);
                }
            }
            CursorAdapter cursorAdapter = new CursorAdapter(context, cursor, false) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(context).inflate(R.layout.component_search_suggestion_list_item, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    final Button suggest = (Button) view.findViewById(R.id.bt_suggest);
                    String body = cursor.getString(cursor.getColumnIndexOrThrow("suggestion"));
                    suggest.setText(body);
                    suggest.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            searchView.setQuery(suggest.getText(), true);
                            searchView.clearFocus();
                        }
                    });
                }
            };
            searchView.setSuggestionsAdapter(cursorAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
