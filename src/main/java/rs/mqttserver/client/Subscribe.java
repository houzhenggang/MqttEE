/**
 * 
 */
package rs.mqttserver.client;

import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.*;
import java.lang.annotation.*;

/**
 * @author stefanvozd
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {

	String topic();

	int qos();

}
