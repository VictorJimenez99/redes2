package redes;
import org.junit.jupiter.api.Test;
import redes.network.Common;

import static org.junit.jupiter.api.Assertions.*;

class CommonTest {

    @Test
    void split() {
        var string = "port:2020\nname:victor";
        var actual = Common.splitMessage(string);
        String[][] expected = {{"port","2020"},{"name","victor"}};
        assertArrayEquals(expected,actual);
    }
    @Test
    void find() {
        var string = "RMIPort:2020\nname:victor";
        var actual = Common.splitMessage(string);
        String[][] expected = {{"RMIPort","2020"},{"name","victor"}};
        assertArrayEquals(expected,actual);
        var value = Common.getPropertyFromMessage(expected, "RMIPort");
        assertEquals(value, "2020");

    }
}