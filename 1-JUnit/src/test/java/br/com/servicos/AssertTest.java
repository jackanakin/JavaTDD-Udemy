package br.com.servicos;

import br.com.kuhn.entidades.Usuario;
import org.junit.Assert;
import org.junit.Test;

public class AssertTest {
    @Test
    public void test(){
        Assert.assertTrue(true);
        Assert.assertFalse(false);

        Assert.assertEquals(1, 1);
        Assert.assertEquals(0.5123, 0.5123, 0.0000); //vai passar
        //Assert.assertEquals(0.5123, 0.5124, 0.0000); //nao vai passar

        int i = 5;
        Integer i2 = 5;
        Assert.assertEquals(Integer.valueOf(i), i2);

        Assert.assertEquals("bola", "bola");
        Assert.assertTrue("bola".equalsIgnoreCase("Bola"));

        Usuario user1 = new Usuario("User 1");
        Usuario user2 = new Usuario("User 1");

        Assert.assertEquals(user1, user2);
        Assert.assertSame(user1, user1);

        Assert.assertNotNull(user1);
    }
}
