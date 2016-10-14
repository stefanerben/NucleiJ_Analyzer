package htl;

import ij.IJ;

//Launcher v1, startet automatisch das Plugin

public class Launcher 
{
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
		
		IJ.run("NucleiJ Analyzer v1","");
		
	}
}
