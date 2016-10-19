package classes;

import java.text.SimpleDateFormat;
import java.util.Date;

//Timestamp Classe v1

public class Timestamp 
{
	public String getCurrentTimeStamp() 
	{
	    SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");	//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}

	
}
