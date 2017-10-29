package fr.vergne.parsing.layer.standard.impl;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.layer.standard.Constant;
import fr.vergne.parsing.layer.standard.ConstantTest;

@RunWith(JUnitPlatform.class)
public class RegexBasedConstantTest implements ConstantTest {

	@Override
	public Constant instantiateConstant(String content) {
		return new RegexBasedConstant(content);
	}

}
