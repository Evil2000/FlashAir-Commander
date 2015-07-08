package de.evil2000.flashaircommander;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<HashMap<String, String>> {

	public FileListAdapter(Context context, ArrayList<HashMap<String, String>> objects) {
		super(context, 0, objects);
	}
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
       // Get the data item for this position
		HashMap<String, String> file = getItem(position);    
       // Check if an existing view is being reused, otherwise inflate the view
       if (convertView == null) {
          convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_filelist, parent, false);
       }
       // Lookup view for data population
       TextView lblLine1 = (TextView) convertView.findViewById(R.id.lblLine1);
       TextView lblLine2 = (TextView) convertView.findViewById(R.id.lblLine2);
       // Populate the data into the template view using the data object
       lblLine1.setText(file.get("filename"));
       lblLine2.setText(file.get("attributes"));
       // Return the completed view to render on screen
       return convertView;
   }
}
