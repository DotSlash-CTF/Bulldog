
import eu.mihosoft.vrl.v3d.parametrics.*;
import com.neuronrobotics.bowlerstudio.vitamins.*;

LengthParameter bodyLength 		= new LengthParameter("Body Length",100,[200,0])
LengthParameter bodyWidth 		= new LengthParameter("Body Width",50,[200,0])
LengthParameter matThickness 		= new LengthParameter("Material Thickness",10,[200,0])

void getBodyOffsetLength(double bodyLength)
{
	
}

CSG mainBody = new Cube(bodyLength, bodyWidth, matThickness).toCSG()
CSG cChannel = Vitamins.get("vexCchannel","5x10")

mainBody = mainBody	.setParameter(bodyLength)
				.setParameter(bodyWidth)
				.setParameter(matThickness)
				.setRegenerate({new Cube(bodyLength, bodyWidth, matThickness).toCSG()})

return cChannel
//return [cChannel,mainBody]
