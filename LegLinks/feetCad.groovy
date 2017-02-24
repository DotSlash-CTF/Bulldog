import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import eu.mihosoft.vrl.v3d.parametrics.*;
import javafx.scene.paint.Color;
import com.neuronrobotics.bowlerstudio.threed.BowlerStudio3dEngine;


class Feet implements ICadGenerator, IParameterChanged{
	//First we load teh default cad generator script 
	ICadGenerator defaultCadGen=(ICadGenerator) ScriptingEngine
	                    .gitScriptRun(
                                "https://github.com/DotSlash-CTF/Bulldog.git", // git location of the library
	                              "FullDog/laserCutCad.groovy" , // file to load
	                              null
                        )
	LengthParameter thickness 		= new LengthParameter("Material Thickness",3.15,[10,1])
	LengthParameter headDiameter 		= new LengthParameter("Head Dimeter",100,[200,50])
	LengthParameter snoutLen 		= new LengthParameter("Snout Length",63,[200,50])
	LengthParameter jawHeight 		= new LengthParameter("Jaw Height",32,[200,10])
	LengthParameter leyeDiam 		= new LengthParameter("Left Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter reyeDiam 		= new LengthParameter("Right Eye Diameter",35,[headDiameter.getMM()/2,29])
	LengthParameter eyeCenter 		= new LengthParameter("Eye Center Distance",headDiameter.getMM()/2,[headDiameter.getMM(),headDiameter.getMM()/2])
	StringParameter servoSizeParam 			= new StringParameter("hobbyServo Default","towerProMG91",Vitamins.listVitaminSizes("hobbyServo"))
	StringParameter boltSizeParam 			= new StringParameter("Bolt Size","M3",Vitamins.listVitaminSizes("capScrew"))

	HashMap<String, Object>  boltMeasurments = Vitamins.getConfiguration( "capScrew",boltSizeParam.getStrValue())
	HashMap<String, Object>  nutMeasurments = Vitamins.getConfiguration( "nut",boltSizeParam.getStrValue())
	//println boltMeasurments.toString() +" and "+nutMeasurments.toString()
	double boltDimeMeasurment = boltMeasurments.get("outerDiameter")
	double nutDimeMeasurment = nutMeasurments.get("width")
	double nutThickMeasurment = nutMeasurments.get("height")
	// private TransformNR offset =BowlerStudio3dEngine.getOffsetforvisualization().inverse();
	ArrayList<CSG> headParts =null
	@Override 
	public ArrayList<CSG> generateCad(DHParameterKinematics d, int linkIndex) {
		ArrayList<CSG> allCad=defaultCadGen.generateCad(d,linkIndex);
		ArrayList<DHLink> dhLinks=d.getChain().getLinks();
		DHLink dh = dhLinks.get(linkIndex)

		//The link configuration
		LinkConfiguration conf = d.getLinkConfiguration(linkIndex);
		// creating the servo
		CSG servoReference=   Vitamins.get(conf.getElectroMechanicalType(),conf.getElectroMechanicalSize())
		//servoReference = servoReference.rotz(90).movez(-servoReference.getMaxZ())
		servoReference = servoReference.rotx(90)
		
		//Creating the horn
		double servoTop = servoReference.getMaxZ()
		CSG horn = Vitamins.get(conf.getShaftType(),conf.getShaftSize())	

		def remoteLegPiece = ScriptingEngine.gitScriptRun(
          					"https://github.com/DotSlash-CTF/Bulldog.git",
          					"LegLinks/LegMethods.groovy",
          					null
          					);
          CSG piece;
          if(linkIndex < 2){
          	piece = remoteLegPiece.createBaseLink(servoReference.movey(-3), horn, (int) dh.getR() - 30, linkIndex == 0)
          }
          else{
          	piece = remoteLegPiece.createConnector(servoReference.movey(-3), horn, (int) dh.getR() - 30).movex(dh.getR())
          }//remoteLegPiece.createConnector(servoReference.movey(-3), horn, (int) dh.getR())
          
          //CSG conn = remoteLegPiece.createBaseLink(servoReference.movey(-3), horn, (int) dh.getR(), linkIndex == 0)
		defaultCadGen.add(allCad, piece.rotx(180).movez(-23.5).movex(-1.75*dh.getR()+5), dh.getListener())
          servoReference = servoReference.movez(100)
		return allCad;
	}
	@Override 
	public ArrayList<CSG> generateBody(MobileBase b ) {
		ArrayList<CSG> allCad=defaultCadGen.generateBody(b);
		//If you want you can add things here
		//allCad.add(myCSG);
		
		return allCad;
	}
	/**
	 * This is a listener for a parameter changing
	 * @param name
	 * @param p
	 */
	 
	public void parameterChanged(String name, Parameter p){
		//new RuntimeException().printStackTrace(System.out);
		println "headParts was set to null from "+name
		new Exception().printStackTrace(System.out)
		headParts=null
	}
};

System.out.println("please show")

return new Feet()
