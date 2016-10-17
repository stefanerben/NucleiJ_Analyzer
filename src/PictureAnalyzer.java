import ij.process.ImageProcessor;

/**
 * PictureAnalyzer class for the NucleiJ Analyzer
 * @author Stefan Erben
 * @version 1.0
 *
 */

public class PictureAnalyzer 
{
	//fehlerhafte (einzlene) Pixel ignorieren und Originalpixelwert wiederherstellen
	public void ignoreSinglePixels(ImageProcessor original, ImageProcessor copy, boolean x10, int w, int h) 
    {
    	for (int x = 0; x <= w; x++) 
		{
			for (int y = 0; y <= h; y++) 
			{
				int c = original.getPixel(x, y);				
				if ( c == 0)
				{
					// naeher Umgebung nach markierten Pixel absuchen
					int i, j, filter, sum = 0;
					if (x10 == true)
					{
						//filter = 1;
						return;
					}
					else
					{
						filter = 3;
					}
					
					for (j=-filter; j<=filter; j++)
					{
						for (i=-filter; i<=filter; i++)
						{
							c = original.getPixel(x+j, y+i);
							if ( c == 0)		//-2420452
							{
								sum++;		//gefundene Pixel zaehlen
							}							
						}
					}
					if (sum < (3*filter))
					{
						//wenn zu wenige im Umkreis gefunden, Originalwert wiederherstellen
						int p = copy.getPixel(x, y);
						original.putPixel(x, y, p);
					}
				}
        	}
     	}
	    return;
	}
	
	
	//UP zum Detektieren von bestimmten blauen Pixeln
	public void detectCellPixels(ImageProcessor original, int w, int h)
	{
	   	//Zellkerne erkennen
	    for (int x = 0; x <= w; x++) 
		{
			for (int y = 0; y <= h; y++) 
			{
				int c = original.getPixel(x, y);
				
				//Blauwert aus RGB herausfiltern
				int r = (c & 0xff0000) >> 16;
				//int g = (c & 0x00ff00) >> 8;
				int b = (c & 0x0000ff);
				
				//moegliche Zellkerne markieren
				if (b > 70 && b < 160 && r < 120)		//if (b > 75 && b < 160)
			    {
			    	original.putPixel(x, y, 0);
			    }
	    	 }
	   	}
	return;
	}
		
	
	//Maske ueber Originalbild legen und Endbild erzeugen
	public void addMasktoOriginal(ImageProcessor maske, ImageProcessor sicherung, int w, int h)
	{
		for (int x = 0; x <= w; x++) 
		{
			for (int y = 0; y <= h; y++) 
			{
				int p = maske.getPixel(x, y);
				if (p == -16777216)
				{
					//Zellkerne Rot markieren
					int k = -2420452;		//-3584 = gelb
					maske.putPixel(x, y, k);					
				}				
				else
				{
					//Originalpixel wiederherstellen
					int k = sicherung.getPixel(x, y);
					maske.putPixel(x, y, k);
				}				
			}
		}
	    return;
	}
		
		
	
}
