package classes;//SummaryCreator Class v1, Stringspeicher fuer SummaryReport

public class StringAdder 
{
	String zwischenspeicherString = "";
	
	public void appendString(String uebergebenerString)
	{
		if (uebergebenerString == "reset")
		{
			zwischenspeicherString = "";
		}
		else
		{
			zwischenspeicherString = zwischenspeicherString + uebergebenerString;
		}
	}
	
	public String getString()
	{
		return zwischenspeicherString;
	}
}
