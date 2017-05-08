package otmobile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class GridActivity extends Activity {
    private List<String> filenames;
    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private Bitmap setThumb (String filename) {
            Bitmap thumbnail;
            thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filename),35,35);
            return thumbnail;
        }
        public ImageAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageBitmap(setThumb(filenames.get(position)));


            return imageView;
        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GridView gridview = (GridView) findViewById(R.id.gridView);
        //gridview.setAdapter(new ImageAdapter(this));

    }




}
