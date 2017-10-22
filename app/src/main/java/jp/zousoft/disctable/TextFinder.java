package jp.zousoft.disctable;

import java.util.StringTokenizer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class TextFinder implements DialogInterface.OnClickListener
{
	private String		mText;
	private Button		mClear;
	private TextView	mWord;
	private Activity	mActv;
	private Update		mUPD;

	public interface Update
	{
		public void updateText();
	}

	public TextFinder(Activity cActive, Update cUpdate)
	{
		mText  = null;
		mClear = (Button)cActive.findViewById(R.id.finder_btn_clear);
		mWord  = (TextView)cActive.findViewById(R.id.finder_txt_find);
		mActv  = cActive;
		mUPD   = cUpdate;
	}

	public boolean empty()
	{
		return null == mText;
	}

	public boolean find(String cText)
	{
		if(null == cText) return false;
		if(0 == cText.length()) return false;
		if(null == mText) return true;

		return -1 != conv(cText).indexOf(mText);
	}

	public boolean findAnd(String cText)
	{
		if(null == cText) return false;
		if(0 == cText.length()) return false;
		if(null == mText) return true;

		String	aText = conv(cText);
		for(StringTokenizer aToken=new StringTokenizer(mText, " ") ; aToken.hasMoreTokens() ; )
		{
			String	aWord = aToken.nextToken();
			if(-1 == aText.indexOf(aWord)) return false;
		}
		return true;
	}

	public boolean findOr(String cText)
	{
		if(null == cText) return false;
		if(0 == cText.length()) return false;
		if(null == mText) return true;

		String	aText = conv(cText);
		for(StringTokenizer aToken=new StringTokenizer(mText, " ") ; aToken.hasMoreTokens() ; )
		{
			String	aWord = aToken.nextToken();
			if(-1 != aText.indexOf(aWord)) return true;
		}
		return false;
	}

	// clickイベントチェック
	private EditText	mInput;
	public boolean check(int cID)
	{
		// CLEARボタン
		if(R.id.finder_btn_clear == cID)
		{
			mText = null;
			update();
			return true;
		}

		// FINDボタン
		if(R.id.finder_btn_find == cID)
		{
			new AlertDialog.Builder(mActv)
			.setTitle(R.string.dlog_find_title)
			.setMessage(R.string.dlog_find_msg)
			.setView(mInput = new EditText(mActv))
			.setNegativeButton(R.string.btn_cancel, null)
			.setPositiveButton(R.string.btn_ok,     this)
			.setNeutralButton (R.string.btn_clear,  this)
			.show();
			return true;
		}

		return false;
	}

	public void onClick(DialogInterface dialog, int whichButton)
	{
		if(DialogInterface.BUTTON_POSITIVE == whichButton)
		{
			set(mInput.getText().toString());
		}
		else if(DialogInterface.BUTTON_NEUTRAL == whichButton)
		{
			mText = null;
		}
		update();
	}

	private void update()
	{
		mWord .setText((null==mText) ? "" : mText);
		mClear.setVisibility(empty()?View.GONE:View.VISIBLE);
		mUPD.updateText();
	}

	private void set(String cText)
	{
		if(null == cText)
		{
			mText = null;
			return;
		}
		if(0 == cText.length())
		{
			mText = null;
			return;
		}

		String	aTemp = cText;
		while(0 != aTemp.length())
		{
			String	aTop = aTemp.substring(0, 1);
			if(" ".equals(aTop) || "�@".equals(aTop)) aTemp = aTemp.substring(1);
			else break;
		}
		while(0 != aTemp.length())
		{
			int		aLen  = aTemp.length();
			String	aLast = aTemp.substring(aLen-1);
			if(" ".equals(aLast) || "�@".equals(aLast)) aTemp = aTemp.substring(0, aLen-1);
			else break;
		}
		mText = conv(aTemp);
	}

	// 英数字を半角に、小文字を大文字に
	private String conv(String cText)
	{
		if(null == cText) return null;
		if(0 == cText.length()) return null;

		String	aText = "";
		for(int i=0 ; i<cText.length() ; ++i)
		{
			int	c = (int)cText.charAt(i);
			if(c == (int)'　') c = (int)' ';
			else if((c>=(int)'０') && (c<=(int)'９')) c = (int)'0' + (c-(int)'０');
			else if((c>=(int)'a') && (c<=(int)'z')) c = (int)'A' + (c-(int)'a');
			else if((c>=(int)'Ａ') && (c<=(int)'Ｚ')) c = (int)'A' + (c-(int)'Ａ');
			else if((c>=(int)'ａ') && (c<=(int)'ｚ')) c = (int)'A' + (c-(int)'ａ');
			aText = aText + String.valueOf((char)c);
		}

		return aText;
	}
}
