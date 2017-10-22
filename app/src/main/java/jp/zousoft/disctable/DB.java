package jp.zousoft.disctable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import android.content.Context;
import android.os.Environment;

public class DB
{
	private final static String	DB_FILE		= "DiscDB.txt";

	private static Vector<Disc>		mDisc;
	private static TreeSet<String>	mBox0;
	private static String[]			mBox;

	public static void Init(Context cContext)
	{
		mDisc = new Vector<Disc>();
		try {
			InputStream	aIN = cContext.openFileInput(DB_FILE);
			ReadDB(new LineNumberReader(new InputStreamReader(aIN)));
			aIN.close();
		}
		catch(FileNotFoundException e) { }
		catch(IOException e) { }

		mBox0 = new TreeSet<String>(new Comparator<String>() {
				public int compare(String S1, String S2)
				{
					String	T1 = S1;
					String	T2 = S2;
					while(T1.length() < T2.length()) T1 = " "+T1;
					while(T1.length() > T2.length()) T2 = " "+T2;
					return T1.compareTo(T2);
				}
			});
		CreateBox();
	}

	// 追加ファイルの読み込み
	public static boolean AddFile(String cFile)
	{
		try {
			File		aFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), cFile);
	//		File		aFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), cFile);
	//		FileReader	aRead = new FileReader(aFile.getPath());
	//		ReadDB(new LineNumberReader(aRead));
	//		aRead.close();
			FileInputStream	aIN = new FileInputStream(aFile.getPath());
			ReadDB(new LineNumberReader(new InputStreamReader(aIN, "SJIS")));
			aIN.close();
		}
		catch(FileNotFoundException e) { }
		catch(IOException e) { }

		int	c = CountBox();
		CreateBox();

		return c != CountBox();
	}

	public static int	Count() { return mDisc.size(); }
	public static Disc	Get(int i) { return mDisc.get(i); }
	public static boolean Add(Disc cDisc)
	{
		mDisc.add(cDisc);
		int	aCnt = mBox0.size();
		mBox0.add(cDisc.mBox);
		if(aCnt == mBox0.size()) return false;
		ChangeBox();
		return true;
	}
	public static boolean Replace(Disc cDisc, int cIndex)
	{
		mDisc.set(cIndex, cDisc);
		int	aCnt = mBox0.size();
		mBox0.add(cDisc.mBox);
		if(aCnt == mBox0.size()) return false;
		ChangeBox();
		return true;
	}
	public static void Del(int cIndex)
	{
		mDisc.remove(cIndex);
		ChangeBox();
	}
	public static void DelAll()
	{
		mDisc.clear();
		ChangeBox();
	}

	public static int		CountBox() { return mBox.length; }
	public static String	GetBox(int i) { return mBox[i]; }

	public static void CreateBox()
	{
		for(int i=0 ; i<mDisc.size() ; ++i)
		{
			mBox0.add(mDisc.get(i).mBox);
		}
		ChangeBox();
	}

	private static void ChangeBox()
	{
		mBox = new String [mBox0.size()];
		int	i = 0;
		for(Iterator<String> I=mBox0.iterator() ; I.hasNext() ; )
		{
			mBox[i++] = I.next();
		}
	}

	private static void ReadDB(LineNumberReader cRead) throws IOException
	{
		for(;;)
		{
			String	aLine = cRead.readLine();
			if(null == aLine) break;

			StringTokenizer	aToken = new StringTokenizer(aLine, "\t");
			int			aCount = 0;
			String[]	aTemp  = new String [6];
			while(aToken.hasMoreElements())
			{
				aTemp[aCount++] = aToken.nextToken();
				if(aCount >= aTemp.length) break;
			}
			if(aCount >= 5)
			{
				Disc	aDisc = new Disc();
				aDisc.mName  = aTemp[0];
				aDisc.mMedia = aTemp[1];
				aDisc.mType  = aTemp[2];
				aDisc.mKind  = aTemp[3];
				aDisc.mBox   = aTemp[4];
				if(5 == aCount) aDisc.mSub = "";
				else            aDisc.mSub = "-".equals(aTemp[5]) ? "" : aTemp[5];
				mDisc.add(aDisc);
			}
		}
	}

	public static void WriteDB(Context cContext)
	{
		try {
			OutputStream	aOut = cContext.openFileOutput(DB_FILE, Context.MODE_PRIVATE);
			for(int i=0 ; i<mDisc.size() ; ++i)
			{
				Disc	aDisc = mDisc.get(i);
				aOut.write((aDisc.mName +"\t").getBytes());
				aOut.write((aDisc.mMedia+"\t").getBytes());
				aOut.write((aDisc.mType +"\t").getBytes());
				aOut.write((aDisc.mKind +"\t").getBytes());
				aOut.write((aDisc.mBox  +"\t").getBytes());
				String	aTemp = (0==aDisc.mSub.length()) ? "-" : aDisc.mSub;
				aOut.write((aTemp+"\n").getBytes());
			}
			aOut.close();
		}
		catch(IOException e) { }
	}
}
