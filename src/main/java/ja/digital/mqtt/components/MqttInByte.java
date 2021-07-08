package ja.digital.mqtt.components;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;
import ja.digital.mqtt.DigitalMqttClient;

import static de.neemann.digital.core.element.PinInfo.input;

import java.net.URISyntaxException;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * A custom Or component
 */
public class MqttInByte extends Node implements Element {

	protected String mqttTopic = null; //"/digital/mqtt/test01";
	protected String mqttBroker = null; //"tcp://test.mosquitto.org:1883";

	protected DigitalMqttClient mqttClient;
	
	static final Key<String> MYKEY_TOPIC =
            new Key<String>("mqttTopic", "/digital/mqtt/test01")
                    .setName("Topic")
                    .setDescription("The topic.");
	
	static final Key<String> MYKEY_BROKER =
            new Key<String>("mqttBroker", "tcp://test.mosquitto.org:1883")
                    .setName("Broker")
                    .setDescription("The broker.");

    static final Key<Integer> ELLIPSE_SIZE =
            new Key.KeyInteger("ellipseSize", 2)
                    .setMin(1)
                    .setMax(5)
                    .setComboBoxValues(1, 2, 3, 4, 5)
                    .setName("Ellipse Size")
                    .setDescription("Sets the size of the ellipse.");

    /**
     * The description of the new component
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(MqttInByte.class,
            input("cc", "Connect control"),
            input("rcv", "Receive data"))
            .addAttribute(MYKEY_BROKER)
            .addAttribute(MYKEY_TOPIC)
            .addAttribute(Keys.ROTATE);

    private ObservableValue oobsData;
    private ObservableValue iobsConnControl;
    private ObservableValue iobsReceive;
    private ObservableValue oobsConnStatus;
    private long valConnStatus, valConnControl, valData, valReceive;
    private long valprvConnControl, valprvReceive;

    /**
     * Creates a component.
     * The constructor is able to access the components attributes and has
     * to create the components output signals, which are instances of the {@link ObservableValue} class.
     * As soon as the constructor is called you have to expect a call to the getOutputs() method.
     *
     * @param attr the attributes which are editable in the components properties dialog
     * @throws URISyntaxException 
     * @throws MqttException 
     */
    public MqttInByte(ElementAttributes attr) {
        
    	oobsConnStatus = new ObservableValue("cs", 1).setDescription("Connection status");
        oobsData = new ObservableValue("d", 8).setDescription("Data");
        
        this.mqttTopic = attr.get(MYKEY_TOPIC);
        this.mqttBroker = attr.get(MYKEY_BROKER);
        
        System.out.println("broker=" + this.mqttBroker + ", topic=" + this.mqttTopic);
    }

    /**
     * This method is called if one of the input values has changed.
     * Here you can read the input values of your component.
     * It is not allowed to write to one of the outputs!!!
     */
    @Override
    public void readInputs() {
        
        valprvConnControl = valConnControl;
        valConnControl = iobsConnControl.getValue();
        
        valprvReceive = valReceive;
        valReceive = iobsReceive.getValue();
        
        /*
        if (mqttClient != null && valueB != 0) {
        	System.out.println("Sending " + String.valueOf(valueB));
        	mqttClient.sendMessage(String.valueOf(valueB));
        }
        */
    }

    /**
     * This method is called if you have to update your output.
     * It is not allowed to read one of the inputs!!!
     */
    @Override
    public void writeOutputs() {
    	
        try {
            if (valConnControl == 1 && valprvConnControl == 0) {
        		mqttClient = new DigitalMqttClient(mqttBroker,mqttTopic);
            }
            if (mqttClient != null && valConnControl == 0) {
            	mqttClient.close();
        		mqttClient = null;
            }
        }
        catch (Exception ex) {
        	mqttClient = null;
        	System.err.println("ex: " + ex.getMessage());
        	ex.printStackTrace();
        }

        valConnStatus = (mqttClient == null ? 0 : 1);
        oobsConnStatus.setValue(valConnStatus);

        if (valReceive == 1 && valprvReceive == 0) {
        	byte[] data = mqttClient.getData();
        	if (data != null && data.length > 0)
        		oobsData.setValue(data[0]);
        }
        
        /*
        if (valprvReceive != valReceive && valReceive == 1) {
        	mqttClient.sendMessage(Long.toHexString(valData));
        }
        */
    }

    /**
     * This method is called to register the input signals which are
     * connected to your components inputs. The order is the same as given in
     * the {@link ElementTypeDescription}.
     * You can store the instances, make some checks and so on.
     * IMPORTANT: If it's necessary that your component is called if the input
     * changes, you have to call the addObserverToValue method on that input.
     * If a combinatorial component is implemented you have to add the observer
     * to all inputs. If your component only reacts on a clock signal you only
     * need to add the observer to the clock signal.
     *
     * @param inputs the list of <code>ObservableValue</code>s to use
     * @throws NodeException NodeException
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        iobsConnControl = inputs.get(0).addObserverToValue(this).checkBits(1, this);
        iobsReceive = inputs.get(1).addObserverToValue(this).checkBits(1, this);
    }

    /**
     * This method must return the output signals of your component.
     *
     * @return the output signals
     */
    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(oobsConnStatus,oobsData);
    }
    
    @Override
    protected void finalize() {
        if (mqttClient != null) {
        	mqttClient.close();
        	mqttClient = null;
        }
    }
}
