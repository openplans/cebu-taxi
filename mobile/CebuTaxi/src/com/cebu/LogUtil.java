package com.cebu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogUtil {
	public static void appendLog(String text)
    {
	       File logFile = new File("sdcard/CTTest"+Utils.date()+".file");
	       if (!logFile.exists())
	       {
	          try
	          {
	             logFile.createNewFile();
	          } 
	          catch (Exception e)
	          {
	        	  System.out.println(text);
	          }
	       }
	       try
	       {
	          //BufferedWriter for performance, true to set append to file flag
	          BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	          buf.append(Utils.time()+" "+text);
	          buf.newLine();
	          buf.close();
	       }
	       catch (Exception e)
	       {	          
	          //e.printStackTrace();
	          System.out.println(text);
	       }

		
    }
	
    public static String changeTime(String time) {
		String[]  utc_time = time.split("\\.");
		return utc_time[0];
	}

	public static String changeDate(String str_date) throws ParseException {

			return new SimpleDateFormat("yyyyMMdd").format((Date)new SimpleDateFormat("ddMMyy").parse(str_date));
	}

}
