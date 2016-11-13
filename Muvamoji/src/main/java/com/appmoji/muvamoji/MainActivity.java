package com.appmoji.muvamoji;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.appmoji.muvamoji.memeicon.Memeicon;
import com.appmoji.muvamoji.memeicon.MemeiconFormat;
import com.appmoji.muvamoji.memeicon.MemeiconManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener
{
    static MainActivity currentInstance = null;

    private static final Uri PROVIDER=
            Uri.parse("content://com.appmoji.muvamoji");

    private GridView gridView;

    private MemeiconManager memeiconManager;
    private MemeiconFormat memeiconFormat;

    private String currentCategory = null;
    private List<Memeicon> currentCatalog = null;

    private SaveDataManager historyManager;

    private HorizontalScrollView categoryScrollView;
    private ViewGroup categoriesView;
    private ImageButton categoryRecentButton;
    private Toolbar mToolbar;
    private int categoryLastSelectedIndex = -1;

    private boolean isPick = false;

    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        initData();
        initGridView();
        initCategory();

        // Check intent.
        final Intent intent = getIntent();
        final String action = intent.getAction();
        final String type = intent.getType();

        if(action.equals(Intent.ACTION_PICK))
        {
            if("image/png".equals(type) || "image/*".equals(type))
            {
                isPick = true;
            }
        }


        imeEnableCheck();

        initToolbar();
    }

    private void initToolbar() {
        // toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setContentInsetsAbsolute(0, 0);

        ImageButton settingButton = (ImageButton) findViewById(R.id.btn_setting);
        settingButton.setOnClickListener(this);

        ImageButton aboutButton = (ImageButton) findViewById(R.id.btn_about);
        aboutButton.setOnClickListener(this);
    }

    private void initData()
    {
        historyManager = new SaveDataManager(this, SaveDataManager.Type.CatalogHistory);
        historyManager.load();

        memeiconManager = new MemeiconManager(this);
    }

    private void initGridView()
    {
        gridView = (GridView)findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Memeicon emoji = (Memeicon)parent.getAdapter().getItem(position);
                if (emoji.isGif()) {
                    sendIntentForGif(emoji);
                }
                else {
                    sendEmoji(emoji);
                }
            }
        });
    }
    int size;
    private void initCategory()
    {
        CategoryManager categoryManager = CategoryManager.getInstance();
        categoryManager.initialize(this);

        categoryScrollView = (HorizontalScrollView)findViewById(R.id.catalog_category_scrollview);
        categoriesView = (ViewGroup)findViewById(R.id.catalog_categories);
        categoryRecentButton = (ImageButton)findViewById(R.id.catalog_category_button_recent);



        size = (int)getResources().getDimension(R.dimen.catalog_tab_icon_size);
//        int sizeHeight = (int)getResources().getDimension(R.dimen.catalog_tab_icon_height);
        final int sz = categoryManager.getCategoryCount();
        for(int i = 0; i < categoryManager.getCategoryCount(); i++)
        {
            final int index = i;
            final ImageButton newButton = new ImageButton(this);
            newButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newButton.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            newButton.setPadding(0, 0, 0, 0);

            // if category is gifs set image drawable for gif
            {
                newButton.setImageBitmap(Utils.bitmapFromAssetFilename(this, "CategoryIcons/" + (i + 1) + ".png"));
            }

            newButton.setBackgroundColor(Color.TRANSPARENT);
            newButton.setTag(i);
            newButton.setContentDescription(categoryManager.getCategoryId(i));


            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCategoryButton(v);
                    if (newButton.isClickable()) {
                        for (int j = 1; j <= sz; j++) {
                            ((ImageButton)categoriesView.getChildAt(j)).setImageBitmap(Utils.bitmapFromAssetFilename(mContext, "CategoryIcons/" + (j) + ".png"));
                            //newButton.setImageBitmap(Utils.bitmapFromAssetFilename(mContext, "CategoryIcons/" + (j + 1) + ".png"));
                        }
                        newButton.setImageBitmap(Utils.bitmapFromAssetFilename(mContext, "CategoryIcons/" + (index + 1) + "x.png"));
                    }
                }
            });

            categoriesView.addView(newButton);
        }
    }

    public void onClickCategoryButton(View v)
    {
        if (categoryLastSelectedIndex > -1) {
            categoriesView.getChildAt(categoryLastSelectedIndex).setSelected(false);
        }

        changeCategory(v.getContentDescription().toString());
        v.setSelected(true);
        updateButtonState();

        try{
        for (int j = 1; j <= size; j++) {
            ((ImageButton)categoriesView.getChildAt(j)).setImageBitmap(Utils.bitmapFromAssetFilename(mContext, "CategoryIcons/" + (j) + ".png"));
            //newButton.setImageBitmap(Utils.bitmapFromAssetFilename(mContext, "CategoryIcons/" + (j + 1) + ".png"));
        }}catch(Exception e) {

        }
    }

    private void changeCategory(String categoryName)
    {
        if (categoryName.equals(currentCategory)) return;

        currentCategory = categoryName;
        memeiconFormat = MemeiconFormat.toFormat(getString(R.string.emoji_format_default_catalog));

        if (categoryName.equals(getString(R.string.ime_category_id_history)))
        {
            List<String> emojiNames = historyManager.getEmojiNames();
            currentCatalog = createEmojiList(emojiNames);
        }
        else
        {
            currentCatalog  = memeiconManager.getEmojiList(categoryName);

            if (categoryName.equals("ga")) {
                memeiconFormat = MemeiconFormat.toFormat(getString(R.string.emoji_format_gif));
            }
        }

        if(currentCatalog != null) {
            gridView.setAdapter(new CatalogAdapter(this, currentCatalog, memeiconFormat));
        }
    }

    private void sendEmoji(Memeicon emoji)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        IconResizeDialogFragment newFragment = IconResizeDialogFragment.newInstance(emoji.getFilename());

        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(android.R.id.content, newFragment)
                .addToBackStack(null).commit();
    }

    /**
     * Send intent.
     */

    private void sendIntentForGif(Memeicon emoji) {

        String path="assets/" + emoji.getImageFilePath(MemeiconFormat.GIF).substring(5);
        Intent share=new Intent(Intent.ACTION_SEND);
        share.setType("image/gif");
        share.putExtra(Intent.EXTRA_STREAM, PROVIDER.buildUpon().path(path).build());
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Send"));
    }

    private ArrayList<Memeicon> createEmojiList(List<String> emojiNames)
    {
        ArrayList<Memeicon> emojies = new ArrayList<>(emojiNames.size());
        for (String emojiName : emojiNames)
        {
            emojies.add(memeiconManager.getEmoji(emojiName));
        }

        return emojies;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        historyManager.save();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initStartCategory();
    }

    /**
     * Initialize start category.
     */
    private void initStartCategory()
    {
        // Load start category from preference.
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = getString(R.string.preference_key_start_category);
        final String defaultCategory = getString(R.string.ime_category_id_history);
        final String historyCategory = getString(R.string.ime_category_id_history);
        final String startCategory = pref.getString(key, historyCategory);

        // If current category is not null, skip initialize.
        if(currentCategory != null)
            return;

        // Initialize scroll position.
        categoryScrollView.scrollTo(0, 0);

        // Search category.
        final int childCount = categoriesView.getChildCount();
        for(int i = 0;  i < childCount;  ++i)
        {
            final ImageButton button = (ImageButton)categoriesView.getChildAt(i);
            if(button.getContentDescription().equals(startCategory))
            {
                pref.edit().putString(key, defaultCategory).commit();
                button.performClick();
                return;
            }
        }

        // If start category is not found, use category "all".
        pref.edit().putString(key, defaultCategory).commit();
        categoryRecentButton.performClick();
    }

    /**
     * Set all tab button color to white
     *  and selected tab to light gray.
     */
    private void updateButtonState() {
        // change position

        // search category
        final int childCount = categoriesView.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            ImageButton button = (ImageButton)categoriesView.getChildAt(i);
            if (button.isSelected()) {
                button.setBackgroundColor(Color.LTGRAY);
                categoryLastSelectedIndex = i;
            } else {
                button.setBackgroundColor(Color.WHITE);
            }
        }
    }

    /**
     * Check IME enable.
     */
    private void imeEnableCheck()
    {
        // Skip if ime enable.
        final InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        for(InputMethodInfo info : imm.getEnabledInputMethodList())
        {
            if(info.getServiceName().equals(MuvamojiIME.class.getName()))
                return;
        }

        // Show dialog and go to settings.
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.editor_enable_check_message);
        dialog.setNegativeButton(R.string.editor_enable_check_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nop
            }
        });
        dialog.setPositiveButton(R.string.editor_enable_check_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_setting == v.getId()) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
        else if (R.id.btn_about == v.getId()) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
    }
}
