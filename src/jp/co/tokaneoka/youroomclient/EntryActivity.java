package jp.co.tokaneoka.youroomclient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.RejectedExecutionException;

import jp.co.tokaneoka.youroomclient.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class EntryActivity extends Activity {
		
	String roomId;
	YouRoomChildEntryAdapter adapter;
	ProgressDialog progressDialog;
	int parentEntryCount;
	int requestCount;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart(){
		super.onStart();
		
        setContentView(R.layout.main);
		
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        YouRoomEntry youRoomEntry = (YouRoomEntry) intent.getSerializableExtra("youRoomEntry");
        String entryId = String.valueOf(youRoomEntry.getId());
        parentEntryCount = youRoomEntry.getDescendantsCount();
        
    	//TODO if String decodeResult = "";
    	ListView listView = (ListView)findViewById(R.id.listView1);
		
    	progressDialog = new ProgressDialog(this);
    	setProgressDialog(progressDialog);
    	progressDialog.show();

		int level = -1;
		youRoomEntry.setLevel(level);		
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		adapter = new YouRoomChildEntryAdapter(this, R.layout.entry_list_item, dataList);
		listView.setAdapter(adapter);
		
		GetChildEntryTask task = new GetChildEntryTask(roomId);
		try {
			task.execute(youRoomEntry);
		} catch  (RejectedExecutionException e) {
			// TODO AsyncTaskでは内部的にキューを持っていますが、このキューサイズを超えるタスクをexecuteすると、ブロックされずに例外が発生します。らしいので、一旦握りつぶしている
			e.printStackTrace();
		}	
	}
	
    // ListViewカスタマイズ用のArrayAdapter
	public class YouRoomChildEntryAdapter extends ArrayAdapter<YouRoomEntry> {
		private LayoutInflater inflater;
		private ArrayList<YouRoomEntry> items;
		
		public YouRoomChildEntryAdapter( Context context, int textViewResourceId, ArrayList<YouRoomEntry> items) {
			super(context, textViewResourceId, items);
			this.items = items;
			this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(final int position, View convertView, ViewGroup parent){
			View view = convertView;
			if (convertView == null) {
				view = inflater.inflate(R.layout.entry_list_item, null);				
			}
			YouRoomEntry roomEntry = (YouRoomEntry)this.getItem(position);
			TextView name = null;
			TextView content = null;
			TextView updateTime = null;
			TextView level = null;
			
			if ( roomEntry != null ){
				name = (TextView)view.findViewById(R.id.textView1);
				updateTime = (TextView)view.findViewById(R.id.textView2);
				content = (TextView)view.findViewById(R.id.textView3);
				level = (TextView)view.findViewById(R.id.textView5);
			}
			if ( name != null ){
				name.setText(roomEntry.getParticipationName());
			}
			if ( updateTime != null ){
				updateTime.setText(roomEntry.getUpdatedTime());
			}
			if ( content != null ){
				content.setText(roomEntry.getContent());
			}
			if ( level != null ){
				String commentLevel = "";
				for(int i=0; i < roomEntry.getLevel(); i++)
					commentLevel += "-> ";
				level.setText(commentLevel);
			}
			
			return view;
		}
	}
	
	private ArrayList<YouRoomEntry> getChildEntryList(String roomId, String entryId, int level){
        YouRoomUtil youRoomUtil = new YouRoomUtil(getApplication());        
        HashMap<String, String> oAuthTokenMap = youRoomUtil.getOauthTokenFromLocal();
    	YouRoomCommand youRoomCommand = new YouRoomCommand(oAuthTokenMap);
    			    	
    	String entry = "";
    	entry = youRoomCommand.getEntry(roomId, entryId);
		ArrayList<YouRoomEntry> dataList = new ArrayList<YouRoomEntry>();
		try {
			JSONObject json = new JSONObject(entry);
			if (json.getJSONObject("entry").has("children")){
	    	JSONArray children = json.getJSONObject("entry").getJSONArray("children");
	    	
	    	for(int i =0 ; i< children.length(); i++){
	    		YouRoomEntry roomChildEntry = new YouRoomEntry();
	    		
		    	JSONObject childObject = children.getJSONObject(i);

		    	int id = childObject.getInt("id");
		    	String participationName = childObject.getJSONObject("participation").getString("name");
		    	String jcontent = childObject.getString("content");
		    	String formattedTime = "";
		    	String unformattedTime = childObject.getString("created_at");
		    		
		    	formattedTime = YouRoomUtil.convertDatetime(unformattedTime);

		    	roomChildEntry.setId(id);
		    	roomChildEntry.setParticipationName(participationName);
		    	roomChildEntry.setUpdatedTime(formattedTime);
		    	roomChildEntry.setContent(jcontent);
		    	roomChildEntry.setLevel(level);
		    	dataList.add(roomChildEntry);
	    	}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return dataList;
	}
	
	public class GetChildEntryTask extends AsyncTask<YouRoomEntry, Integer, ArrayList<YouRoomEntry>> {
		
		private String roomId;
		private YouRoomEntry roomChildEntry;
		private Object objLock = new Object();
		
		public GetChildEntryTask(String roomId){
			this.roomId = roomId;
		}
		
		@Override
		protected ArrayList<YouRoomEntry> doInBackground(YouRoomEntry... roomChildEntries) {
			roomChildEntry = roomChildEntries[0];
			String entryId = String.valueOf(roomChildEntry.getId());
						
			ArrayList<YouRoomEntry> dataList = getChildEntryList(roomId, entryId, roomChildEntry.getLevel() + 1 );
			
			if ( dataList.size() > 0){
				for (int i=0; i< dataList.size(); i++){
					GetChildEntryTask task = new GetChildEntryTask(roomId);
					task.execute(dataList.get(i));
				}
			}
			
			return dataList;
		}
		
		@Override
		protected void onProgressUpdate(Integer... progress) {
			progressDialog.setProgress(progress[0]);
		}
		
		@Override
		protected void onPostExecute(ArrayList<YouRoomEntry> dataChildList){
			synchronized (objLock){
				if (dataChildList.size() > 0) {
					for (int i=0; i< dataChildList.size(); i++){
						adapter.insert(dataChildList.get(i), adapter.getPosition(roomChildEntry) + i + 1);
					}
				}
				requestCount++;
				publishProgress(requestCount);			
				Log.e("count", "requestCount = " + requestCount);
			}
			adapter.notifyDataSetChanged();
			//親が一回呼ばれるので+1
			if ( parentEntryCount <= requestCount +1 )
				progressDialog.dismiss();
		}			
	}
	
	public void setProgressDialog(ProgressDialog progressDialog){
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("処理を実行中しています");
		progressDialog.setIndeterminate(false);
		progressDialog.setMax(parentEntryCount);
		progressDialog.setCancelable(true);
	}
	
}
