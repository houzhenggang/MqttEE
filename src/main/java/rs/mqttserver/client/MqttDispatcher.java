package rs.mqttserver.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class MqttDispatcher implements MqttCallback {

	final static Logger log = Logger.getLogger(MqttDispatcher.class);
	private static List<Method> methods;
	private static List<String> topicslist;
	private static List<Integer> qoslist;
	private static Map<String,List<Method>> subsribers;
	ExecutorService cachedPool = Executors.newCachedThreadPool();
	ExecutorService singleThreadExec = Executors.newSingleThreadExecutor();
	
	public static void init(Class c){
		String scan_path;

		if(c != null) scan_path = c.getPackage().getName();
		else scan_path = "rs.mqttserver";

		log.info("Scanning: "+scan_path);

		Reflections reflections = new Reflections( 
			    new ConfigurationBuilder().setUrls( 
			    ClasspathHelper.forPackage( scan_path ) ).setScanners(
			    new MethodAnnotationsScanner() ) );
			Set<Method> methodsAnnotated = reflections.getMethodsAnnotatedWith(Subscribe.class);

			topicslist =  new ArrayList<String>();
			qoslist = new ArrayList<Integer>();
			
			methods = new ArrayList<Method>(methodsAnnotated);
			subsribers = new HashMap<String,List<Method>>();
			
			for(Method mt : methods){
				String topic =((Subscribe) mt.getDeclaredAnnotation(Subscribe.class)).topic();
				int qos =((Subscribe) mt.getDeclaredAnnotation(Subscribe.class)).qos();

				if(!topic.contains("/")) topic = Prop.getInstance().getValue(topic);

				topicslist.add(topic);
				qoslist.add(qos);
				
				//Mqtt wildcard support
				topicslist.add(topic);
				qoslist.add(qos);
				
				//Mqtt wildcard support enable
				topic = topic.replaceAll("\\+", "[a-zA-Z0-9]+");
				topic  = topic.replaceAll("/#", "[a-zA-Z0-9-/]*");
				topic  = topic.replaceAll("#", "[a-zA-Z0-9-/]*");
				
				if(subsribers.get(topic)==null){	
					subsribers.put(topic, new ArrayList<Method>());
				}
				
				subsribers.get(topic).add(mt);
				
			}
	
	}

	public static String[] getTopicsArray() {
		String[] array = new String[ topicslist.size()];
		for(int i=0;i<topicslist.size();i++)array[i]=topicslist.get(i);
		return array;
	}
	public static int[] getQosList() {
		int[] array = new int[qoslist.size()];
		for(int i=0;i<qoslist.size();i++)array[i]=qoslist.get(i);
		return array;
	}
	
	public MqttDispatcher() {
		log.info("MqttDispatcher initialized!");
	}

	public void connectionLost(Throwable e) {
		log.error("Connection lost!");
		log.error(e.getMessage(), e);
		try {
			MqttClientFactory.getConnectedMqttAsyncClient();
		} catch (MqttException e1) {
			log.error(e.getMessage(), e);
		}
		log.info("Reconnected!");
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		log.info("Delivery complete!");

	}


	public void messageArrived(String topic, MqttMessage msg)
			throws MqttPersistenceException, MqttException {
			log.info("Message arrived: [ topic="+topic+", message="+msg+" ]");
			
		Iterator it = subsribers.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if(topic.matches( (String) pair.getKey())){
				List<Method> smethods =  (List<Method>) pair.getValue();
				for(Method m : smethods){
					boolean async = !m.isAnnotationPresent(Sync.class);
					log.info("Routing '"+topic+"' to method: [ class="+m.getDeclaringClass()+", method="+m.getName()+",  async="+async+", subscription="+((Subscribe) m.getDeclaredAnnotation(Subscribe.class)).topic()+" ]");
					
					if(async){			
						cachedPool.submit(new MethodWrapper(m,topic,msg));
					} else {	
						singleThreadExec.submit(new MethodWrapper(m,topic,msg));
					}
				}
			}
			

		}
		
		
	}
	

	
	private class MethodWrapper implements  Callable<Boolean> {
		private Method m;
		String topic;
		MqttMessage msg;
		
		public MethodWrapper(Method m,String topic,MqttMessage msg) {
			this.m=m;
			this.topic=topic;
			this.msg=msg;
		}

		public Boolean call() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
			Class<?> c = m.getDeclaringClass();
			Object o = c.newInstance();
			
			//If method is set to private...
			m.setAccessible(true);
			m.invoke(o, topic, msg);
			
			return true;
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		cachedPool.shutdownNow();
		singleThreadExec.shutdownNow();
	}
}
