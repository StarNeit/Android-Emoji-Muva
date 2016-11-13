package com.appmoji.muvamoji;

import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.appmoji.muvamoji.memeicon.Memeicon;
import com.appmoji.muvamoji.memeicon.MemeiconFormat;
import com.appmoji.muvamoji.memeicon.MemeiconManager;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kou on 13/08/11.
 */
public class MuvamojiIME extends InputMethodService {
    static final String TAG = TextEditorActivity.TAG + "::MuvamojiIME";
    static MuvamojiIME currentInstance = null;

    private final Handler invalidateHandler;

    private Emojidex emojidex;
    private MemeiconManager memeiconManager;

    private InputMethodManager inputMethodManager = null;
    private int showIMEPickerCode = 0;
    private int showSearchWindowCode = 0;
    private EmojidexSubKeyboardView subKeyboardView = null;
    private Keyboard.Key keyEnter = null;
    private int keyEnterIndex;
    private int imeOptions;

    private View layout;
    private HorizontalScrollView categoryScrollView;
    private ImageButton categoryRecentButton;

    private ViewFlipper keyboardViewFlipper;
    private boolean swipeFlag = false;
    private boolean sizeOnOff = false;

    private PopupWindow popup;

    private SaveDataManager historyManager;
    private KeyboardViewManager keyboardViewManager;

    private String currentCategory = null;

    /**
     * Construct MuvamojiIME object.
     */
    public MuvamojiIME()
    {
        setTheme(R.style.IMETheme);

        invalidateHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                keyboardViewManager.getCurrentView().invalidateKey(msg.arg1);
            }
        };
    }

    @Override
    public void onInitializeInterface() {
        deleteCache();

        // Get InputMethodManager object.
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        showIMEPickerCode = getResources().getInteger(R.integer.ime_keycode_show_ime_picker);
        showSearchWindowCode = getResources().getInteger(R.integer.ime_keycode_show_search_window);

        // Initialize Emojidex object.
        emojidex = Emojidex.getInstance();
        emojidex.initialize(this);

        // Create PreferenceManager.
        historyManager = new SaveDataManager(this, SaveDataManager.Type.History);
//        searchManager = new SaveDataManager(this, SaveDataManager.Type.Search);

        memeiconManager = new MemeiconManager(this);

        // Emoji download.
//        new EmojidexUpdater(this).startUpdateThread();
    }

    @Override
    public View onCreateInputView() {
        // Create IME layout.
        layout = getLayoutInflater().inflate(R.layout.ime, null);

        // Get all category button.
//        categoryAllButton = (Button)layout.findViewById(R.id.ime_category_button_all);

        createCategorySelector();
        createKeyboardView();
//        createSubKeyboardView();

        return layout;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Load save data.
        if( !restarting )
        {
            historyManager.load();
        }

        // Set current instance.
        currentInstance = this;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);

        // Get ime options.
        if( (info.inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE) != 0 )
            imeOptions = EditorInfo.IME_ACTION_NONE;
        else
            imeOptions = info.imeOptions;

        // Set enter key parameter.
        if(keyEnter == null)
            return;

        switch(imeOptions)
        {
            case EditorInfo.IME_ACTION_NONE:
                keyEnter.icon = getResources().getDrawable(R.drawable.ime_key_enter);
                keyEnter.label = null;
                break;
            default:
                keyEnter.icon = null;
                keyEnter.iconPreview = null;
                keyEnter.label = getTextForImeAction(imeOptions);
                break;
        }

        // Redraw keyboard view.
//        subKeyboardView.invalidateKey(keyEnterIndex);

        // Initialize.
        initStartCategory();
    }

    @Override
    public void onFinishInput() {
        currentInstance = null;
        historyManager.save();
        super.onFinishInput();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideWindow();
    }

    @Override
    public void hideWindow()
    {
        if( !keyboardViewManager.getCurrentView().closePopup() )
        {
            currentCategory = null;
            super.hideWindow();
        }
    }

    /**
     * Create category selector.
     */
    private void createCategorySelector()
    {
        final CategoryManager categoryManager = CategoryManager.getInstance();
        categoryManager.initialize(this);

        // Create category buttons and add to IME layout.
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);

        final ImageButton switchLangButton = (ImageButton)layout.findViewById(R.id.keyboard_language_switch);
        switchLangButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSwitchLanguage(v);
            }
        });

        final ImageButton deleteButton = (ImageButton)layout.findViewById(R.id.keyboard_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentInputConnection().sendKeyEvent(new  KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        });
        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                getCurrentInputConnection().sendKeyEvent(new  KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0));
                return false;
            }
        });

        final int categoryCount = categoryManager.getCategoryCount();
        for(int i = 0;  i < categoryCount;  ++i)
        {
            // Create button.
            final ImageButton newButton = new ImageButton(this);

            newButton.setLayoutParams(new RelativeLayout.LayoutParams(Utils.dpToPixels(this, 60), RelativeLayout.LayoutParams.WRAP_CONTENT));
            newButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            newButton.setPadding(0, 0, 0, 0);
            // if category is gifs set image drawable for gif
            if (categoryManager.getCategoryId(i).equals("ga")) {
                Glide.with(this)
                        .load(Uri.parse("file:///android_asset/gifs/" + categoryManager.getCategoryText(i) + ".gif"))
                        .into(newButton);
            } else {
                newButton.setImageBitmap(Utils.bitmapFromAssetFilename(this, "mojis/" + categoryManager.getCategoryText(i) + ".png"));
            }
            newButton.setBackgroundColor(Color.TRANSPARENT);
            newButton.setTag(i);
            newButton.setContentDescription(categoryManager.getCategoryId(i));
            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickCategoryButton(v);
                }
            });

            // Add button to IME layout.
            categoriesView.addView(newButton);
        }

        // Create categories scroll buttons.
        categoryScrollView = (HorizontalScrollView)layout.findViewById(R.id.ime_category_scrollview);
    }

    /**
     * Create main KeyboardView object and add to IME layout.
     */
    private void createKeyboardView()
    {
        // Add KeyboardViewFlipper to IME layout.
        keyboardViewFlipper = (ViewFlipper)layout.findViewById(R.id.keyboard_viewFlipper);

        // Create KeyboardViewManager.
        keyboardViewManager = new KeyboardViewManager(this, new CustomOnKeyboardActionListener(), new CustomOnTouchListener());

        for(View view : keyboardViewManager.getViews())
            keyboardViewFlipper.addView(view);

        // Other keyboard buttons
        final CheckBox sizeOnOffChkBox = (CheckBox)layout.findViewById(R.id.ime_size_on_off);
        sizeOnOffChkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sizeOnOff = isChecked;
            }
        });

        final Button shareButton = (Button)layout.findViewById(R.id.ime_key_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commitAppShare();
            }
        });
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
        final String startCategory = pref.getString(key, defaultCategory);

        // If current category is not null, skip initialize.
        if(currentCategory != null)
            return;

        // Initialize scroll position.
        categoryScrollView.scrollTo(0, 0);

        // Search category.
        final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);
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
     * move to the next keyboard view
     * @param direction left or down
     */
    public void moveToNextKeyboard(String direction)
    {
        if (direction.equals("left"))
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        }
        if (keyboardViewManager.next()) {
            keyboardViewFlipper.showNext();
        }
    }

    /**
     * move to the prev keyboard view
     * @param direction right or up
     */
    public void moveToPrevKeyboard(String direction)
    {
        if (direction.equals("right"))
        {
            keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_in));
            keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_out));
        }
        if (keyboardViewManager.prev()) {
            keyboardViewFlipper.showPrevious();
        }
    }

    /**
     * When click category button.
     * @param v     Clicked button.
     */
    public void onClickCategoryButton(View v)
    {
        final String categoryName = v.getContentDescription().toString();
        changeCategory(categoryName);
    }

    /**
     * When click switch language button
     * @param v
     */
    private void onClickSwitchLanguage(View v) {
        boolean hasDefaultIME = false;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuvamojiIME.this);
        final String idNothing = getString(R.string.preference_entryvalue_default_keyboard_nothing);
        final String defaultIME = prefs.getString(getString(R.string.preference_key_default_keyboard), idNothing);

        if( !defaultIME.equals(idNothing) ) {
            for (InputMethodInfo info : inputMethodManager.getEnabledInputMethodList()) {
                if (info.getId().equals(defaultIME))
                    hasDefaultIME = true;
            }
        }

        if (hasDefaultIME)
            switchInputMethod(defaultIME);
        else
            inputMethodManager.showInputMethodPicker();
    }

    /**
     * Change category.
     * @param category  Category.
     */
    public void changeCategory(String category)
    {
        changeCategory(category, 0);
    }

    /**
     * Change category.
     * @param category      Category.
     * @param defaultPage   Default page number.
     */
    public void changeCategory(String category, int defaultPage)
    {
        if( category == null ||
            (currentCategory != null && currentCategory.equals(category))   )
            return;

        currentCategory = category;

        if(category.equals(getString(R.string.ime_category_id_history)))
        {
            final List<String> emojiNames = historyManager.getEmojiNames();
            keyboardViewManager.initializeFromName(emojiNames, defaultPage);
        }
        else if (category.equals("ga")) {
            final List<Memeicon> emojies = memeiconManager.getEmojiList(category);
            keyboardViewManager.initializeForGif(emojies, defaultPage);
        }
        else
        {
            final List<Memeicon> emojies = memeiconManager.getEmojiList(category);
            keyboardViewManager.initialize(emojies, defaultPage);
        }
    }

    /**
     * show favorites keyboard
     * @param v view
     */
//    public void showFavorites(View v)
//    {
//        // load favorites
//        ArrayList<String> favorites = FileOperation.load(this, FileOperation.FAVORITES);
//        keyboardViewManager.initializeFromName(favorites);
//    }

    /**
     * show settings
     * @param v view
     */
//    public void showSettings(View v)
//    {
//        closePopupWindow(v);
//        View view = getLayoutInflater().inflate(R.layout.settings, null);
//        createPopupWindow(view);
//    }

    /**
     * create popup window
     * @param v view
     */
//    public void createDeleteFavoritesWindow(View v)
//    {
//        closePopupWindow(v);
//        View view = getLayoutInflater().inflate(R.layout.popup_delete_all_favorites, null);
//        createPopupWindow(view);
//    }

    /**
     * delete all favorites data
     * @param v view
     */
//    public void deleteAllFavorites(View v)
//    {
//        closePopupWindow(v);
//
//        // delete
//        boolean result = FileOperation.deleteFile(getApplicationContext(), FileOperation.FAVORITES);
//        showResultToast(result);
//        currentCategory = null;
//        categoryAllButton.performClick();
//    }

    /**
     * create popup window
     * @param view view
     */
//    private void createPopupWindow(View view)
//    {
//        int height = keyboardViewFlipper.getHeight();
//
//        // create popup window
//        popup = new PopupWindow(this);
//        popup.setContentView(view);
//        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//        popup.showAtLocation(layout, Gravity.CENTER_HORIZONTAL, 0, -height);
//    }

    /**
     * close popup window
     * @param v view
     */
//    public void closePopupWindow(View v)
//    {
//        if (popup != null)
//        {
//            popup.dismiss();
//            popup = null;
//        }
//    }

    /**
     * show toast
     * @param result success or failure
     */
//    private void showResultToast(boolean result)
//    {
//        if (result)
//            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
//        else
//            Toast.makeText(this, R.string.delete_failure, Toast.LENGTH_SHORT).show();
//    }

    /**
     * Re-draw key.
     * @param emojiName     Emoji name.
     */
    void invalidate(String emojiName)
    {
        final MuvamojiKeyboardView view = keyboardViewManager.getCurrentView();
        final Keyboard keyboard = view.getKeyboard();
        final List<Keyboard.Key> keys = keyboard.getKeys();

        for(int i = 0;  i < keys.size();  ++i)
        {
            if(keys.get(i).popupCharacters.equals(emojiName))
            {
                final Message msg = invalidateHandler.obtainMessage();
                msg.arg1 = i;
                invalidateHandler.sendMessage(msg);

                break;
            }
        }
    }

    /**
     * Delete the cache files from the default cache directory.
     */
    private void deleteCache()
    {
        File cacheDir = getExternalCacheDir();
        if (cacheDir == null) return;

        File[] list = cacheDir.listFiles();
        for (File f : list) f.delete();
    }

    /**
     * Commit emoji to current input connection..
     * @param emoji     Emoji.
     */
    void commitEmoji(Memeicon emoji)
    {
        // copy emoji to clipboard
        historyManager.addFirst(emoji.getFilename());

        if (sizeOnOff) {
            createPopupWindow(emoji);
        }
        else {
            // just copy image to clipboard
            copyEmojiToClipboard(emoji);
            Toast.makeText(getApplicationContext(), R.string.clipboard, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Commit App icon to current input connection..
     */
    private void commitAppShare() {
        getCurrentInputConnection().commitText("https://itunes.apple.com/us/app/muvamoji/id1087839782?ls=1&mt=8", 1);
    }

    void changeKeyboard(Emoji emoji) {
        // If current category is not have emoji, change category.
        final String emojiCategory = emoji.getCategory();
        if(     !currentCategory.equals(getString(R.string.all_category))
            &&  !currentCategory.equals(emojiCategory) )
        {
            final ViewGroup categoriesView = (ViewGroup)layout.findViewById(R.id.ime_categories);
            final int count = categoriesView.getChildCount();
            for(int i = 0;  i < count;  ++i)
            {
                Button button = (Button)categoriesView.getChildAt(i);
                if(emojiCategory.equals(button.getContentDescription()))
                {
                    button.performClick();
                    break;
                }
            }
        }

        // Set page.
        keyboardViewFlipper.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.right_in));
        keyboardViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.left_out));
        keyboardViewManager.setPage(emoji.getName());
        keyboardViewFlipper.showNext();
    }

    private int mPopupImageWidth, mPopupImageHeight;
    private int mDstImageWidth, mDstImageHeight;
    private final int fillDefault = 30, nSeekMin = 10;
    private Bitmap bm, resizedbitmap;
    private Uri uri = null;

    /**
     * Create PopupWindow.
     */
    protected void createPopupWindow(Memeicon icon)
    {
        closePopup();

        // Create popup window.
        View view = getLayoutInflater().inflate(R.layout.popup_favorite, null);
        popup = new PopupWindow(this);
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_blank));
        popup.setOutsideTouchable(true);
        popup.setFocusable(false);
        popup.setContentView(view);
        popup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popup.setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        popup.showAtLocation(layout, Gravity.CENTER, 0, -layout.getHeight());

        final ImageView popupIcon = (ImageView)view.findViewById(R.id.ime_popup_show_image);
        bm = icon.getDrawable(MemeiconFormat.PNG).getBitmap();
        popupIcon.setImageBitmap(bm);
        // get image view size from layout
        mPopupImageWidth = popupIcon.getLayoutParams().width;
        mPopupImageHeight = popupIcon.getLayoutParams().height;

        // initialize seekbar
        final SeekBar mSeekbar = (SeekBar) view.findViewById(R.id.ime_popup_seekBar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressValue;
                if (progress <= 90) {
                    progressValue = progress + nSeekMin;
                } else {
                    return;
                }

                mDstImageWidth = mPopupImageWidth/100*progressValue;
                mDstImageHeight = mPopupImageHeight/100*progressValue;
                resizedbitmap = Bitmap.createScaledBitmap(bm, mDstImageWidth, mDstImageHeight, true);
                popupIcon.setImageBitmap(resizedbitmap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSeekbar.setSecondaryProgress(seekBar.getProgress());
            }
        });
        mSeekbar.setProgress(fillDefault);

        // set send button
        Button sendButton = (Button)view.findViewById(R.id.ime_popup_send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (closePopup()) {
                    // send resized image
                    copyToClipboardIntent(resizedbitmap, true);

                    Toast.makeText(getApplicationContext(), R.string.clipboard, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Set close button.
        ImageButton closeButton = (ImageButton)view.findViewById(R.id.ime_popup_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePopup();
            }
        });

//        setVariants();
    }

    /**
     * Copy resized bitmap to clipboard
     * @param bitmap
     */
    private void copyToClipboardIntent(Bitmap bitmap, boolean resized) {
        final File temporaryFile = new File(getExternalCacheDir(), "tmp" + System.currentTimeMillis() + ".png");

        // Create temporary file.
        try
        {
            // Change background color to white.
            final Bitmap newBitmap;
            if (resized) {
                newBitmap = Bitmap.createBitmap(mDstImageWidth, mDstImageHeight, bitmap.getConfig());
            } else {
                newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
            }
            newBitmap.eraseColor(Color.WHITE);
            final Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Save temporary file.
            final FileOutputStream os = new FileOutputStream(temporaryFile);
            newBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            uri = null;
            return;
        }

        uri = Uri.fromFile(temporaryFile);

        ClipData theClip = ClipData.newUri(getContentResolver(),"Image", uri);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(theClip);
    }

    /**
     * copy emoji to clipboard
     * @param emoji
     */
    private void copyEmojiToClipboard(Memeicon emoji) {
        copyToClipboardIntent(emoji.getDrawable(MemeiconFormat.PNG).getBitmap(), false);
    }


    /**
     * Close popup window.
     * @return  false if popup is not opened.
     */
    public boolean closePopup()
    {
        if (popup != null)
        {
            popup.dismiss();
            popup = null;
            return true;
        }
        return false;
    }

    /**
     * Custom OnKeyboardActionListener.
     */
    private class CustomOnKeyboardActionListener implements KeyboardView.OnKeyboardActionListener
    {
        @Override
        public void onPress(int primaryCode) {
            // nop
        }

        @Override
        public void onRelease(int primaryCode) {
            // nop
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (swipeFlag)
            {
                swipeFlag = false;
                return;
            }

            final List<Integer> codes = new ArrayList<Integer>();
            for(int i = 0;  i < keyCodes.length && keyCodes[i] != -1;  ++i)
                codes.add(keyCodes[i]);

            // Input show ime picker or default keyboard.
            if (primaryCode == showIMEPickerCode)
            {
                boolean hasDefaultIME = false;
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuvamojiIME.this);
                final String idNothing = getString(R.string.preference_entryvalue_default_keyboard_nothing);
                final String defaultIME = prefs.getString(getString(R.string.preference_key_default_keyboard), idNothing);

                if( !defaultIME.equals(idNothing) ) {
                    for (InputMethodInfo info : inputMethodManager.getEnabledInputMethodList()) {
                        if (info.getId().equals(defaultIME))
                            hasDefaultIME = true;
                    }
                }

                if (hasDefaultIME)
                    switchInputMethod(defaultIME);
                else
                    inputMethodManager.showInputMethodPicker();
            }
            else if (primaryCode == showSearchWindowCode)
            {
                showSearchWindow();
            }
            else
            {
                // Input emoji.
                final Memeicon emoji = memeiconManager.getEmoji(codes);
                if(emoji != null)
                {
                    commitEmoji(emoji);
                }
                // Input enter key.
                else if(primaryCode == KeyEvent.KEYCODE_ENTER && imeOptions != EditorInfo.IME_ACTION_NONE)
                {
                    getCurrentInputConnection().performEditorAction(imeOptions);
                }
                // Input other.
                else
                {
                    sendDownUpKeyEvents(primaryCode);
                }
            }
        }

        @Override
        public void onText(CharSequence text) {
            // nop
        }

        @Override
        public void swipeLeft() {
            // nop
        }

        @Override
        public void swipeRight() {
            // nop
        }

        @Override
        public void swipeDown() {
            // nop
        }

        @Override
        public void swipeUp() {
            // nop
        }

        /**
         * Show emoji search window.
         */
        private void showSearchWindow() {
            final Intent intent = new Intent(MuvamojiIME.this, SearchActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    /**
     * Custom OnGestureListener.
     */
    private class CustomOnGestureListener implements GestureDetector.OnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            // nop
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            // nop
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // nop
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // nop
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // nop
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float disX = e1.getX() - e2.getX();
            float disY = e1.getY() - e2.getY();
            if ((Math.abs(disX) < 100) && (Math.abs(disY) < 50))
                return true;

            // closing the popup window.
            MuvamojiKeyboardView view = keyboardViewManager.getCurrentView();
            view.closePopup();

            // left or right
            if (Math.abs(disX) > Math.abs(disY))
            {
                if (disX > 0)
                    moveToNextKeyboard("left");
                else
                    moveToPrevKeyboard("right");
            }
            swipeFlag = true;
            return false;
        }
    }


    /**
     * Custom OnTouchListener.
     */
    private class CustomOnTouchListener implements View.OnTouchListener
    {
        private final GestureDetector detector;

        /**
         * Construct object.
         */
        public CustomOnTouchListener()
        {
            detector = new GestureDetector(getApplicationContext(), new CustomOnGestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return detector.onTouchEvent(event);
        }
    }
}
