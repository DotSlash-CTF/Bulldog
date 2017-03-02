import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.creature.ICadGenerator;
import com.neuronrobotics.bowlerstudio.creature.CreatureLab;
import org.apache.commons.io.IOUtils;
import com.neuronrobotics.bowlerstudio.vitamins.*;
import javafx.scene.paint.Color;
import eu.mihosoft.vrl.v3d.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;

def remoteLegPiece = ScriptingEngine.gitScriptRun(
            "https://github.com/DotSlash-CTF/Bulldog.git",
            "LegLinks/LegMethods.groovy",
            null
            );

CSG servo = com.neuronrobotics.bowlerstudio.vitamins.Vitamins
.get( "hobbyServo","hv6214mg")//old is towerProMG91
            
CSG horn = com.neuronrobotics.bowlerstudio.vitamins.Vitamins
.get( "hobbyServoHorn","hv6214mg_1")

//shortest is 50, largest is 180
int length = 80
int rotLength = 120
int baseTiltL = 105
boolean normFalseRotTrue = false


//ArrayList<CSG> shoulder = remoteLegPiece.createShoulder(servo, length)

ArrayList<CSG> imobile = remoteLegPiece.createThigh(servo, horn, length)

ArrayList<CSG> basePan = remoteLegPiece.rotatedLegLink(servo, horn, rotLength)

ArrayList<CSG> baseTilt = remoteLegPiece.createThigh(servo, horn, baseTiltL)




CSG connector = remoteLegPiece.createConnector(servo, horn, length)
CSG connector2 = remoteLegPiece.createConnector(servo, horn, length)
conector2 = connector2.movex(20)
connector = connector.makeKeepaway(-2)
					
for(int i = 0; i < basePan.size(); i++)
{
	basePan.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
for(int i = 0; i < imobile.size(); i++)
{
	imobile.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
for(int i = 0; i < baseTilt.size(); i++)
{
	baseTilt.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
connector.setManufactuing({CSG arg0 ->
						return arg0.toZMin();
			})
connector2.setManufactuing({CSG arg0 ->
						return arg0.toZMin();
			})


ArrayList<CSG> totalParts = imobile;
//totalParts.add(connector)

for(int i = 0; i < basePan.size(); i++)
{
	totalParts.add(basePan.get(i).movex(length))
}
for(int i = 0; i < baseTilt.size(); i++)
{
	totalParts.add(baseTilt.get(i).movex((rotLength+length)))
}

//again for back leg:
length = 80
rotLength = 105
baseTiltL = 130

imobile = remoteLegPiece.createThigh(servo, horn, length)

basePan = remoteLegPiece.rotatedLegLink(servo, horn, rotLength)

baseTilt = remoteLegPiece.createThigh(servo, horn, baseTiltL)

for(int i = 0; i < basePan.size(); i++)
{
	basePan.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
for(int i = 0; i < imobile.size(); i++)
{
	imobile.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
for(int i = 0; i < baseTilt.size(); i++)
{
	baseTilt.get(i).setManufactuing({CSG arg0 ->
								return arg0.toZMin();
			})
}
int moveY = -100
for(int i = 0; i < imobile.size(); i++)
{
	totalParts.add(imobile.get(i).movey(moveY))
}
for(int i = 0; i < basePan.size(); i++)
{
	totalParts.add(basePan.get(i).movex(length).movey(moveY))
}
for(int i = 0; i < baseTilt.size(); i++)
{
	totalParts.add(baseTilt.get(i).movex((length+rotLength)).movey(moveY))
}
return totalParts