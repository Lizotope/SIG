package tpsig;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgis.Geometry;
import org.postgis.PGgeometry;

import tpsig.database.Utils;
import tpsig.geoexplorer.gui.CoordinateConverter;
import tpsig.geoexplorer.gui.GeoMainFrame;
import tpsig.geoexplorer.gui.LineString;
import tpsig.geoexplorer.gui.MapPanel;
import tpsig.geoexplorer.gui.Point;
import tpsig.geoexplorer.gui.Polygon;

public class Question12 
{
	MapPanel mp;
	CoordinateConverter cc;
	
	public static void main(String[] args) throws Exception
	{
		Question12 q12 = new Question12();
		
		try
		{
			q12.init();
			q12.buildHighways();
			q12.buildBuildings();
			q12.exit();
		}
		catch(Exception ex)
		{
			System.out.println(String.format("Erreur : %s", ex.getMessage()));
		}
	}
	
    public void init()
    {
    	// Preparation du graphique
    	mp = new MapPanel(0, 0, 1000);
    	GeoMainFrame main = new GeoMainFrame("Question12", mp);
    	main.setBounds(0, 0, 1024, 768);
    	cc = new CoordinateConverter(1024, 768, 0, 0, 1000);
    	main.setVisible(true);
    	mp.autoAdjust();
    }
    
    public void exit()
    {
        Utils.closeConnection();    	
    }
    
    public void buildHighways() throws SQLException
    {
        PreparedStatement stmt = Utils.getConnection().prepareStatement(
        		  "select  ST_Transform(w.linestring, 2154) , w.tags->'highway' "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and	   exist(w.tags, 'highway') ");
        ResultSet res = stmt.executeQuery();
        
        System.out.println("Build highways...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            String highwayType = res.getString(2);
            
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString highway = (org.postgis.LineString)p;
            	Color c = Color.RED;
            	switch (highwayType)
            	{
            		case "road":
            			c = Color.GRAY;
            			break;
            		case "residential":
            			c = Color.BLUE;
            			break;
            		case "path":
            			c = Color.LIGHT_GRAY;
            			break;
            		case "footway":
            			c = Color.PINK;
            			break;
            		case "service":
            			c = Color.ORANGE;
            			break;
            		case "track":
            			c = Color.BLACK;
            			break;
            		case "motorway":
            			c = Color.BLACK;
            			break;
            		case "primary":
            			c = Color.BLACK;
            			break;            			
            		default:
            			c = Color.BLACK;
            	}
            	
            	LineString ls = new LineString(c);
            	for (int i = 0; i < highway.numPoints(); i++)
            		ls.addPoint( new Point(cc.xMapToScreen(highway.getPoint(i).getX()), -cc.yMapToScreen(highway.getPoint(i).getY())));
            	mp.addPrimitive(ls);
            }
        }
        mp.autoAdjust();
        res.close();
        stmt.close();
        System.out.println("Build highways... done");
    }
    
    public void buildBuildings() throws SQLException
    {
    	PreparedStatement stmt = Utils.getConnection().prepareStatement(
    			  "select  ST_Transform(w.linestring, 2154) "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.linestring) "
        		+ "and	   exist(w.tags, 'building')");
    	ResultSet res = stmt.executeQuery();
        
        System.out.println("Build buildings...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            if (p.getType() == Geometry.LINESTRING)
            {
            	org.postgis.LineString building = (org.postgis.LineString)p;
            	
            	Polygon pol = new Polygon();
            	for (int i = 0; i < building.numPoints(); i++)
            		pol.addPoint(new Point(cc.xMapToScreen(building.getPoint(i).getX()), -cc.yMapToScreen(building.getPoint(i).getY())));
            	mp.addPrimitive(pol);
            }
        }
        res.close();
        stmt.close();
        System.out.println("Build buildings... done");
    }
    
    /* Dessin avec les bounding box des batiments, plus rapide mais moins precis
     * public void buildBuildings() throws SQLException
    {
    	System.out.println("Retrieve buildings...");
    	PreparedStatement stmt = Utils.getConnection().prepareStatement(
    			  "select  ST_Transform(bbox, 2154) "
        		+ "from	   ways w "
        		+ "where   ST_Contains(ST_PolygonFromText('POLYGON((5.7 45.1, 5.8 45.1, 5.8 45.2, 5.7 45.2, 5.7 45.1))', 4326), w.bbox) "
        		+ "and	   exist(w.tags, 'building')");
    	ResultSet res = stmt.executeQuery();
        
        System.out.println("Build buildings...");
        while (res.next()) 
        {
            Geometry p  = ((PGgeometry)res.getObject(1)).getGeometry();
            
            if (p.getType() == Geometry.POLYGON)
            {
            	org.postgis.Polygon building = (org.postgis.Polygon)p;
            	
            	Polygon pol = new Polygon();
            	for (int i = 0; i < building.numPoints(); i++)
            		pol.addPoint(new Point(cc.xMapToScreen(building.getPoint(i).getX()), -cc.yMapToScreen(building.getPoint(i).getY())));
            	mp.addPrimitive(pol);
            }
        }
        res.close();
        stmt.close();
        System.out.println("Build buildings... done");
    }*/
}
