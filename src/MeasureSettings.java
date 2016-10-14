import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;

//MeasureSettings Class v1

public class MeasureSettings 
{
	public void setMeasurementProporties(ImagePlus imp, double distance) 
	{
		String distanceString = String.valueOf(distance);
    	String befehlMeassurement = "distance=% known=1 pixel=1 unit=µm";
    	IJ.run(imp, "Set Scale...", befehlMeassurement.replace("%", distanceString));
		
    	return;	
	}
	
	//in dateinamen erkennen
	public double selectMagnificationAutomatically(String str, boolean x10)
	{
		boolean x40 = str.contains("x40");
    	
    	double distanceTmp = 4.392;
    	
    	if ( x40 == true && x10 == false)
    	{
    		distanceTmp = 4.392;
    	}
    	else if ( x40 == false && x10 == true)
    	{
    		distanceTmp = 1.098;
    	}
    	else
    	{
    		//Generic Dialog zum selber Auswählen
    		distanceTmp = selectMagnificationManually();
    	}
		return distanceTmp;
	}
	
	

	public double selectMagnificationManually()
	{
		GenericDialog x1040 = new GenericDialog("Which Magnification?");
		x1040.addMessage("No Parameters found.\nWhich magnification has the picture? (x10/x40)?");
		
		String[] items = {"x10", "x40"};
		x1040.addRadioButtonGroup("Which magnification has this Scan?", items, 2, 1, "x40");
		
		x1040.showDialog();
		if (x1040.wasCanceled())
            return 4.392;
		
		String vergroesserung = x1040.getNextRadioButton();
		double vergroesserungDouble;
		if (vergroesserung == "x10")
		{
			vergroesserungDouble = 1.098;
		}
		else
		{
			vergroesserungDouble = 4.392;
		}
		
		return vergroesserungDouble;
		
	}
}
