package com.appmoji.muvamoji;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.appmoji.muvamoji.ScalingUtilities.ScalingLogic;
import com.appmoji.muvamoji.memeicon.Memeicon;
import com.appmoji.muvamoji.memeicon.MemeiconFormat;
import com.appmoji.muvamoji.memeicon.MemeiconManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IconResizeDialogFragment extends DialogFragment implements View.OnTouchListener {
    private Context context;
    private MemeiconFormat format;
    private Dialog mDialog;
    private Bitmap bm, resizedbitmap;
    private Memeicon mMemeicon;
    private SeekBar mSeekbar;
    private ImageView mImageView;
    private static final String MEMEICON_KEY = "memeicon";
    private int mViewWidth, mViewHeight, mDstImageW, mDstImageH;
    private final int fillDefault = 100, nSeekMin = 50;
    private MemeiconManager manager;

    private GridView skintone_grid;

    private Uri uri = null;

    public static IconResizeDialogFragment newInstance(String iconName) {
        IconResizeDialogFragment fragment = new IconResizeDialogFragment();
        Bundle args = new Bundle();
        args.putString(MEMEICON_KEY, iconName);
        fragment.setArguments(args);
        return fragment;
    }


    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    /**
     * Invoked when pressing button for showing result of the "Fit" decoding
     * method
     */
    protected Bitmap fitButtonPressed(Bitmap unscaledBitmap,int mDstWidth,int mDstHeight) {
        final long startTime = SystemClock.uptimeMillis();

        // Part 2: Scale image
        Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, mDstWidth,
                mDstHeight, ScalingLogic.CROP);
        return scaledBitmap;
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);

        Paint paint= new Paint(Paint.FILTER_BITMAP_FLAG |
                Paint.DITHER_FLAG |
                Paint.ANTI_ALIAS_FLAG);

        canvas.drawBitmap(bitmap, 0, 0, paint);

        return scaledBitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.icon_resize, container, false);
        context = container.getContext();
        manager = new MemeiconManager(context);

        format = MemeiconFormat.toFormat(context.getString(R.string.emoji_format_default_catalog));

        ImageButton closeButton = (ImageButton) view.findViewById(R.id.close);
        closeButton.setOnTouchListener(this);
        view.setOnTouchListener(this);

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // finish dialog
                dismiss();
                System.gc();
                sendIntent(resizedbitmap);
            }
        });

        // set resource to image view
        Bundle bundle = getArguments();

        if (bundle != null) {
            String name = bundle.getString(MEMEICON_KEY);
            mMemeicon = manager.getEmoji(name);
        }

        if (mMemeicon != null) {
            mImageView = (ImageView) view.findViewById(R.id.showImageView);
            bm = mMemeicon.getDrawable(format).getBitmap();
            mImageView.setImageBitmap(bm);
            // get image view size from layout
            mViewWidth = mImageView.getLayoutParams().width;
            mViewHeight = mImageView.getLayoutParams().height;
            // fit to layout
            if (mViewWidth > 0 && mViewHeight > 0) {
                //resizedbitmap = Bitmap.createScaledBitmap(bm, mViewWidth, mViewHeight, true);
                //resizedbitmap = fitButtonPressed(bm, mViewWidth, mViewHeight);

                resizedbitmap = scaleBitmap(bm, mViewWidth, mViewHeight);
                mImageView.setImageBitmap(resizedbitmap);
            }
        }

        // initialize seekbar
        mSeekbar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int progressValue;
                progressValue = progress + nSeekMin;

                mDstImageW = ((int)(5.12 * progressValue));
                mDstImageH = ((int)(5.12 * progressValue));
                //resizedbitmap = Bitmap.createScaledBitmap(bm, mDstImageW, mDstImageH, true);
                //resizedbitmap = fitButtonPressed(bm, mDstImageW, mDstImageH);

                resizedbitmap = scaleBitmap(bm, mDstImageW, mDstImageH);
                mImageView.setImageBitmap(resizedbitmap);
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



        memeiconManager = new MemeiconManager(getActivity());
        initGridView(view);
        init_skin_tone();


        return view;
    }

    private String currentCategory = null;
    private MemeiconManager memeiconManager;
    private MemeiconFormat memeiconFormat;
    private List<Memeicon> currentCatalog = null;

    private void init_skin_tone()
    {
       /* memeiconFormat = MemeiconFormat.toFormat(getString(R.string.emoji_format_default_catalog));

        List<String> emojiNames = new LinkedList<String>();
        emojiNames.add("d27-1.png");
        emojiNames.add("d27-2.png");
        emojiNames.add("d27-3.png");
        currentCatalog  = memeiconManager.getEmojiList(emojiNames);

        if(currentCatalog != null) {
            skintone_grid.setAdapter(new CatalogAdapter(getActivity(), currentCatalog, memeiconFormat));
        }*/
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

    private void initGridView(View view)
    {
        skintone_grid = (GridView)view.findViewById(R.id.gridView);
        skintone_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Memeicon emoji = (Memeicon)parent.getAdapter().getItem(position);
                Log.e("ss","" + position);
            }
        });
    }
    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mDialog = super.onCreateDialog(savedInstanceState);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCanceledOnTouchOutside(true);

        return mDialog;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (R.id.close == v.getId()) {
            dismiss();
            System.gc();
            return false;
        } else {
            return true;
        }
    }

    private void sendIntent(Bitmap bitmap) {
        final File temporaryFile = new File(context.getExternalCacheDir(), "tmp" + System.currentTimeMillis() + ".png");

        // Create temporary file.
        try
        {
            // Change background color to white.
            final Bitmap newBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
            //newBitmap.eraseColor(Color.TRANSPARENT);
            final Canvas canvas = new Canvas(newBitmap);
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            Paint paint2 = new Paint();
            paint2.setColor(0xffffffff);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint2);


            Paint paint= new Paint(Paint.FILTER_BITMAP_FLAG |
                    Paint.DITHER_FLAG |
                    Paint.ANTI_ALIAS_FLAG);
            canvas.drawBitmap(bitmap, (512 - bitmap.getWidth()) / 2, (512 - bitmap.getHeight()) / 2, paint);

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

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, null));
    }
}


