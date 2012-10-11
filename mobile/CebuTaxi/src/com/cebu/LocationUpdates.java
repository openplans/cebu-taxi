package com.cebu;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.location.Location;

public class LocationUpdates {
	private String date;
	private String time;
	private double lat;
	private double lon;
	private double speed;
	private double accuracy;
	private double bearing;
	private boolean active;
	public LocationUpdates(Location loc)
	{
		active=false;
		if(loc!=null)
		{
			Date dte = new Date(System.currentTimeMillis());
			SimpleDateFormat Currdate = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat tme = new SimpleDateFormat("HHmmss");		 
			date=Currdate.format(dte);
			time=tme.format(dte);
			lat =round(((double) (loc.getLatitude())),5,BigDecimal.ROUND_HALF_UP);// loc.getLatitude();
			lon =round(((double) (loc.getLongitude())),5,BigDecimal.ROUND_HALF_UP);// loc.getLongitude();
			accuracy = round(((double) (loc.getAccuracy())),3,BigDecimal.ROUND_HALF_UP);// accuracy returns in meters.
			bearing = round(((double) (loc.getBearing())),3,BigDecimal.ROUND_HALF_UP);
			speed=round(loc.getSpeed(),3,BigDecimal.ROUND_HALF_UP);
			active=true;
		}
	}
	public static double round(double unrounded, int precision, int roundingMode)
	{
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);
		return rounded.doubleValue();
	}
	
	@Override
	public String toString() {
		if (active==true)
		return date + "T" + time + "," + lat + "," + lon + "," + speed + ","
				+ bearing + "," + accuracy;
		else
			return "";
	}

}
