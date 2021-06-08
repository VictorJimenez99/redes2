package redes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommonTest {

    @Test
    void split() {
        var string = "port:2020\nname:victor";
        var actual = Common.splitMessage(string);
        String[][] expected = {{"port","2020"},{"name","victor"}};
        assertArrayEquals(expected,actual);
    }
}