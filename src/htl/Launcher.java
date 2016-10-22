package htl;

import ij.IJ;
import ij.ImageJ;

/**
 * Launcher Class, starts NucleiJ Analyzer Programm
 * @author Stefan Erben
 * @version 1.0
 *
 */

public class Launcher 
{
	/**
	 * starts the plugin without the typical ImageJ User Interface
	 * @param args Command Line Parameters
	 */
	public static void main(String...args)
	{
		/*
		new ImageJ(null);
		ResultsTable rt = Analyzer.getResultsTable();
		if ( rt == null)
		{
			rt = new ResultsTable();
			Analyzer.setResultsTable(rt);
		}
		rt.show("Results");
		 */
		
		IJ.run("NucleiJ Analyzer v1","");		//Starten des Plugins
		
	}
}
