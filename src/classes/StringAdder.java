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
		else if (uebergebenerString == "csvHeader")
		{
			zwischenspeicherString = "Dateiname;gesamte Gewebeflaeche [um2];Total area of all nuclei [um2];Zellkernflaeche in %;Zellkerne / mm2;Arithmetic Perimeter [um];Smallest cell nucleus [um2];largest cell nucleus [um2];Arithmetic mean area [um2];Median area [um2];oval cell nucleus;\n";
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
