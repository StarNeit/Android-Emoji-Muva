package com.appmoji.muvamoji;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.appmoji.muvamoji.memeicon.Memeicon;
import com.appmoji.muvamoji.memeicon.MemeiconFormat;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class CatalogAdapter extends BaseAdapter
{
    private final Context context;
    private final MemeiconFormat format;
    private final ArrayList<Memeicon> emojies;

    public CatalogAdapter (Context context, List<Memeicon> emojies, MemeiconFormat format)
    {
        this.context = context;
        this.format = format;
        this.emojies = new ArrayList<Memeicon>(emojies);
    }

    @Override
    public int getCount()
    {
        return emojies.size();
    }

    @Override
    public Object getItem(int position)
    {
        return emojies.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView imageView = (ImageView)convertView;

        if (imageView == null)
        {
            imageView = new ImageView(context);
            int size = (int)context.getResources().getDimension(R.dimen.catalog_icon_size);
            imageView.setLayoutParams(new AbsListView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        if (format.equals(MemeiconFormat.GIF)) {
            Glide.with(context)
                    .load(Uri.parse("file:///android_asset/" + emojies.get(position).getImageFilePath(format)))
                    .into(imageView);
        }
        else {
            imageView.setImageDrawable(emojies.get(position).getDrawable(format));
        }

        return imageView;
    }
}
