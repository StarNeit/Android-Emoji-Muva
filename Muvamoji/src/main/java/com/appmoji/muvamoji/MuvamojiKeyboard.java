package com.appmoji.muvamoji;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.util.DisplayMetrics;

import com.appmoji.muvamoji.memeicon.Memeicon;
import com.appmoji.muvamoji.memeicon.MemeiconFormat;

import java.util.List;

/**
 * Created by kou on 14/10/29.
 */
public class MuvamojiKeyboard extends Keyboard {
    private static MemeiconFormat EMOJI_FORMAT = null;

    private final Row row;
    private float iconSize;
    private Context mContext;

    private int displayWidth;
    private int columnCount;
    private int rowCount;

    /**
     * Construct object.
     * @param context      Application context.
     */
    private MuvamojiKeyboard(Context context)
    {
        super(context, R.xml.keyboard);
        mContext = context;

        if(EMOJI_FORMAT == null)
            EMOJI_FORMAT = MemeiconFormat.toFormat(context.getResources().getString(R.string.emoji_format_png));

        getKeys().clear();

        row = new Row(this);
        row.defaultWidth = getKeyWidth();
        row.defaultHeight = getKeyHeight();
        row.defaultHorizontalGap = getHorizontalGap();
        row.verticalGap = getVerticalGap();
        row.rowEdgeFlags = 0;
        row.mode = 0;

        iconSize = context.getResources().getDimension(R.dimen.ime_key_icon_size);

        columnCount = Math.max(columnCount, 1);
        rowCount = Math.max(rowCount, 1);
    }

    /**
     * Initialize keyboard.
     * @param emojies   Emoji list.
     */
    public void initialize(List<Memeicon> emojies)
    {
        reset();

        final int count = emojies.size();
        final List<Key> keys = getKeys();
        final int leftMargin = (displayWidth - columnCount * getKeyWidth() - (columnCount - 1) * getHorizontalGap()) / 2;
        for(int i = 0;  i < count;  ++i)
        {
            final Key newKey = createKey(emojies.get(i));
            newKey.x = i % columnCount * (getKeyWidth() + getHorizontalGap()) + leftMargin;
            newKey.y = i / columnCount * (getKeyHeight() + getVerticalGap());

            keys.add(newKey);
        }
    }

    /**
     * Reset keyboard.
     */
    public void reset()
    {
        getKeys().clear();
    }

    /**
     * Get key count max.
     * @return  Key count max.
     */
    public int getKeyCountMax()
    {
        return columnCount * rowCount;
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser)
    {
        final DisplayMetrics metrics = res.getDisplayMetrics();
        displayWidth = metrics.widthPixels;

        columnCount = displayWidth / (getKeyWidth() + getHorizontalGap());
        rowCount = res.getInteger(R.integer.ime_keyboard_row_count);

        final Row newRow = super.createRowFromXml(res, parser);
        newRow.defaultWidth = displayWidth;
        newRow.defaultHeight = rowCount * (getKeyHeight() + getVerticalGap());

        return newRow;
    }

    /**
     * Create key to keyboard.
     * @param emoji         Emoji.
     * @return              New key.
     */
    private Key createKey(Memeicon emoji)
    {
        final Key newKey = new Key(row);

        final List<Integer> codes = emoji.getCodes();
        final int codesSize = codes.size();
        newKey.codes = new int[codesSize];
        for(int i = 0;  i < codesSize;  ++i)
            newKey.codes[i] = codes.get(i);

        final BitmapDrawable icon;
        if (emoji.isGif()) {
            icon = emoji.getDrawable(MemeiconFormat.GIF);
        } else {
            icon = emoji.getDrawable(MemeiconFormat.PNG);
        }

        icon.setTargetDensity((int)(icon.getBitmap().getDensity() * iconSize / icon.getIntrinsicWidth()));
        newKey.icon = icon;

        newKey.popupCharacters = emoji.getFilename();

        return newKey;
    }

    /**
     * Create new keyboard.
     * @param context   Application context.
     * @return          New keyboard.
     */
    public static MuvamojiKeyboard create(Context context)
    {
        context = context.getApplicationContext();

        final MuvamojiKeyboard newKeyboard = new MuvamojiKeyboard(context);

        return newKeyboard;
    }
}
