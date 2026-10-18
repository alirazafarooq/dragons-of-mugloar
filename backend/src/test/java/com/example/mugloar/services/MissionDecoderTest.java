
package com.example.mugloar.service;

import com.example.mugloar.model.Mission;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MissionDecoderTest {

    @Test
    void decodesBase64MissionFields() {
        Mission mission = new Mission(
                "Znd0NFZrSlQ=",
                "SW5maWx0cmF0ZSBUaGUgU2NhcmxldCBDcm9jb2RpbGUgVHJpYmUgYW5kIHJlY292ZXIgdGhlaXIgc2VjcmV0cy4=",
                148,
                2,
                "1",
                "R2FtYmxl"
        );

        Mission decoded = MissionDecoder.decode(mission);

        assertEquals(
                "Infiltrate The Scarlet Crocodile Tribe and recover their secrets.",
                decoded.message()
        );
        assertEquals("Gamble", decoded.probability());
        assertEquals("fwt4VkJT", decoded.adId());
        assertEquals(148, decoded.reward());
        assertEquals(2, decoded.expiresIn());
        assertEquals("1", decoded.encrypted());
    }

    @Test
    void decodesRot13MissionFields() {
        Mission mission = new Mission(
                "Pb7O0VJA",
                "Xvyy Chgrev Wóafqóggve jvgu cbg naq znxr Évzuía Jnxrsvryq sebz fjnzc va Oyrnxuvyy gb gnxr gur oynzr",
                126,
                2,
                "2",
                "Vzcbffvoyr"
        );

        Mission decoded = MissionDecoder.decode(mission);

        assertEquals(
                "Kill Puteri Jónsdóttir with pot and make Éimhín Wakefield from swamp in Bleakhill to take the blame",
                decoded.message()
        );
        assertEquals("Impossible", decoded.probability());
        assertEquals(
                new String(
                        java.util.Base64.getDecoder().decode("Pb7O0VJA"),
                        java.nio.charset.StandardCharsets.UTF_8
                ),
                decoded.adId()
        );
        assertEquals(126, decoded.reward());
        assertEquals(2, decoded.expiresIn());
        assertEquals("2", decoded.encrypted());
    }

    @Test
    void leavesReadableMissionsUnchanged() {
        Mission mission = new Mission(
                "uOWZFzs6",
                "Steal super awesome diamond cat from Ira Travers",
                202,
                4,
                null,
                "Sure thing"
        );

        assertSame(mission, MissionDecoder.decode(mission));
    }

    @Test
    void preservesInvalidBase64Fields() {
        Mission mission = new Mission(
                "test123",
                "%%%invalid%%%",
                100,
                3,
                "1",
                "Not encoded text!"
        );

        Mission decoded = MissionDecoder.decode(mission);

        assertEquals("%%%invalid%%%", decoded.message());
        assertEquals("Not encoded text!", decoded.probability());
    }

    @Test
    void preservesUnknownEncoding() {
        Mission mission = new Mission(
                "test123",
                "Original message",
                100,
                3,
                "3",
                "Original probability"
        );

        Mission decoded = MissionDecoder.decode(mission);

        assertEquals("Original message", decoded.message());
        assertEquals("Original probability", decoded.probability());
    }

    @Test
    void preservesNullFields() {
        Mission mission = new Mission(
                "test123",
                null,
                100,
                3,
                "1",
                null
        );

        Mission decoded = MissionDecoder.decode(mission);

        assertNull(decoded.message());
        assertNull(decoded.probability());
    }
}
