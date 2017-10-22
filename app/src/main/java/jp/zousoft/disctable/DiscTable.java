package jp.zousoft.disctable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

// 注
// 横画面に固定 <activity android:screenOrientation="landscape">
// 楯画面に固定 <activity android:screenOrientation="portrait">

public class DiscTable extends AppCompatActivity implements TextFinder.Update, View.OnClickListener, AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
{
	interface Selector
	{
		public boolean	accept(Disc cDisc);
	}

	private int[]		mList;
	private TextFinder	mFind;
	private ArrayAdapter<String>	mABox;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disc_table);

		DB.Init(this);
		mFind = new TextFinder(this, this);
		updateText();

		updateSpins();
		((Spinner)findViewById(R.id.spin_media)).setOnItemSelectedListener(this);
		((Spinner)findViewById(R.id.spin_type )).setOnItemSelectedListener(this);
		((Spinner)findViewById(R.id.spin_kind )).setOnItemSelectedListener(this);
		((Spinner)findViewById(R.id.spin_box  )).setOnItemSelectedListener(this);
		mABox = new ArrayAdapter<String>(DiscTable.this, android.R.layout.simple_spinner_item);
		mABox.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		updateBoxSpin();

		((Button)findViewById(R.id.finder_btn_find )).setOnClickListener(this);
		((Button)findViewById(R.id.finder_btn_clear)).setOnClickListener(this);
		findViewById(R.id.finder_btn_clear).setVisibility(View.GONE);

		// 新規作成・ファイル読み込みボタン
		findViewById(R.id.btn_new).setOnClickListener(this);

		// データの追加
		ListView	aList = (ListView)findViewById(R.id.disc_list);
		aList.setAdapter(new ListArrayAdapter());
		aList.setOnItemClickListener(this);
		aList.setOnItemLongClickListener(this);
	}

	private void updateSpins()
	{
		updateSpin(R.id.spin_media, R.string.class_media, R.array.spin_media);
		updateSpin(R.id.spin_type,  R.string.class_type,  R.array.spin_type);
		updateSpin(R.id.spin_kind,  R.string.class_kind,  R.array.spin_kind);
	}

	private void updateSpin(int cSID, int cDID, int cAID)
	{
		// 年メニュー更新
		ArrayAdapter<String>	aAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		aAdapter.add(getResources().getString(cDID));
		String[]	aList = getResources().getStringArray(cAID);
		for(int i=0 ; i<aList.length ; ++i) aAdapter.add(aList[i]);

		Spinner	aSpin = (Spinner)findViewById(cSID);
		String	aText = (String)aSpin.getSelectedItem();
		aSpin.setAdapter(aAdapter);
		for(int i=0 ; i<aSpin.getCount() ; ++i)
		{
			if(aSpin.getItemAtPosition(i).equals(aText))
			{
				aSpin.setSelection(i);
				break;
			}
		}
		aSpin.invalidate();
	}

	public void onClick(View v)
	{
		int	aID = v.getId();
		if(R.id.btn_new == aID)
		{
			// 新規作成
			mIdx = -1;
			new NewInputDialog(this).show();
		}
		else
		{
			mFind.check(aID);
		}
	}

	private String	mMedia	= null;
	private String	mType	= null;
	private String	mKind	= null;
	private String	mBox	= null;
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
	{
		int	aID = parent.getId();
		if(R.id.spin_media == aID)
		{
			Spinner	aSpin = (Spinner)findViewById(R.id.spin_media);
			if(0 == aSpin.getSelectedItemPosition())
			{
				mMedia = null;
			}
			else
			{
				mMedia = (String)aSpin.getSelectedItem();
			}
		}
		else if(R.id.spin_type == aID)
		{
			Spinner	aSpin = (Spinner)findViewById(R.id.spin_type);
			if(0 == aSpin.getSelectedItemPosition())
			{
				mType = null;
			}
			else
			{
				mType = (String)aSpin.getSelectedItem();
			}
		}
		else if(R.id.spin_kind == aID)
		{
			Spinner	aSpin = (Spinner)findViewById(R.id.spin_kind);
			if(0 == aSpin.getSelectedItemPosition())
			{
				mKind = null;
			}
			else
			{
				mKind = (String)aSpin.getSelectedItem();
			}
		}
		else if(R.id.spin_box == aID)
		{
			Spinner	aSpin = (Spinner)findViewById(R.id.spin_box);
			if(0 == aSpin.getSelectedItemPosition())
			{
				mBox = null;
			}
			else
			{
				mBox = (String)aSpin.getSelectedItem();
			}
		}
		updateText();
	}

	public void onNothingSelected(AdapterView<?> arg0)
	{
	}

	private int	mIdx;
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		mIdx = mList[position];
		new NewInputDialog(this).show();
	}

	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		mIdx = mList[position];
		String	aMsg = "\""+DB.Get(mIdx).mName+"\""+getResources().getString(R.string.txt_del_msg);
		new AlertDialog.Builder(this)
		.setTitle(R.string.txt_del_title)
		.setMessage(aMsg)
		.setPositiveButton(R.string.btn_ok,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton)
				{
					DB.Del(mIdx);
					DB.WriteDB(DiscTable.this);
					updateBoxSpin();
					updateText();
				}
			})
		.setNegativeButton(R.string.btn_cancel, null)
		.show();

		return true;
	}

	private void updateBoxSpin()
	{
		mABox.clear();
		mABox.add(getResources().getString(R.string.class_box));
		for(int i=0 ; i<DB.CountBox() ; ++i) mABox.add(DB.GetBox(i));
		Spinner	aSpin = (Spinner)findViewById(R.id.spin_box);
		String	aText = (String)aSpin.getSelectedItem();
		aSpin.setAdapter(mABox);
		for(int i=0 ; i<aSpin.getCount() ; ++i)
		{
			if(aSpin.getItemAtPosition(i).equals(aText))
			{
				aSpin.setSelection(i);
				break;
			}
		}
		aSpin.invalidate();
	}

	@SuppressLint("DefaultLocale")
	public void updateText()
	{
		int[]	aTemp = new int [DB.Count()];
		int		aCnt  = 0;
		for(int i=0 ; i<aTemp.length ; ++i)
		{
			Disc	aDisc = DB.Get(i);
			if(null != mMedia)
			{
				if(! mMedia.equals(aDisc.mMedia)) continue;
			}
			if(null != mType)
			{
				if(! mType.equals(aDisc.mType)) continue;
			}
			if(null != mKind)
			{
				if(! mKind.equals(aDisc.mKind)) continue;
			}
			if(null != mBox)
			{
				if(! mBox.equals(aDisc.mBox)) continue;
			}
			if(! mFind.empty())
			{
				if(! mFind.find(aDisc.mName)) continue;
			}
			aTemp[aCnt++] = i;
		}
		mList = new int [aCnt];
		for(int i=0 ; i<aCnt ; ++i) mList[i] = aTemp[i];

		((TextView)findViewById(R.id.txt_count)).setText("("+mList.length+"/"+DB.Count()+")");
		((ListView)findViewById(R.id.disc_list)).invalidateViews();
	}

	// option menu
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_disc_list, menu);
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}

	private View	mJump;
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int	aID = item.getItemId();
		if(R.id.menu_read == aID)
		{
			readFile();
			return true;
		}
		else if(R.id.menu_jump == aID)
		{
			mJump = getLayoutInflater().inflate(R.layout.layout_jump_panel, null);
			new AlertDialog.Builder(DiscTable.this)
			.setTitle(R.string.dlog_jump_title)
			.setMessage(R.string.dlog_jump_msg)
			.setView(mJump)
			.setNegativeButton(R.string.btn_cancel, null)
			.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton)
				{
					int	n = -1;
					switch(((RadioGroup)mJump.findViewById(R.id.jump_radio)).getCheckedRadioButtonId())
					{
					case R.id.jump_first:	n = 1;				break;
					case R.id.jump_last:	n = mList.length;	break;
					case R.id.jump_num:
						try {
							TextView	T = (TextView)mJump.findViewById(R.id.jump_text);
							n = Integer.parseInt(T.getText().toString());
							if(     n < 1)            n = 1;
							else if(n > mList.length) n = mList.length; 
						} catch(NumberFormatException e) { n = -1; }
						break;
					}
					if(-1 != n)
					{
						((ListView)findViewById(R.id.disc_list)).setSelection(n-1);
					}
				}
			})
			.show();
			return true;
		}
		else if(R.id.menu_delete == aID)
		{
			new AlertDialog.Builder(this)
			.setTitle(R.string.txt_del_all)
			.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton)
					{
						DB.DelAll();
						DB.WriteDB(DiscTable.this);
						updateBoxSpin();
						updateText();
					}
				})
			.setNegativeButton(R.string.btn_cancel, null)
			.show();
			return true;
		}
		else if(R.id.menu_quit == aID)
		{
			new AlertDialog.Builder(this)
			.setTitle(R.string.txt_quit_title)
			.setMessage(R.string.txt_msg_quit)
			.setPositiveButton(R.string.btn_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton)
					{
						finish();
					}
				})
			.setNegativeButton(R.string.btn_cancel, null)
			.show();
		}

		return false;
	}

	private String[]	mFiles;
	private void readFile()
	{
		// 外部ファイル読み込み
//		String[]	aFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).list();
		String[]	aFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).list();
		String	 	aExt  = ".txt";
		int			aCnt  = 0;
		for(int i=0 ; i<aFile.length ; ++i)
		{
			if(aFile[i].indexOf(aExt) == aFile[i].length()-aExt.length())
			{
				aFile[aCnt++] = aFile[i];
			}
		}
		if(0 == aCnt)
		{
			new AlertDialog.Builder(this)
			.setTitle(R.string.txt_dlog_title)
			.setMessage(R.string.txt_dlog_nofile)
			.setPositiveButton("OK", null)
			.show();
		}
		else
		{
			mFiles = new String [aCnt];
			for(int i=0 ; i<aCnt ; ++i) mFiles[i] = aFile[i];
			new AlertDialog.Builder(this)
			.setTitle(R.string.txt_dlog_ext)
			.setNegativeButton(R.string.btn_close, null)
			.setItems(mFiles, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which)
				{
					if(DB.AddFile(mFiles[which])) updateBoxSpin();
					DB.WriteDB(DiscTable.this);
					updateText();
				}
				})
			.show();
		}
	}

	private class ListArrayAdapter extends BaseAdapter
	{
		public ListArrayAdapter()
		{
		}

		public int getCount()
		{
			return mList.length;
		}
		
		public String getItem(int cPos)
		{
			return DB.Get(mList[cPos]).mName;
		}
		
		public long getItemId(int cPos)
		{
			return cPos;
		}

		public View getView(int cPos, View cConvert, ViewGroup cParent)
		{
			if(null == cConvert)
			{
				cConvert = getLayoutInflater().inflate(R.layout.layout_disc_info, null);
			}
			
			Disc	aDisc = DB.Get(mList[cPos]);
			((TextView)cConvert.findViewById(R.id.disc_no   )).setText((1+cPos)+".");
			((TextView)cConvert.findViewById(R.id.disc_name )).setText(aDisc.mName);
			((TextView)cConvert.findViewById(R.id.disc_media)).setText(aDisc.mMedia);
			((TextView)cConvert.findViewById(R.id.disc_type )).setText(aDisc.mType);
			((TextView)cConvert.findViewById(R.id.disc_kind )).setText(aDisc.mKind);
			((TextView)cConvert.findViewById(R.id.disc_box  )).setText(aDisc.mBox);
			((TextView)cConvert.findViewById(R.id.disc_sub  )).setText((0==aDisc.mSub.length())?"-":aDisc.mSub);

			return cConvert;
		}
	}

	// 新規データ入力ダイアログ
	class NewInputDialog extends AlertDialog.Builder implements DialogInterface.OnClickListener
	{
		private View	mPanel;

		public NewInputDialog(Context cContext)
		{
			super(cContext);

			mPanel = getLayoutInflater().inflate(R.layout.dlog_disc_info, null);
			((EditText)mPanel.findViewById(R.id.dlog_txt_name)).setInputType(InputType.TYPE_CLASS_TEXT);
			((EditText)mPanel.findViewById(R.id.dlog_txt_box )).setInputType(InputType.TYPE_CLASS_TEXT);
			((EditText)mPanel.findViewById(R.id.dlog_txt_sub )).setInputType(InputType.TYPE_CLASS_TEXT);

			Resources	aRes = getResources();
			setTitle(aRes.getString((-1==mIdx)?R.string.dlog_title1:R.string.dlog_title2));
			setView(mPanel);
			setNegativeButton(aRes.getString(R.string.btn_cancel), null);
			setPositiveButton(aRes.getString(R.string.btn_ok),     this);
		
			if(-1 != mIdx)
			{
				Disc	aDisc = DB.Get(mIdx);
				((EditText)mPanel.findViewById(R.id.dlog_txt_name)).setText(aDisc.mName);
				((EditText)mPanel.findViewById(R.id.dlog_txt_box )).setText(aDisc.mBox);
				((EditText)mPanel.findViewById(R.id.dlog_txt_sub )).setText(aDisc.mSub);
				setSpinner(R.id.dlog_spin_media, aDisc.mMedia);
				setSpinner(R.id.dlog_spin_type,  aDisc.mType);
				setSpinner(R.id.dlog_spin_kind,  aDisc.mKind);
			}
		}

		private void setSpinner(int cID, String cText)
		{
			Spinner	SP = (Spinner)mPanel.findViewById(cID);
			for(int i=0 ; i<SP.getCount() ; ++i)
			{
				if(cText.equals(SP.getItemAtPosition(i)))
				{
					SP.setSelection(i);
					break;
				}
			}
		}

		public void onClick(DialogInterface dialog, int whichButton)
		{
			Disc	aDisc = createDisc();
			if(null == aDisc)
			{
				new AlertDialog.Builder(DiscTable.this)
				.setTitle(R.string.txt_dlog_title)
				.setMessage(R.string.txt_dlog_msg)
				.setPositiveButton(R.string.btn_ok, null)
				.show();
				return;
			}
			if(-1 == mIdx)
			{
				// 新規作成
				if(DB.Add(aDisc)) updateBoxSpin();
				DB.WriteDB(DiscTable.this);
				updateText();
			}
			else
			{
				// データ修正
				if(DB.Replace(aDisc, mIdx)) updateBoxSpin();
				DB.WriteDB(DiscTable.this);
				updateText();
			}
			((ListView)findViewById(R.id.disc_list)).invalidateViews();
		}

		private Disc createDisc()
		{
			Disc	aDisc = new Disc();
			aDisc.mName  = getText(R.id.dlog_txt_name);		if(null == aDisc.mName) return null;
			aDisc.mBox   = getText(R.id.dlog_txt_box);		if(null == aDisc.mBox)  return null;
			aDisc.mMedia = getText(R.id.dlog_spin_media);
			aDisc.mType  = getText(R.id.dlog_spin_type);
			aDisc.mKind  = getText(R.id.dlog_spin_kind);
			aDisc.mSub   = getText(R.id.dlog_txt_sub);		if(null == aDisc.mSub) aDisc.mSub = "";

			return aDisc;
		}

		private String getText(int cID)
		{
			View	v = mPanel.findViewById(cID);
			String	aText;
			if(v instanceof EditText)
			{
				aText = ((EditText)v).getText().toString();
				while((aText.length()>1) && " ".equals(aText.substring(0, 1)))
				{
					aText = aText.substring(1);
				}
				while((aText.length()>1) && " ".equals(aText.substring(aText.length()-1)))
				{
					aText = aText.substring(0, aText.length()-1);
				}
				if(0 == aText.length()) aText = null;
			}
			else if(v instanceof Spinner)
			{
				aText = (String)((Spinner)v).getSelectedItem();
			}
			else
			{
				aText = null;
			}

			return aText;
		}
	}
}
