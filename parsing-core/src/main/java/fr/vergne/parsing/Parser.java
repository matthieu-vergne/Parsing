package fr.vergne.parsing;

import java.io.File;

public interface Parser<S extends Structure> {

	public S parse(String string);

	public S parse(File file);
}
