package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;

public class LayerProxyTest {

	@Test
	public void testGetLayerFromInstantiation() {
		Atom layer = new Atom("a");
		LayerProxy<Atom> proxy = new LayerProxy<Atom>(layer);
		
		assertSame(layer, proxy.getLayer());
	}
	
	@Test
	public void testGetLayerFromSet() {
		Atom layer1 = new Atom("a");
		LayerProxy<Atom> proxy = new LayerProxy<Atom>(layer1);
		
		Atom layer2 = new Atom("b");
		proxy.setLayer(layer2);
		assertNotSame(layer1, proxy.getLayer());
		assertSame(layer2, proxy.getLayer());
	}
	
	@Test
	public void testProxyContentEqualsLayerContent() {
		Formula layer = new Formula("[a-z]");
		LayerProxy<Formula> proxy = new LayerProxy<Formula>(layer);
		
		layer.setContent("a");
		assertEquals("a", proxy.getContent());
		layer.setContent("b");
		assertEquals("b", proxy.getContent());
		proxy.setLayer(new Formula("[0-9]", "1"));
		assertEquals("1", proxy.getContent());
	}
	
	@Test
	public void testUpdateOnLayerNotifiesListeners() {
		Formula layer = new Formula("[a-z]");
		LayerProxy<Formula> proxy = new LayerProxy<Formula>(layer);
		
		final String[] value = new String[] {null};
		proxy.addContentListener(new ContentListener() {
			
			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});
		
		layer.setContent("a");
		assertEquals("a", value[0]);
		proxy.setLayer(new Formula("[0-9]", "1"));
		assertEquals("1", value[0]);
		layer.setContent("b");
		assertEquals("1", value[0]);
	}

}
