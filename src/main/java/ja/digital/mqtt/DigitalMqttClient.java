package ja.digital.mqtt;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class DigitalMqttClient implements MqttCallback, java.lang.AutoCloseable {

	private static int clientSeq = 0;
    private final int qos = 1;
    private String topic = null;
    private MqttClient client;
    
    protected int id = 0;
    protected byte[] data;

    public DigitalMqttClient(String uri, String topic) throws MqttException, URISyntaxException {
        this(new URI(uri),topic);
    }

    public DigitalMqttClient(URI uri, String topic) {
    	
    	this.id = ++ DigitalMqttClient.clientSeq;
    	this.topic = topic;

        String host = String.format("tcp://%s:%d", uri.getHost(), uri.getPort());
        
        /*
        String[] auth = this.getAuth(uri);
        String username = auth[0];
        String password = auth[1];
        if (!uri.getPath().isEmpty()) {
            this.topic = uri.getPath().substring(1);
        }
        */

        String clientId = MqttClient.generateClientId();

        MqttConnectOptions conOpt = new MqttConnectOptions();
        conOpt.setCleanSession(true);
        //conOpt.setUserName(username);
        //conOpt.setPassword(password.toCharArray());
        conOpt.setConnectionTimeout(1);
        //conOpt.setKeepAliveInterval(0);

        try {
        	
            this.client = new MqttClient(host, clientId, new MemoryPersistence());
            this.client.setCallback(this);
            this.client.connect(conOpt);

            if (this.topic != null) {
            	System.out.println("Subscribing to " + this.topic);
            	this.client.subscribe(this.topic, qos);
            }
            
        }
        catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    private String[] getAuth(URI uri) {
        String a = uri.getAuthority();
        String[] first = a.split("@");
        return first[0].split(":");
    }

    public void close() {
    	try {
    		this.client.disconnectForcibly();
    		this.client.close(true);
    		this.client = null;
    	}
    	catch (Exception ex) {
			ex.printStackTrace();
    	}
    }
    
    public void sendMessage(String topic, String payload) {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        try {
			this.client.publish(topic, message);
		} catch (Exception e) {
			e.printStackTrace();
		} // Blocking publish
    }

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
    public void connectionLost(Throwable cause) {
        System.out.println("Connection lost because: " + cause);
    }

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
    public void messageArrived(String topic, MqttMessage message) throws MqttException {
        System.out.println(String.format("id=%s => in: [%s] %s", this.id, topic, new String(message.getPayload())));
        data = message.getPayload();
    }
    
    public byte[] getData() {
    	return data;
    }

}