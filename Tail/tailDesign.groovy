CSG bone1 = new Cylinder(5, 5, 10, (int) 30).toCSG().setColor(javafx.scene.paint.Color.WHITE)
CSG connector1 = new Cylinder(3, 3, 10, (int) 30).toCSG().movez(7.5).setColor(javafx.scene.paint.Color.BLACK)
CSG bone2 = bone1.movez(15).setColor(javafx.scene.paint.Color.WHITE)
CSG connector2 = connector1.movez(15).setColor(javafx.scene.paint.Color.BLACK)
CSG bone3 = bone2.movez(15).setColor(javafx.scene.paint.Color.WHITE)

bone1 = bone1.difference(connector1)
bone2 = bone2.difference(connector1).difference(connector2)
bone3 = bone3.difference(connector2)

return[bone1, bone2, bone3, connector1, connector2]