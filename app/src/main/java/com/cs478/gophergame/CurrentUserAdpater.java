package com.cs478.gophergame;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class CurrentUserAdpater extends BaseAdapter {

    private Context mContext;
    private List<Integer> mThumbIds;
    private static final int PADDING = 2;

    public CurrentUserAdpater(Context c, List<Integer> ids) {
        mContext = c;
        this.mThumbIds = ids;
    }


    @Override
    public int getCount() {
        return mThumbIds.size();
    }

    @Override
    public Object getItem(int position) {
        return mThumbIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mThumbIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = (ImageView) convertView;

        // if convertView's not recycled, initialize some attributes
        if (imageView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 150));
            imageView.setPadding(PADDING, PADDING, PADDING, PADDING);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        imageView.setImageResource(mThumbIds.get(position));
        imageView.setTag(mThumbIds.get(position));
        return imageView;
    }

}
