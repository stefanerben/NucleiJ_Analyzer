package classes;

/**
 * Created by Stefan on 19.10.2016.
 */
public class PictureCharacteristics
{
    public int gewebepixel;
    public double magnification;

    //setValue
    public void setGewebepixel(int uebergebeneGewebepixel)
    {
        gewebepixel = uebergebeneGewebepixel;
    }

    public void setMagnification(double uebergebeneMagnification)
    {
        magnification = uebergebeneMagnification;
    }


    //getValue
    public double getTumorArea()
    {
        double tumorArea = gewebepixel / (magnification*magnification);
        return tumorArea;

    }
}
