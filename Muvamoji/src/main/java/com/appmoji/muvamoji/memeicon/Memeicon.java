package com.appmoji.muvamoji.memeicon;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class Memeicon {

    private final List<Integer> codes = new ArrayList<Integer>();
    private final WeakReference<Bitmap>[] bitmaps = new WeakReference[MemeiconFormat.values().length];

    private Resources res;
    private String category;
    private String codetext;
    private String filename;
    private boolean isGif = false;

    public Memeicon(Resources res, String category, int id_num, int codes) {
        this.res = res;
        this.category = category;
        this.filename = category + id_num;

        initialize(res, codes);
    }

    public void setIsGif(boolean value) {
        this.isGif = value;
    }

    public boolean isGif() { return this.isGif; }

    public String getFilename() { return this.filename; }

    /**
     * Get category name.
     * @return  Category name.
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * Get emoji codes.
     * @return  Emoji codes.
     */
    public List<Integer> getCodes()
    {
        return codes;
    }

    /**
     * Get image of format.
     * @param format    Image format.
     * @return          Image.
     */
    public BitmapDrawable getDrawable(MemeiconFormat format)
    {
        final int index = format.ordinal();

        // Load image.
        if(bitmaps[index] == null || bitmaps[index].get() == null)
        {
            final Bitmap newBitmap = loadBitmap(format);
            bitmaps[index] = new WeakReference<Bitmap>(newBitmap.copy(newBitmap.getConfig(), true));
            newBitmap.recycle();
        }

        // Create drawable.
        final BitmapDrawable result = new BitmapDrawable(res, bitmaps[index].get());
        result.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());

        return result;
    }

    public String getImageFilePath(MemeiconFormat format)
    {
        return format.getRelativeDir() + "/" + filename + format.getExtension();
    }

    /**
     * Load bitmap from local storage.
     * @param format    Emoji format.
     * @return          Bitmap.
     */
    Bitmap loadBitmap(MemeiconFormat format)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPurgeable = true;
        Bitmap result = null;

        // Load bitmap from file.
        try
        {
            final InputStream is = res.getAssets().open(getImageFilePath(format));
            result = BitmapFactory.decodeStream(is, null, options);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        // If load failed, use default image.
        if(result == null)
        {
            // If file not found, use default image.
            try {
                final InputStream is = res.getAssets().open(format.getRelativeDir() + "/not_found" + format.getExtension());
                result = BitmapFactory.decodeStream(is, null, options);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Initialize emoji object.
     * @param res  Emoji kind.
     */
    void initialize(Resources res)
    {
        this.res = res;

        // Set codes.
        final int count = codetext.codePointCount(0, codetext.length());
        int next = 0;
        for(int i = 0;  i < count;  ++i)
        {
            final int codePoint = codetext.codePointAt(next);
            next += Character.charCount(codePoint);

            // Ignore Variation selectors.
            if(Character.getType(codePoint) == Character.NON_SPACING_MARK)
                continue;

            codes.add(codePoint);
        }

        // Adjustment text.
        if(codes.size() < count)
        {
            codetext = "";
            for(Integer codePoint : codes)
                codetext += String.valueOf(Character.toChars(codePoint));
        }
    }

    /**
     * Initialize emoji object.
     * @param res       Resources object.
     * @param codes     Original emoji code.
     */
    void initialize(Resources res, int codes)
    {
        codetext = new String(Character.toChars(codes));

        initialize(res);
    }

    /**
     * Check emoji has codes.
     * @return  true if emoji has code.
     */
    boolean hasCodes()
    {
        return !codes.isEmpty() || filename != null;
    }
}
