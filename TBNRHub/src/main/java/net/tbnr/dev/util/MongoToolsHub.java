package net.tbnr.dev.util;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import net.cogzmc.core.util.Point;

public class MongoToolsHub {
    private static final String X = "x";
    private static final String Y = "y";
    private static final String Z = "z";
    private static final String PITCH = "pitch";
    private static final String YAW = "yaw";

    public static Double getNumFor(DBObject object, Object key) {
        return ((Double)object.get(key.toString()));
    }

    public static Point pointFrom(DBObject object) {
        return Point.of(
                getNumFor(object, X),
                getNumFor(object, Y),
                getNumFor(object, Z),
                getNumFor(object, PITCH).floatValue(),
                getNumFor(object, YAW).floatValue()
        );
    }

    public static DBObject pointForDB(Point point) {
        return new BasicDBObjectBuilder().add(X, point.getX()).add(Y, point.getY()).add(Z, point.getZ()).add(PITCH, point.getPitch()).add(YAW, point.getYaw()).get();
    }
}
