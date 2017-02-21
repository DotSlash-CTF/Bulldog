CSG bone1 = new Cylinder(5, 5, 10, (int) 30).toCSG()
CSG connector1 = new Cylinder(3, 3, 10, (int) 30).toCSG().movez(7.5).setColor(javafx.scene.paint.Color.BLACK)
CSG bone2 = bone1.movez(15)
CSG connector2 = connector1.movez(15).setColor(javafx.scene.paint.Color.BLACK)
CSG bone3 = bone2.movez(15)

bone1 = bone1.difference(connector1).setColor(javafx.scene.paint.Color.WHITE)
bone2 = bone2.difference(connector1).difference(connector2).setColor(javafx.scene.paint.Color.WHITE)
bone3 = bone3.difference(connector2).setColor(javafx.scene.paint.Color.WHITE)

CSG hornAttachment = new Cylinder(10, 10, 20, (int) 30).toCSG().movez(-20)

CSG tail = bone1.union(bone2.union(bone3.union(connector1.union(connector2.union(hornAttachment
)))))

return tail.toZMin()