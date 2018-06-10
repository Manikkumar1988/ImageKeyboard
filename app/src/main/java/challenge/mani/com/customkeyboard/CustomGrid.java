package challenge.mani.com.customkeyboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class CustomGrid extends BaseAdapter {
	  private Context mContext;
	  private final int[] Imageid;

	    public CustomGrid(Context c,int[] Imageid ) {
	        mContext = c;
	        this.Imageid = Imageid;
	    }

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Imageid.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			View grid;
			LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	        if (convertView == null) {

				grid = inflater.inflate(R.layout.grid_item, null);
	        	ImageView imageView = grid.findViewById(R.id.grid_image);
	        	imageView.setImageResource(Imageid[position]);
	        } else {
	        	grid = (View) convertView;
	        }

			return grid;
		}
}