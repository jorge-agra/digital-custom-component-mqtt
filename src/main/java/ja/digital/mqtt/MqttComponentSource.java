package ja.digital.mqtt;

import de.neemann.digital.draw.library.ComponentManager;
import de.neemann.digital.draw.library.ComponentSource;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.InvalidNodeException;
import de.neemann.digital.gui.Main;
import ja.digital.mqtt.components.MqttOutByte;

/**
 * Adds some components to Digital
 */
public class MqttComponentSource implements ComponentSource {

    /**
     * Attach your components to the simulator by calling the add methods
     *
     * @param manager the ComponentManager
     * @throws InvalidNodeException InvalidNodeException
     */
    public void registerComponents(ComponentManager manager) throws InvalidNodeException {

        // add a component and use the default shape
        manager.addComponent("Mqtt", MqttOutByte.DESCRIPTION);
    }

    /**
     * Start Digital with this ComponentSource attached to make debugging easier.
     * IMPORTANT: Remove the jar from Digitals settings!!!
     *
     * @param args args
     */
    public static void main(String[] args) {
        new Main.MainBuilder()
                .setLibrary(new ElementLibrary().registerComponentSource(new MqttComponentSource()))
                .openLater();
    }
}
