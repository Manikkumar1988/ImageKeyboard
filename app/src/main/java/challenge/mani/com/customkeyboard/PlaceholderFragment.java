package challenge.mani.com.customkeyboard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

/**
   * A placeholder fragment containing a simple view.
   */
  public class PlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
      PlaceholderFragment fragment = new PlaceholderFragment();
      Bundle args = new Bundle();
      args.putInt(ARG_SECTION_NUMBER, sectionNumber);
      fragment.setArguments(args);
      return fragment;
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_main, container, false);

      int[] imageId = {};
      Bundle bundle = getArguments();

      if(bundle.getInt(ARG_SECTION_NUMBER) == 1) {
        imageId = new int[] {
            R.drawable.diwali_one, R.drawable.diwali_two,
        };
      } else {
        imageId = new int[] {
            R.drawable.gm_one, R.drawable.good_morn_two
        };
      }

      CustomGrid adapter = new CustomGrid(getContext(), imageId);
      GridView grid=(GridView)rootView.findViewById(R.id.simpleGridView);
      grid.setAdapter(adapter);
      grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
            int position, long id) {
          Toast.makeText(getContext(), "You Clicked at " +position, Toast.LENGTH_SHORT).show();

        }
      });

      return rootView;
    }
  }